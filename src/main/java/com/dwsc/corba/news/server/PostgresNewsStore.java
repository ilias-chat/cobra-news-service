package com.dwsc.corba.news.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PostgresNewsStore implements NewsStore {

    private final String jdbcUrl;
    private final String user;
    private final String password;

    public PostgresNewsStore(String jdbcUrl, String user, String password) {
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.password = password;
        initSchema();
        seedIfEmpty();
    }

    @Override
    public List<NewsRecord> list() {
        String sql = "select id, title, content, date from corba_news order by date desc";
        List<NewsRecord> out = new ArrayList<>();
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new NewsRecord(rs.getString("id"), rs.getString("title"), rs.getString("content"), rs.getString("date")));
            }
            return out;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed listing news from Postgres", ex);
        }
    }

    @Override
    public NewsRecord get(String id) {
        String sql = "select id, title, content, date from corba_news where id = ?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new NewsRecord(rs.getString("id"), rs.getString("title"), rs.getString("content"), rs.getString("date"));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed reading news from Postgres", ex);
        }
    }

    @Override
    public void put(NewsRecord record) {
        String sql =
                "insert into corba_news(id, title, content, date) values (?, ?, ?, ?) "
                        + "on conflict (id) do update set title = excluded.title, content = excluded.content, date = excluded.date";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, record.id());
            ps.setString(2, record.title());
            ps.setString(3, record.content());
            ps.setString(4, record.date());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed upserting news into Postgres", ex);
        }
    }

    @Override
    public boolean delete(String id) {
        String sql = "delete from corba_news where id = ?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed deleting news from Postgres", ex);
        }
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, user, password);
    }

    private void initSchema() {
        String ddl =
                "create table if not exists corba_news ("
                        + "id varchar(128) primary key, "
                        + "title varchar(512) not null, "
                        + "content text not null, "
                        + "date varchar(64) not null"
                        + ")";
        try (Connection c = open(); Statement s = c.createStatement()) {
            s.execute(ddl);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed initializing corba_news schema", ex);
        }
    }

    private void seedIfEmpty() {
        String countSql = "select count(*) from corba_news";
        try (Connection c = open(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(countSql)) {
            rs.next();
            if (rs.getLong(1) > 0) {
                return;
            }
            put(
                    new NewsRecord(
                            "news-001",
                            "Player scouting feed is live",
                            "CORBA producer is online and broadcasting player news events.",
                            Instant.now().toString()));
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed seeding corba_news table", ex);
        }
    }
}
