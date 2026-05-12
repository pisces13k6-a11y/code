package com.cfanalyzer.dao;

import com.cfanalyzer.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseManager {
    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DatabaseConfig.jdbcUrl(), DatabaseConfig.getUsername(), DatabaseConfig.getPassword());
    }
}
