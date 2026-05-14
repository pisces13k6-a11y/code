package com.cfanalyzer.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseConfig {
    private static final Logger logger = Logger.getLogger(DatabaseConfig.class.getName());
    
    // ========== CẤU HÌNH DATABASE ==========
    private static String host = "localhost";
    private static int port = 3306;
    private static String database = "codeforces_analyzer";
    private static String username = "root";
    private static String password = "root";
    private static String groqApiKey = "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";

    // Load MySQL driver khi class được khởi tạo
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("MySQL Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            logger.severe("MySQL Driver not found: " + e.getMessage());
        }
    }


    public static Connection getConnection() throws SQLException {
        try {
            // Tạo URL kết nối
            String url = String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                host, port, database
            );
            
            // Kết nối đến database
            Connection conn = DriverManager.getConnection(url, username, password);
            logger.info("Database connection established");
            return conn;
            
        } catch (SQLException e) {
            logger.severe("Failed to connect to database: " + e.getMessage());
            throw e;
        }
    }

    // ========== GETTER & SETTER ==========
    public static String getHost() { 
        return host; 
    }
    public static void setHost(String h) { 
        host = h; 
    }
    
    public static int getPort() { 
        return port; 
    }
    public static void setPort(int p) { 
        port = p; 
    }
    
    public static String getDatabase() { 
        return database; 
    }
    public static void setDatabase(String db) { 
        database = db; 
    }
    
    public static String getUsername() { 
        return username; 
    }
    public static void setUsername(String u) { 
        username = u; 
    }
    
    public static String getPassword() { 
        return password; 
    }
    public static void setPassword(String p) { 
        password = p; 
    }
    public static String getGroqApiKey() {
        return groqApiKey;
    }
}