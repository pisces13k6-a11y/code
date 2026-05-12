package com.cfanalyzer.dao;

import com.cfanalyzer.config.DatabaseConfig;
import com.cfanalyzer.model.Submission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Submission entities.
 */
public class SubmissionDAO {

    /**
     * Insert or update a submission.
     */
    public boolean upsert(Submission sub) throws SQLException {
        String sql = "INSERT INTO submissions (id, user_id, problem_name, problem_rating, language, " +
                     "verdict, time_consumed, memory_consumed, submission_date, code, analyzed) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "verdict = VALUES(verdict), code = COALESCE(VALUES(code), code), analyzed = analyzed";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sub.getId());
            ps.setInt(2, sub.getUserId());
            ps.setString(3, sub.getProblemName());
            ps.setInt(4, sub.getProblemRating());
            ps.setString(5, sub.getLanguage());
            ps.setString(6, sub.getVerdict());
            ps.setInt(7, sub.getTimeConsumed());
            ps.setInt(8, sub.getMemoryConsumed());
            ps.setTimestamp(9, sub.getSubmissionDate());
            ps.setString(10, sub.getCode());
            ps.setBoolean(11, sub.isAnalyzed());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Get submissions for a user that have not yet been analyzed.
     */
    public List<Submission> findUnanalyzedByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM submissions WHERE user_id = ? AND analyzed = FALSE ORDER BY id DESC";
        List<Submission> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Get all submissions for a user.
     */
    public List<Submission> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM submissions WHERE user_id = ? ORDER BY submission_date DESC";
        List<Submission> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Get submission by ID.
     */
    public Submission findById(long id) throws SQLException {
        String sql = "SELECT * FROM submissions WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Get the maximum submission ID for a user (for incremental crawling).
     */
    public long getMaxSubmissionId(int userId) throws SQLException {
        String sql = "SELECT MAX(id) FROM submissions WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return 0L;
    }

    /**
     * Mark a submission as analyzed.
     */
    public void markAnalyzed(long submissionId) throws SQLException {
        String sql = "UPDATE submissions SET analyzed = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, submissionId);
            ps.executeUpdate();
        }
    }

    /**
     * Get the most recent N submissions across all users.
     */
    public List<Submission> findRecent(int limit) throws SQLException {
        String sql = "SELECT s.*, u.username FROM submissions s " +
                     "JOIN users u ON s.user_id = u.id " +
                     "ORDER BY s.submission_date DESC LIMIT ?";
        List<Submission> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Get total analyzed submission count.
     */
    public int getTotalAnalyzedCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM submissions WHERE analyzed = TRUE";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Get total submission count.
     */
    public int getTotalCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM submissions";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Submission mapRow(ResultSet rs) throws SQLException {
        Submission sub = new Submission();
        sub.setId(rs.getLong("id"));
        sub.setUserId(rs.getInt("user_id"));
        sub.setProblemName(rs.getString("problem_name"));
        sub.setProblemRating(rs.getInt("problem_rating"));
        sub.setLanguage(rs.getString("language"));
        sub.setVerdict(rs.getString("verdict"));
        sub.setTimeConsumed(rs.getInt("time_consumed"));
        sub.setMemoryConsumed(rs.getInt("memory_consumed"));
        sub.setSubmissionDate(rs.getTimestamp("submission_date"));
        sub.setCode(rs.getString("code"));
        sub.setAnalyzed(rs.getBoolean("analyzed"));
        return sub;
    }
}
