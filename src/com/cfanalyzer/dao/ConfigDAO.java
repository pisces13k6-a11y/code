package com.cfanalyzer.dao;

import com.cfanalyzer.config.DatabaseConfig;

import java.sql.*;

/**
 * Data Access Object for application configuration stored in database.
 */
public class ConfigDAO {

    /**
     * Get a configuration value by key.
     */
    public String getValue(String key) throws SQLException {
        String sql = "SELECT config_value FROM config WHERE config_key = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("config_value");
            }
        }
        return null;
    }

    /**
     * Get a configuration value with a default fallback.
     */
    public String getValue(String key, String defaultValue) {
        try {
            String val = getValue(key);
            return (val != null && !val.isEmpty()) ? val : defaultValue;
        } catch (SQLException e) {
            return defaultValue;
        }
    }

    /**
     * Set a configuration value.
     */
    public void setValue(String key, String value) throws SQLException {
        String sql = "INSERT INTO config (config_key, config_value) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), updated_at = CURRENT_TIMESTAMP";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }

    /**
     * Get crawl interval in hours from config.
     */
    public int getCrawlIntervalHours() {
        try {
            String val = getValue("crawl_interval_hours");
            if (val != null && !val.isEmpty()) return Integer.parseInt(val);
        } catch (Exception ignored) {}
        return 24;
    }

    /**
     * Get Groq API key from config.
     */
    public String getGroqApiKey() {
        return getValue("groq_api_key", "");
    }
}
