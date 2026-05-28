package com.dwsc.corba.news.client;

import dwsc.corba.news.NewsItem;
import dwsc.corba.news.NewsService;
import dwsc.corba.news.NewsServiceHelper;
import java.util.ArrayList;
import java.util.List;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

/**
 * Reusable CORBA consumer client.
 *
 * <p>The ORB and the resolved service stub are initialised once and reused across all requests.
 * Creating a new JacORB ORB per request is expensive (spawns threads, opens sockets, may block on
 * hostname resolution) and causes silent hangs in constrained environments such as Cloud Run.
 */
public class CorbaNewsClient {

    private final String orbHost;
    private final int orbPort;
    private final String serviceName;

    private ORB orb;
    private NewsService cachedService;

    public CorbaNewsClient(String orbHost, int orbPort, String serviceName) {
        this.orbHost = orbHost;
        this.orbPort = orbPort;
        this.serviceName = serviceName;
    }

    public synchronized List<NewsRow> listNews() throws Exception {
        ensureConnected();
        try {
            NewsItem[] rows = cachedService.listNews();
            List<NewsRow> out = new ArrayList<>(rows.length);
            for (NewsItem item : rows) {
                out.add(new NewsRow(item.id, item.title, item.content, item.date));
            }
            return out;
        } catch (Exception ex) {
            cachedService = null;
            throw ex;
        }
    }

    private void ensureConnected() throws Exception {
        if (cachedService != null) {
            return;
        }
        if (orb == null) {
            orb =
                    ORB.init(
                            new String[] {
                                "-ORBInitialHost", orbHost,
                                "-ORBInitialPort", String.valueOf(orbPort),
                                "-ORBInitRef",
                                        "NameService=corbaloc::"
                                                + orbHost
                                                + ":"
                                                + orbPort
                                                + "/NameService"
                            },
                            null);
        }
        org.omg.CORBA.Object namingRef = orb.resolve_initial_references("NameService");
        NamingContextExt naming = NamingContextExtHelper.narrow(namingRef);
        cachedService = NewsServiceHelper.narrow(naming.resolve_str(serviceName));
        System.out.println("CORBA client connected to " + serviceName);
    }
}
