package com.cfanalyzer.dao;

import com.cfanalyzer.config.DatabaseConfig;
import com.cfanalyzer.model.UserRating;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for UserRating entities.
 */
public class UserRatingDAO {

    /**
     * Insert or update a user rating record.
     */
    public boolean upsert(UserRating rating) throws SQLException {
        String sql = "INSERT INTO user_ratings (user_id, data_structure_score, algorithm_score, " +
                     "ai_usage_percentage, total_submissions, accepted_submissions) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "data_structure_score = VALUES(data_structure_score), " +
                     "algorithm_score = VALUES(algorithm_score), " +
                     "ai_usage_percentage = VALUES(ai_usage_percentage), " +
                     "total_submissions = VALUES(total_submissions), " +
                     "accepted_submissions = VALUES(accepted_submissions), " +
                     "updated_at = CURRENT_TIMESTAMP";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rating.getUserId());
            ps.setDouble(2, rating.getDataStructureScore());
            ps.setDouble(3, rating.getAlgorithmScore());
            ps.setDouble(4, rating.getAiUsagePercentage());
            ps.setInt(5, rating.getTotalSubmissions());
            ps.setInt(6, rating.getAcceptedSubmissions());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Find rating for a specific user.
     */
    public UserRating findByUserId(int userId) throws SQLException {
        String sql = "SELECT ur.*, u.username FROM user_ratings ur " +
                     "JOIN users u ON ur.user_id = u.id WHERE ur.user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Get all user ratings sorted by a given field.
     */
    public List<UserRating> findAll() throws SQLException {
        String sql = "SELECT ur.*, u.username FROM user_ratings ur " +
                     "JOIN users u ON ur.user_id = u.id " +
                     "ORDER BY (ur.data_structure_score + ur.algorithm_score) DESC";
        List<UserRating> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private UserRating mapRow(ResultSet rs) throws SQLException {
        UserRating r = new UserRating();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setUsername(rs.getString("username"));
        r.setDataStructureScore(rs.getDouble("data_structure_score"));
        r.setAlgorithmScore(rs.getDouble("algorithm_score"));
        r.setAiUsagePercentage(rs.getDouble("ai_usage_percentage"));
        r.setTotalSubmissions(rs.getInt("total_submissions"));
        r.setAcceptedSubmissions(rs.getInt("accepted_submissions"));
        r.setUpdatedAt(rs.getTimestamp("updated_at"));
        return r;
    }
}
