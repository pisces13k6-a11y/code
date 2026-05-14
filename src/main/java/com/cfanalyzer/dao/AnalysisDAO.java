package com.cfanalyzer.dao;

import com.cfanalyzer.config.DatabaseConfig;
import com.cfanalyzer.model.Analysis;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AnalysisDAO {
    private static final Logger logger = Logger.getLogger(AnalysisDAO.class.getName());

    public void insert(Analysis analysis) {
        String sql = "INSERT INTO analysis_results (submission_id, data_structures, algorithms, " +
                     "ai_detection_score, ai_confidence, summary, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE ai_detection_score=VALUES(ai_detection_score)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, analysis.getSubmissionId());
            pstmt.setString(2, String.join(",", analysis.getDataStructures()));
            pstmt.setString(3, String.join(",", analysis.getAlgorithms()));
            pstmt.setDouble(4, analysis.getAiDetectionScore());
            pstmt.setDouble(5, analysis.getAiConfidence());
            pstmt.setString(6, analysis.getSummary());
            pstmt.setTimestamp(7, Timestamp.valueOf(java.time.LocalDateTime.now()));
            
            pstmt.executeUpdate();
            logger.info("Analysis inserted for submission: " + analysis.getSubmissionId());
            
        } catch (SQLException e) {
            logger.warning("Insert analysis failed: " + e.getMessage());
        }
    }

    /**
     * Find all analyses by user
     */
    public List<Analysis> findByUser(long userId) {
        String sql = "SELECT a.*, s.problem_name, s.language, s.verdict, s.source_code, s.submitted_at " +
                     "FROM analysis_results a " +
                     "JOIN submissions s ON a.submission_id = s.id " +
                     "JOIN users u ON s.user_id = u.id " +
                     "WHERE u.id = ? " +
                     "ORDER BY s.submitted_at DESC";
        
        List<Analysis> analyses = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                analyses.add(mapResultSetToAnalysis(rs));
            }
            
        } catch (SQLException e) {
            logger.warning("Find analyses by user failed: " + e.getMessage());
        }
        
        return analyses;
    }

    private Analysis mapResultSetToAnalysis(ResultSet rs) throws SQLException {
        Analysis analysis = new Analysis();
        analysis.setId(rs.getLong("id"));
        analysis.setSubmissionId(rs.getLong("submission_id"));
        
        String dsStr = rs.getString("data_structures");
        if (dsStr != null && !dsStr.isEmpty()) {
            for (String ds : dsStr.split(",")) {
                analysis.getDataStructures().add(ds.trim());
            }
        }
        
        String algoStr = rs.getString("algorithms");
        if (algoStr != null && !algoStr.isEmpty()) {
            for (String algo : algoStr.split(",")) {
                analysis.getAlgorithms().add(algo.trim());
            }
        }
        
        analysis.setAiDetectionScore(rs.getDouble("ai_detection_score"));
        analysis.setAiConfidence(rs.getDouble("ai_confidence"));
        analysis.setSummary(rs.getString("summary"));
        analysis.setProblemName(rs.getString("problem_name"));
        analysis.setLanguage(rs.getString("language"));
        analysis.setVerdict(rs.getString("verdict"));
        analysis.setSourceCode(rs.getString("source_code"));
        
        Timestamp submittedTs = rs.getTimestamp("submitted_at");
        if (submittedTs != null) {
            analysis.setSubmittedAt(submittedTs.toLocalDateTime());
        }
        
        return analysis;
    }
}