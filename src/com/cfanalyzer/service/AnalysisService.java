package com.cfanalyzer.service;

import com.cfanalyzer.analyzer.CodeAnalysisEngine;
import com.cfanalyzer.dao.AnalysisDAO;
import com.cfanalyzer.dao.SubmissionDAO;
import com.cfanalyzer.model.Analysis;
import com.cfanalyzer.model.Submission;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic for submission analysis.
 */
public class AnalysisService {

    private final CodeAnalysisEngine engine = new CodeAnalysisEngine();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final AnalysisDAO analysisDAO = new AnalysisDAO();

    /**
     * Analyze all unanalyzed submissions for a user.
     */
    public int analyzeUser(int userId) {
        return engine.analyzeUserSubmissions(userId);
    }

    /**
     * Get analysis for a specific submission.
     */
    public Analysis getAnalysisForSubmission(long submissionId) throws SQLException {
        return analysisDAO.findBySubmissionId(submissionId);
    }

    /**
     * Get all analyses for a user.
     */
    public List<Analysis> getAnalysesForUser(int userId) throws SQLException {
        return analysisDAO.findByUserId(userId);
    }

    /**
     * Get all submissions for a user.
     */
    public List<Submission> getSubmissionsForUser(int userId) throws SQLException {
        return submissionDAO.findByUserId(userId);
    }

    /**
     * Get total analyzed count.
     */
    public int getTotalAnalyzedCount() throws SQLException {
        return submissionDAO.getTotalAnalyzedCount();
    }

    /**
     * Get average scores for dashboard display.
     */
    public double getAverageDataStructureScore() throws SQLException {
        return analysisDAO.getAverageDataStructureScore();
    }

    public double getAverageAlgorithmScore() throws SQLException {
        return analysisDAO.getAverageAlgorithmScore();
    }

    public double getAverageAiUsagePercentage() throws SQLException {
        return analysisDAO.getAverageAiUsagePercentage();
    }

    /**
     * Re-analyze a specific submission.
     */
    public Analysis reAnalyzeSubmission(long submissionId) {
        return engine.analyzeSubmission(submissionId);
    }
}
