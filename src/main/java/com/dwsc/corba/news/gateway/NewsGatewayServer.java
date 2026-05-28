package com.dwsc.corba.news.gateway;

import com.dwsc.corba.news.client.CorbaNewsClient;
import com.dwsc.corba.news.client.NewsRow;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Standalone REST gateway that translates HTTP JSON calls into CORBA calls.
 */
public final class NewsGatewayServer {

    private static final String DEFAULT_ORB_HOST = "127.0.0.1";
    private static final int DEFAULT_ORB_PORT = 1050;
    private static final String DEFAULT_SERVICE_NAME = "NewsService";
    private static final int DEFAULT_HTTP_PORT = 8095;

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private NewsGatewayServer() {}

    public static void main(String[] args) throws Exception {
        String orbHost = env("ORB_HOST", DEFAULT_ORB_HOST);
        int orbPort = parseInt(env("ORB_PORT", String.valueOf(DEFAULT_ORB_PORT)), DEFAULT_ORB_PORT);
        String serviceName = env("CORBA_SERVICE_NAME", DEFAULT_SERVICE_NAME);
        String httpPortRaw = System.getenv("PORT");
        if (httpPortRaw == null || httpPortRaw.isBlank()) {
            httpPortRaw = env("GATEWAY_HTTP_PORT", String.valueOf(DEFAULT_HTTP_PORT));
        }
        int httpPort = parseInt(httpPortRaw.trim(), DEFAULT_HTTP_PORT);

        CorbaNewsClient corbaClient = new CorbaNewsClient(orbHost, orbPort, serviceName);
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", httpPort), 0);
        server.createContext("/health", exchange -> handleHealth(exchange));
        server.createContext("/api/news", exchange -> handleNews(exchange, corbaClient));
        server.createContext("/", exchange -> writeJson(exchange, 404, Map.of("error", "Not found")));
        server.start();

        System.out.println("Standalone CORBA News Gateway started");
        System.out.printf("HTTP port: %d%n", httpPort);
        System.out.printf("ORB host: %s%n", orbHost);
        System.out.printf("ORB port: %d%n", orbPort);
        System.out.printf("Service name: %s%n", serviceName);
    }

    private static void handleHealth(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }
        writeJson(exchange, 200, Map.of("status", "ok"));
    }

    private static void handleNews(HttpExchange exchange, CorbaNewsClient corbaClient) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }

        String path = exchange.getRequestURI().getPath();
        try {
            if ("/api/news".equals(path) || "/api/news/".equals(path)) {
                List<NewsRow> rows = corbaClient.listNews();
                writeJson(exchange, 200, rows);
                return;
            }

            if (path.startsWith("/api/news/")) {
                String id = path.substring("/api/news/".length()).trim();
                if (id.isEmpty()) {
                    writeJson(exchange, 400, Map.of("error", "Missing id"));
                    return;
                }
                List<NewsRow> rows = corbaClient.listNews();
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
        } catch (Exception ex) {
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

    private static void writeJson(HttpExchange exchange, int statusCode, Object payload) throws IOException {
        byte[] data = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
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
