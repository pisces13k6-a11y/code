package com.cfanalyzer.dao;

import com.cfanalyzer.config.DatabaseConfig;
import com.cfanalyzer.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class UserDAO {
    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());

    /**
     * Insert a new user
     */
    public void insert(User user) {
        String sql = "INSERT INTO users (handle, active, created_at) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getHandle());
            pstmt.setBoolean(2, user.isActive());
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            
            pstmt.executeUpdate();
            logger.info("User inserted: " + user.getHandle());
            
        } catch (SQLException e) {
            logger.warning("Insert user failed: " + e.getMessage());
        }
    }

    /**
     * Find user by ID
     */
    public User findById(long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            
        } catch (SQLException e) {
            logger.warning("Find user by ID failed: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Find user by handle
     * ✅ NEW METHOD
     */
    public User findByHandle(String handle) {
        String sql = "SELECT * FROM users WHERE handle = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, handle);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            
        } catch (SQLException e) {
            logger.warning("Find user by handle failed: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Find all users
     */
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            logger.warning("Find all users failed: " + e.getMessage());
        }
        
        return users;
    }

    /**
     * Delete user by ID
     */
    public void delete(long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("User deleted: ID=" + id);
            }
            
        } catch (SQLException e) {
            logger.warning("Delete user failed: " + e.getMessage());
        }
    }

    /**
     * Update user's last crawled time
     */
    public void updateLastCrawledAt(long userId) {
        String sql = "UPDATE users SET last_crawled_at = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(2, userId);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Updated last crawled time for user: " + userId);
            }
            
        } catch (SQLException e) {
            logger.warning("Update last crawled at failed: " + e.getMessage());
        }
    }

    /**
     * Map ResultSet to User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setHandle(rs.getString("handle"));
        user.setActive(rs.getBoolean("active"));
        
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            user.setCreatedAt(createdTs.toLocalDateTime());
        }
        
        Timestamp lastCrawledTs = rs.getTimestamp("last_crawled_at");
        if (lastCrawledTs != null) {
            user.setLastCrawledAt(lastCrawledTs.toLocalDateTime());
        }
        
        return user;
    }
}