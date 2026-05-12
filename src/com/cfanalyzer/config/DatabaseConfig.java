package com.cfanalyzer.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database configuration and connection management.
 */
public class DatabaseConfig {

    private static String host = "localhost";
    private static int port = 3306;
    private static String database = "codeforces_analyzer";
    private static String username = "root";
    private static String password = "";

    private static Connection connection;

    public static String getHost() { return host; }
    public static void setHost(String h) { host = h; }

    public static int getPort() { return port; }
    public static void setPort(int p) { port = p; }

    public static String getDatabase() { return database; }
    public static void setDatabase(String db) { database = db; }

    public static String getUsername() { return username; }
    public static void setUsername(String u) { username = u; }

    public static String getPassword() { return password; }
    public static void setPassword(String p) { password = p; }

    /**
     * Get a database connection, reusing existing if still valid.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = createNewConnection();
        }
        return connection;
    }

    /**
     * Create a new database connection.
     */
    public static Connection createNewConnection() throws SQLException {
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8mb4",
                host, port, database);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Please add mysql-connector-java.jar to lib/", e);
        }
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Test if database connection is available.
     */
    public static boolean testConnection() {
        try (Connection conn = createNewConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Close the shared connection.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("[ERROR] Failed to close connection: " + e.getMessage());
            }
            connection = null;
        }
    }

    /**
     * Build JDBC URL from current settings.
     */
    public static String buildUrl() {
        return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                host, port, database);
    }
}
