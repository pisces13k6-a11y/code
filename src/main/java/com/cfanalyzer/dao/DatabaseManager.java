package com.cfanalyzer.dao;

import com.cfanalyzer.config.DatabaseConfig;
import java.sql.Connection;
import java.util.logging.Logger;

public class DatabaseManager {
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());

    public static void initializeDatabase() {
        try {
            Connection conn = DatabaseConfig.getConnection();
            if (conn != null) {
                logger.info("Database initialized successfully");
                conn.close();
            }
        } catch (Exception e) {
            logger.severe("Database initialization failed: " + e.getMessage());
        }
    }

    public static boolean testConnection() {
        try {
            Connection conn = DatabaseConfig.getConnection();
            boolean connected = conn != null && !conn.isClosed();
            if (conn != null) conn.close();
            return connected;
        } catch (Exception e) {
            logger.warning("Connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Shutdown database resources
     */
    public static void shutdown() {
        logger.info("Database manager shutdown");
    }
}