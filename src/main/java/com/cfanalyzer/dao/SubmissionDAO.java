package com.cfanalyzer.dao;

import com.cfanalyzer.model.Submission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SubmissionDAO {
    private static final Logger LOGGER = Logger.getLogger(SubmissionDAO.class.getName());

    public void saveIfNotExists(Submission s) {
        String sql = "INSERT IGNORE INTO submissions(user_id, cf_submission_id, contest_id, problem_id, problem_name, language, verdict, submitted_at, source_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, s.getUserId());
            ps.setLong(2, s.getCfSubmissionId());
            if (s.getContestId() == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, s.getContestId());
            ps.setString(4, s.getProblemId());
            ps.setString(5, s.getProblemName());
            ps.setString(6, s.getLanguage());
            ps.setString(7, s.getVerdict());
            if (s.getSubmittedAt() == null) ps.setNull(8, Types.TIMESTAMP); else ps.setTimestamp(8, Timestamp.valueOf(s.getSubmittedAt()));
            ps.setString(9, s.getSourceCode());
            ps.executeUpdate();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to save submission: " + s.getCfSubmissionId(), ex);
        }
    }

    public List<Submission> findByUser(long userId) {
        List<Submission> out = new ArrayList<>();
        String sql = "SELECT id, user_id, cf_submission_id, contest_id, problem_id, problem_name, language, verdict, submitted_at, source_code FROM submissions WHERE user_id = ? ORDER BY submitted_at DESC LIMIT 200";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Submission s = map(rs);
                    out.add(s);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to fetch submissions for user: " + userId, ex);
        }
        return out;
    }

    public List<Submission> findUnanalyzedByUser(long userId) {
        List<Submission> out = new ArrayList<>();
        String sql = "SELECT s.id, s.user_id, s.cf_submission_id, s.contest_id, s.problem_id, s.problem_name, s.language, s.verdict, s.submitted_at, s.source_code FROM submissions s LEFT JOIN analysis_results a ON a.submission_id = s.id WHERE s.user_id = ? AND a.id IS NULL ORDER BY s.submitted_at DESC LIMIT 100";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to fetch unanalyzed submissions for user: " + userId, ex);
        }
        return out;
    }

    public int countByUser(long userId) {
        String sql = "SELECT COUNT(*) FROM submissions WHERE user_id = ?";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to count submissions for user: " + userId, ex);
        }
        return 0;
    }

    private Submission map(ResultSet rs) throws SQLException {
        Submission s = new Submission();
        s.setId(rs.getLong("id"));
        s.setUserId(rs.getLong("user_id"));
        s.setCfSubmissionId(rs.getLong("cf_submission_id"));
        int contestId = rs.getInt("contest_id");
        s.setContestId(rs.wasNull() ? null : contestId);
        s.setProblemId(rs.getString("problem_id"));
        s.setProblemName(rs.getString("problem_name"));
        s.setLanguage(rs.getString("language"));
        s.setVerdict(rs.getString("verdict"));
        Timestamp ts = rs.getTimestamp("submitted_at");
        if (ts != null) s.setSubmittedAt(ts.toLocalDateTime());
        s.setSourceCode(rs.getString("source_code"));
        return s;
    }
}
