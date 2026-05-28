package com.dwsc.corba.news.server;

import java.util.List;

public interface NewsStore {
    List<NewsRecord> list();

    NewsRecord get(String id);

    void put(NewsRecord record);

    boolean delete(String id);
}
