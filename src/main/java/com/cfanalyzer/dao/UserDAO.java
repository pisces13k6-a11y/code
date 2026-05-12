package com.cfanalyzer.dao;

import com.cfanalyzer.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public long addUser(String handle) throws SQLException {
        String sql = "INSERT INTO users(handle, active) VALUES (?, TRUE)";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, handle);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return -1;
    }

    public void deleteUser(long id) throws SQLException {
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, handle, active, created_at, last_crawled_at FROM users ORDER BY id DESC";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setHandle(rs.getString("handle"));
                u.setActive(rs.getBoolean("active"));
                Timestamp created = rs.getTimestamp("created_at");
                Timestamp crawled = rs.getTimestamp("last_crawled_at");
                if (created != null) u.setCreatedAt(created.toLocalDateTime());
                if (crawled != null) u.setLastCrawledAt(crawled.toLocalDateTime());
                users.add(u);
            }
        } catch (Exception ignored) {
        }
        return users;
    }

    public void updateLastCrawled(long userId) {
        String sql = "UPDATE users SET last_crawled_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }
}
