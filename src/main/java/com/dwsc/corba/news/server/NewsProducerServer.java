package com.dwsc.corba.news.server;

import dwsc.corba.news.NewsService;
import dwsc.corba.news.NewsServiceHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * CORBA Producer process.
 *
 * Run with:
 * mvn -f corba-news-service/pom.xml exec:java -Dexec.mainClass=com.dwsc.corba.news.server.NewsProducerServer
 */
public final class NewsProducerServer {

    private static final String DEFAULT_ORB_HOST = "0.0.0.0";
    private static final int DEFAULT_ORB_PORT = 1050;
    private static final String DEFAULT_SERVICE_NAME = "NewsService";

    private NewsProducerServer() {}

    public static void main(String[] args) throws Exception {
        String orbHost = env("ORB_HOST", DEFAULT_ORB_HOST);
        int orbPort = parseInt(env("ORB_PORT", String.valueOf(DEFAULT_ORB_PORT)), DEFAULT_ORB_PORT);
        String serviceName = env("CORBA_SERVICE_NAME", DEFAULT_SERVICE_NAME);

        ORB orb =
                ORB.init(
                        new String[] {
                            "-ORBInitialHost", orbHost,
                            "-ORBInitialPort", String.valueOf(orbPort)
                        },
                        null);

        POA rootPoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        rootPoa.the_POAManager().activate();

        NewsServiceImpl servant = new NewsServiceImpl(new InMemoryNewsStore());
        org.omg.CORBA.Object ref = rootPoa.servant_to_reference(servant);
        NewsService href = NewsServiceHelper.narrow(ref);

        org.omg.CORBA.Object namingRef = orb.resolve_initial_references("NameService");
        NamingContextExt naming = NamingContextExtHelper.narrow(namingRef);
        NameComponent[] path = naming.to_name(serviceName);
        naming.rebind(path, href);

        System.out.println("CORBA News Producer started");
        System.out.printf("Naming host: %s%n", orbHost);
        System.out.printf("Naming port: %d%n", orbPort);
        System.out.printf("Service name: %s%n", serviceName);
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    try {
                                        orb.shutdown(true);
                                    } catch (Exception ignored) {
                                        // no-op
                                    }
                                }));
        orb.run();
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
