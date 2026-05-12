package com.cfanalyzer.dao;

import com.cfanalyzer.config.DatabaseConfig;
import com.cfanalyzer.model.Analysis;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Analysis entities.
 */
public class AnalysisDAO {

    private static final Gson GSON = new Gson();
    private static final Type STRING_LIST_TYPE = new TypeToken<List<String>>(){}.getType();

    /**
     * Insert or update an analysis record.
     */
    public boolean upsert(Analysis analysis) throws SQLException {
        String sql = "INSERT INTO ai_analysis (submission_id, data_structures, algorithms, " +
                     "ai_detection_score, ai_indicators, complexity_analysis, code_quality_score) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "data_structures = VALUES(data_structures), " +
                     "algorithms = VALUES(algorithms), " +
                     "ai_detection_score = VALUES(ai_detection_score), " +
                     "ai_indicators = VALUES(ai_indicators), " +
                     "complexity_analysis = VALUES(complexity_analysis), " +
                     "code_quality_score = VALUES(code_quality_score), " +
                     "analyzed_at = CURRENT_TIMESTAMP";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, analysis.getSubmissionId());
            ps.setString(2, GSON.toJson(analysis.getDataStructures()));
            ps.setString(3, GSON.toJson(analysis.getAlgorithms()));
            ps.setDouble(4, analysis.getAiDetectionScore());
            ps.setString(5, GSON.toJson(analysis.getAiIndicators()));
            ps.setString(6, analysis.getComplexityAnalysis());
            ps.setDouble(7, analysis.getCodeQualityScore());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Find analysis by submission ID.
     */
    public Analysis findBySubmissionId(long submissionId) throws SQLException {
        String sql = "SELECT * FROM ai_analysis WHERE submission_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, submissionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Get all analyses for a user's submissions.
     */
    public List<Analysis> findByUserId(int userId) throws SQLException {
        String sql = "SELECT a.* FROM ai_analysis a " +
                     "JOIN submissions s ON a.submission_id = s.id " +
                     "WHERE s.user_id = ?";
        List<Analysis> list = new ArrayList<>();
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
     * Get average scores across all users.
     */
    public double getAverageDataStructureScore() throws SQLException {
        String sql = "SELECT AVG(data_structure_score) FROM user_ratings";
        return getAverageFromQuery(sql);
    }

    public double getAverageAlgorithmScore() throws SQLException {
        String sql = "SELECT AVG(algorithm_score) FROM user_ratings";
        return getAverageFromQuery(sql);
    }

    public double getAverageAiUsagePercentage() throws SQLException {
        String sql = "SELECT AVG(ai_usage_percentage) FROM user_ratings";
        return getAverageFromQuery(sql);
    }

    private double getAverageFromQuery(String sql) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }

    private Analysis mapRow(ResultSet rs) throws SQLException {
        Analysis a = new Analysis();
        a.setId(rs.getInt("id"));
        a.setSubmissionId(rs.getLong("submission_id"));

        String dsJson = rs.getString("data_structures");
        try {
            a.setDataStructures(dsJson != null ? GSON.fromJson(dsJson, STRING_LIST_TYPE) : new ArrayList<>());
        } catch (JsonSyntaxException e) {
            a.setDataStructures(new ArrayList<>());
        }

        String algJson = rs.getString("algorithms");
        try {
            a.setAlgorithms(algJson != null ? GSON.fromJson(algJson, STRING_LIST_TYPE) : new ArrayList<>());
        } catch (JsonSyntaxException e) {
            a.setAlgorithms(new ArrayList<>());
        }

        a.setAiDetectionScore(rs.getDouble("ai_detection_score"));

        String indJson = rs.getString("ai_indicators");
        try {
            a.setAiIndicators(indJson != null ? GSON.fromJson(indJson, STRING_LIST_TYPE) : new ArrayList<>());
        } catch (JsonSyntaxException e) {
            a.setAiIndicators(new ArrayList<>());
        }

        a.setComplexityAnalysis(rs.getString("complexity_analysis"));
        a.setCodeQualityScore(rs.getDouble("code_quality_score"));
        a.setAnalyzedAt(rs.getTimestamp("analyzed_at"));
        return a;
    }
}
