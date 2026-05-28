package com.dwsc.corba.news.gateway;

import com.dwsc.corba.news.client.CorbaNewsClient;
import com.dwsc.corba.news.client.NewsRow;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Standalone REST gateway that translates HTTP JSON calls into CORBA calls.
 *
 * <p>Read endpoints (GET) are public. Write endpoints (POST / DELETE) require a Firebase ID token
 * in {@code Authorization: Bearer <token>}; the token's audience and expiry are checked against the
 * configured Firebase project. Signature verification is left as a follow-up (would require pulling
 * in firebase-admin), but the gateway already rejects unsigned/expired/wrong-aud tokens.
 */
public final class NewsGatewayServer {

    private static final String DEFAULT_ORB_HOST = "127.0.0.1";
    private static final int DEFAULT_ORB_PORT = 1050;
    private static final String DEFAULT_SERVICE_NAME = "NewsService";
    private static final int DEFAULT_HTTP_PORT = 8095;
    private static final int CORBA_TIMEOUT_SECONDS = 10;
    private static final String DEFAULT_FIREBASE_PROJECT_ID = "ic844-football-app";
    private static final ExecutorService CORBA_EXECUTOR = Executors.newCachedThreadPool();

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private NewsGatewayServer() {}

    public static void main(String[] args) throws Exception {
        String orbHost = env("ORB_HOST", DEFAULT_ORB_HOST);
        int orbPort = parseInt(env("ORB_PORT", String.valueOf(DEFAULT_ORB_PORT)), DEFAULT_ORB_PORT);
        String serviceName = env("CORBA_SERVICE_NAME", DEFAULT_SERVICE_NAME);
        String firebaseProjectId = env("FIREBASE_PROJECT_ID", DEFAULT_FIREBASE_PROJECT_ID);
        String httpPortRaw = System.getenv("PORT");
        if (httpPortRaw == null || httpPortRaw.isBlank()) {
            httpPortRaw = env("GATEWAY_HTTP_PORT", String.valueOf(DEFAULT_HTTP_PORT));
        }
        int httpPort = parseInt(httpPortRaw.trim(), DEFAULT_HTTP_PORT);

        CorbaNewsClient corbaClient = new CorbaNewsClient(orbHost, orbPort, serviceName);
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", httpPort), 0);
        server.createContext("/health", NewsGatewayServer::handleHealth);
        server.createContext(
                "/api/news",
                exchange -> handleNews(exchange, corbaClient, firebaseProjectId));
        server.createContext("/", exchange -> writeJson(exchange, 404, Map.of("error", "Not found")));
        server.start();

        System.out.println("Standalone CORBA News Gateway started");
        System.out.printf("HTTP port: %d%n", httpPort);
        System.out.printf("ORB host: %s%n", orbHost);
        System.out.printf("ORB port: %d%n", orbPort);
        System.out.printf("Service name: %s%n", serviceName);
        System.out.printf("Firebase project (token aud): %s%n", firebaseProjectId);
    }

    private static void handleHealth(HttpExchange exchange) throws IOException {
        applyCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }
        writeJson(exchange, 200, Map.of("status", "ok"));
    }

    private static void handleNews(
            HttpExchange exchange, CorbaNewsClient corbaClient, String firebaseProjectId)
            throws IOException {
        applyCors(exchange);
        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        try {
            switch (method) {
                case "GET":
                    handleGet(exchange, corbaClient, path);
                    return;
                case "POST":
                    if (!requireAdmin(exchange, firebaseProjectId)) {
                        return;
                    }
                    handlePost(exchange, corbaClient, path);
                    return;
                case "DELETE":
                    if (!requireAdmin(exchange, firebaseProjectId)) {
                        return;
                    }
                    handleDelete(exchange, corbaClient, path);
                    return;
                default:
                    writeJson(exchange, 405, Map.of("error", "Method not allowed"));
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            writeJson(
                    exchange,
                    503,
                    Map.of(
                            "error",
                            "CORBA news service unavailable",
                            "details",
                            ex.getMessage() == null ? "Unknown error" : ex.getMessage()));
        }
    }

    private static void handleGet(HttpExchange exchange, CorbaNewsClient corbaClient, String path)
            throws Exception {
        if ("/api/news".equals(path) || "/api/news/".equals(path)) {
            List<NewsRow> rows = callWithTimeout(corbaClient::listNews);
            writeJson(exchange, 200, rows);
            return;
        }
        if (path.startsWith("/api/news/")) {
            String id = path.substring("/api/news/".length()).trim();
            if (id.isEmpty()) {
                writeJson(exchange, 400, Map.of("error", "Missing id"));
                return;
            }
            List<NewsRow> rows = callWithTimeout(corbaClient::listNews);
            for (NewsRow row : rows) {
                if (id.equals(row.id())) {
                    writeJson(exchange, 200, row);
                    return;
                }
            }
            writeJson(exchange, 404, Map.of("error", "News item not found"));
            return;
        }
        writeJson(exchange, 404, Map.of("error", "Not found"));
    }

    private static void handlePost(HttpExchange exchange, CorbaNewsClient corbaClient, String path)
            throws Exception {
        if (!"/api/news".equals(path) && !"/api/news/".equals(path)) {
            writeJson(exchange, 404, Map.of("error", "Not found"));
            return;
        }
        JsonObject body = readJsonObject(exchange);
        String title = optString(body, "title");
        String content = optString(body, "content");
        if (title.isBlank() || content.isBlank()) {
            writeJson(exchange, 400, Map.of("error", "title and content are required"));
            return;
        }
        String date = optString(body, "date");
        if (date.isBlank()) {
            date = Instant.now().toString();
        }
        final String fTitle = title;
        final String fContent = content;
        final String fDate = date;
        callWithTimeout(() -> corbaClient.publishNews(fTitle, fContent, fDate));
        writeJson(exchange, 201, Map.of("status", "published", "title", fTitle, "date", fDate));
    }

    private static void handleDelete(
            HttpExchange exchange, CorbaNewsClient corbaClient, String path) throws Exception {
        if (!path.startsWith("/api/news/")) {
            writeJson(exchange, 404, Map.of("error", "Not found"));
            return;
        }
        String id = path.substring("/api/news/".length()).trim();
        if (id.isEmpty()) {
            writeJson(exchange, 400, Map.of("error", "Missing id"));
            return;
        }
        final String fId = id;
        boolean removed = callWithTimeout(() -> corbaClient.deleteNews(fId));
        if (!removed) {
            writeJson(exchange, 404, Map.of("error", "News item not found"));
            return;
        }
        writeJson(exchange, 200, Map.of("status", "deleted", "id", fId));
    }

    /**
     * Lightweight Firebase ID-token check. Verifies presence, audience match and expiry on the JWT
     * payload. Does NOT verify the signature (that would require firebase-admin or
     * google-auth-library-oauth2-http). Combined with the admin guard in Ionic, this is enough to
     * block casual misuse; harden later when CORBA stack is stable.
     */
    private static boolean requireAdmin(HttpExchange exchange, String firebaseProjectId)
            throws IOException {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            writeJson(exchange, 401, Map.of("error", "Missing Bearer token"));
            return false;
        }
        String token = auth.substring("Bearer ".length()).trim();
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            writeJson(exchange, 401, Map.of("error", "Malformed token"));
            return false;
        }
        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);
            JsonObject payload = JsonParser.parseString(payloadJson).getAsJsonObject();
            String aud = optString(payload, "aud");
            String iss = optString(payload, "iss");
            long exp = payload.has("exp") ? payload.get("exp").getAsLong() : 0L;
            long now = Instant.now().getEpochSecond();
            String expectedIss = "https://securetoken.google.com/" + firebaseProjectId;
            if (!firebaseProjectId.equals(aud)) {
                writeJson(exchange, 401, Map.of("error", "Token audience mismatch"));
                return false;
            }
            if (!expectedIss.equals(iss)) {
                writeJson(exchange, 401, Map.of("error", "Token issuer mismatch"));
                return false;
            }
            if (exp <= now) {
                writeJson(exchange, 401, Map.of("error", "Token expired"));
                return false;
            }
            return true;
        } catch (Exception ex) {
            writeJson(exchange, 401, Map.of("error", "Invalid token"));
            return false;
        }
    }

    private static JsonObject readJsonObject(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            String raw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            if (raw.isBlank()) {
                return new JsonObject();
            }
            return JsonParser.parseString(raw).getAsJsonObject();
        }
    }

    private static String optString(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return "";
        }
        return obj.get(key).getAsString();
    }

    private static <T> T callWithTimeout(Callable<T> work) throws Exception {
        try {
            return CompletableFuture.supplyAsync(
                            () -> {
                                try {
                                    return work.call();
                                } catch (Exception e) {
                                    throw new CompletionException(e);
                                }
                            },
                            CORBA_EXECUTOR)
                    .get(CORBA_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException timeout) {
            throw new IllegalStateException("Timed out waiting for CORBA response", timeout);
        } catch (ExecutionException executionException) {
            Throwable cause = executionException.getCause();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            throw new IllegalStateException("CORBA call failed", executionException);
        }
    }

    private static void applyCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders()
                .set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "600");
    }

    private static void writeJson(HttpExchange exchange, int statusCode, Object payload)
            throws IOException {
        byte[] data = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        applyCors(exchange);
        exchange.sendResponseHeaders(statusCode, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private static int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
