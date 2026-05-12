package com.cfanalyzer.config;

public final class DatabaseConfig {
    private static String host = "localhost";
    private static int port = 3306;
    private static String database = "codeforces_analyzer";
    private static String username = "root";
    private static String password = "";

    private DatabaseConfig() {
    }

    public static String jdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    public static String getHost() { return host; }
    public static int getPort() { return port; }
    public static String getDatabase() { return database; }
    public static String getUsername() { return username; }
    public static String getPassword() { return password; }

    public static void setHost(String host) { DatabaseConfig.host = host; }
    public static void setPort(int port) { DatabaseConfig.port = port; }
    public static void setDatabase(String database) { DatabaseConfig.database = database; }
    public static void setUsername(String username) { DatabaseConfig.username = username; }
    public static void setPassword(String password) { DatabaseConfig.password = password; }
}
