package com.cfanalyzer.dao;

import com.cfanalyzer.model.Analysis;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnalysisDAO {
    private static final Logger LOGGER = Logger.getLogger(AnalysisDAO.class.getName());
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<List<String>>() {}.getType();

    public void save(Analysis analysis) {
        String sql = "INSERT INTO analysis_results(submission_id, data_structures, algorithms, ai_detection_score, ai_confidence, summary) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE data_structures=VALUES(data_structures), algorithms=VALUES(algorithms), ai_detection_score=VALUES(ai_detection_score), ai_confidence=VALUES(ai_confidence), summary=VALUES(summary)";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, analysis.getSubmissionId());
            ps.setString(2, gson.toJson(analysis.getDataStructures()));
            ps.setString(3, gson.toJson(analysis.getAlgorithms()));
            ps.setDouble(4, analysis.getAiDetectionScore());
            ps.setDouble(5, analysis.getAiConfidence());
            ps.setString(6, analysis.getSummary());
            ps.executeUpdate();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to save analysis for submission: " + analysis.getSubmissionId(), ex);
        }
    }

    public List<Analysis> findByUser(long userId) {
        List<Analysis> out = new ArrayList<>();
        String sql = "SELECT a.id, a.submission_id, a.data_structures, a.algorithms, a.ai_detection_score, a.ai_confidence, a.summary FROM analysis_results a INNER JOIN submissions s ON s.id = a.submission_id WHERE s.user_id = ?";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Analysis a = new Analysis();
                    a.setId(rs.getLong("id"));
                    a.setSubmissionId(rs.getLong("submission_id"));
                    String ds = rs.getString("data_structures");
                    String alg = rs.getString("algorithms");
                    a.setDataStructures(ds == null ? new ArrayList<>() : gson.fromJson(ds, listType));
                    a.setAlgorithms(alg == null ? new ArrayList<>() : gson.fromJson(alg, listType));
                    a.setAiDetectionScore(rs.getDouble("ai_detection_score"));
                    a.setAiConfidence(rs.getDouble("ai_confidence"));
                    a.setSummary(rs.getString("summary"));
                    out.add(a);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to fetch analyses for user: " + userId, ex);
        }
        return out;
    }
}
