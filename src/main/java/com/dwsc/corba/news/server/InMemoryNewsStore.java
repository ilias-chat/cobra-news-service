package com.dwsc.corba.news.server;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory news storage, isolated from Mongo/Postgres databases.
 */
public class InMemoryNewsStore implements NewsStore {

    private final Map<String, NewsRecord> byId = new ConcurrentHashMap<>();

    public InMemoryNewsStore() {
        seed();
    }

    @Override
    public List<NewsRecord> list() {
        List<NewsRecord> rows = new ArrayList<>(byId.values());
        rows.sort(Comparator.comparing(NewsRecord::date).reversed());
        return rows;
    }

    @Override
    public NewsRecord get(String id) {
        return byId.get(id);
    }

    @Override
    public void put(NewsRecord record) {
        byId.put(record.id(), record);
    }

    @Override
    public boolean delete(String id) {
        return byId.remove(id) != null;
    }

    private void seed() {
        put(
                new NewsRecord(
                        "news-001",
                        "Player scouting feed is live",
                        "CORBA producer is online and broadcasting player news events.",
                        Instant.now().toString()));
    }
}
