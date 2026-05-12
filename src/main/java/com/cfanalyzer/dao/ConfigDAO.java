package com.cfanalyzer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigDAO {
    private static final Logger LOGGER = Logger.getLogger(ConfigDAO.class.getName());

    public String getValue(String key, String fallback) {
        String sql = "SELECT config_value FROM config WHERE config_key = ?";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to load config for key: " + key, ex);
        }
        return fallback;
    }

    public void putValue(String key, String value) {
        String sql = "INSERT INTO config(config_key, config_value) VALUES (?, ?) ON DUPLICATE KEY UPDATE config_value = VALUES(config_value)";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to save config for key: " + key, ex);
        }
    }
}
