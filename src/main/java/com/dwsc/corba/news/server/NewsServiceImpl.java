package com.dwsc.corba.news.server;

import dwsc.corba.news.NewsItem;
import dwsc.corba.news.NewsServicePOA;
import java.util.List;
import java.util.UUID;

public class NewsServiceImpl extends NewsServicePOA {

    private final NewsStore store;

    public NewsServiceImpl(NewsStore store) {
        this.store = store;
    }

    @Override
    public NewsItem[] listNews() {
        List<NewsRecord> rows = store.list();
        NewsItem[] out = new NewsItem[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            out[i] = toCorba(rows.get(i));
        }
        return out;
    }

    @Override
    public NewsItem getNews(String id) {
        NewsRecord found = store.get(id);
        if (found == null) {
            return new NewsItem("", "", "", "");
        }
        return toCorba(found);
    }

    @Override
    public void publishNews(NewsItem news) {
        String id = (news.id == null || news.id.isBlank()) ? UUID.randomUUID().toString() : news.id;
        store.put(
                new NewsRecord(
                        id,
                        safe(news.title),
                        safe(news.content),
                        safe(news.date)));
    }

    @Override
    public boolean deleteNews(String id) {
        return store.delete(id);
    }

    private static NewsItem toCorba(NewsRecord row) {
        NewsItem out = new NewsItem();
        out.id = row.id();
        out.title = row.title();
        out.content = row.content();
        out.date = row.date();
        return out;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
