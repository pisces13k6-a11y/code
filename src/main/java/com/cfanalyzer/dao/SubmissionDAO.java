package com.cfanalyzer.dao;

import com.cfanalyzer.config.DatabaseConfig;
import com.cfanalyzer.model.Submission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SubmissionDAO {
    private static final Logger logger = Logger.getLogger(SubmissionDAO.class.getName());

    /**
     * Insert a new submission
     */
    public void insert(Submission submission) {
        String sql = "INSERT INTO submissions (user_id, cf_submission_id, contest_id, problem_id, problem_name, " +
                     "language, verdict, submitted_at, source_code, tags, crawled_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE language=VALUES(language), verdict=VALUES(verdict)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, submission.getUserId());
            pstmt.setLong(2, submission.getCfSubmissionId());
            pstmt.setInt(3, submission.getContestId());
            pstmt.setString(4, submission.getProblemId());
            pstmt.setString(5, submission.getProblemName());
            pstmt.setString(6, submission.getLanguage());
            pstmt.setString(7, submission.getVerdict());
            pstmt.setTimestamp(8, submission.getSubmittedAt() != null ? 
                Timestamp.valueOf(submission.getSubmittedAt()) : null);
            pstmt.setString(9, submission.getSourceCode());
            pstmt.setString(10, submission.getTags());
            pstmt.setTimestamp(11, submission.getCrawledAt() != null ? 
                Timestamp.valueOf(submission.getCrawledAt()) : Timestamp.valueOf(java.time.LocalDateTime.now()));
            
            pstmt.executeUpdate();
            logger.info("Submission inserted: " + submission.getProblemName());
            
        } catch (SQLException e) {
            logger.warning("Insert submission failed: " + e.getMessage());
        }
    }

    /**
     * Find unanalyzed submissions by user
     */
    public List<Submission> findUnanalyzedByUser(long userId) {
        String sql = "SELECT s.* FROM submissions s " +
                     "LEFT JOIN analysis_results a ON s.id = a.submission_id " +
                     "WHERE s.user_id = ? AND a.id IS NULL " +
                     "LIMIT 10";
        
        List<Submission> submissions = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                submissions.add(mapResultSetToSubmission(rs));
            }
            
        } catch (SQLException e) {
            logger.warning("Find unanalyzed submissions failed: " + e.getMessage());
        }
        
        return submissions;
    }

    /**
     * Find all submissions by user
     */
    public List<Submission> findByUser(long userId) {
        String sql = "SELECT * FROM submissions WHERE user_id = ? ORDER BY submitted_at DESC";
        List<Submission> submissions = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                submissions.add(mapResultSetToSubmission(rs));
            }
            
        } catch (SQLException e) {
            logger.warning("Find submissions by user failed: " + e.getMessage());
        }
        
        return submissions;
    }

    /**
     * Map ResultSet to Submission object
     */
    private Submission mapResultSetToSubmission(ResultSet rs) throws SQLException {
        Submission sub = new Submission();
        sub.setId(rs.getLong("id"));
        sub.setUserId(rs.getLong("user_id"));
        sub.setCfSubmissionId(rs.getLong("cf_submission_id"));
        sub.setContestId(rs.getInt("contest_id"));
        sub.setProblemId(rs.getString("problem_id"));
        sub.setProblemName(rs.getString("problem_name"));
        sub.setLanguage(rs.getString("language"));
        sub.setVerdict(rs.getString("verdict"));
        
        Timestamp submittedTs = rs.getTimestamp("submitted_at");
        if (submittedTs != null) {
            sub.setSubmittedAt(submittedTs.toLocalDateTime());
        }
        
        sub.setSourceCode(rs.getString("source_code"));
        sub.setTags(rs.getString("tags"));
        
        Timestamp crawledTs = rs.getTimestamp("crawled_at");
        if (crawledTs != null) {
            sub.setCrawledAt(crawledTs.toLocalDateTime());
        }
        
        return sub;
    }
}