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
 */
public class CorbaNewsClient {

    private final String orbHost;
    private final int orbPort;
    private final String serviceName;

    public CorbaNewsClient(String orbHost, int orbPort, String serviceName) {
        this.orbHost = orbHost;
        this.orbPort = orbPort;
        this.serviceName = serviceName;
    }

    public List<NewsRow> listNews() throws Exception {
        ORB orb =
                ORB.init(
                        new String[] {
                            "-ORBInitialHost", orbHost,
                            "-ORBInitialPort", String.valueOf(orbPort),
                            "-ORBInitRef", "NameService=corbaloc::" + orbHost + ":" + orbPort + "/NameService"
                        },
                        null);
        try {
            org.omg.CORBA.Object namingRef = orb.resolve_initial_references("NameService");
            NamingContextExt naming = NamingContextExtHelper.narrow(namingRef);
            NewsService service = NewsServiceHelper.narrow(naming.resolve_str(serviceName));

            NewsItem[] rows = service.listNews();
            List<NewsRow> out = new ArrayList<>(rows.length);
            for (NewsItem news : rows) {
                out.add(new NewsRow(news.id, news.title, news.content, news.date));
            }
            return out;
        } finally {
            orb.destroy();
        }
    }
}
