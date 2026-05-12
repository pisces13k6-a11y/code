package com.cfanalyzer.analyzer;

import com.cfanalyzer.dao.AnalysisDAO;
import com.cfanalyzer.dao.ConfigDAO;
import com.cfanalyzer.dao.SubmissionDAO;
import com.cfanalyzer.model.Analysis;
import com.cfanalyzer.model.Submission;

import java.sql.SQLException;
import java.util.List;

/**
 * Orchestrates code analysis: fetches unanalyzed submissions, calls Groq AI, persists results.
 */
public class CodeAnalysisEngine {

    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final AnalysisDAO analysisDAO = new AnalysisDAO();
    private final ConfigDAO configDAO = new ConfigDAO();
    private final GroqAIAnalyzer groqAnalyzer = new GroqAIAnalyzer();

    /**
     * Analyze all unanalyzed submissions for a user.
     * @param userId the user ID
     * @return number of submissions analyzed
     */
    public int analyzeUserSubmissions(int userId) {
        String apiKey = configDAO.getGroqApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("[WARN] " + now() + " Groq API key not configured. Skipping analysis for user " + userId);
            return 0;
        }

        int count = 0;
        try {
            List<Submission> unanalyzed = submissionDAO.findUnanalyzedByUser(userId);
            System.out.println("[INFO] " + now() + " Analyzing " + unanalyzed.size() + " submissions for user " + userId);

            for (Submission sub : unanalyzed) {
                try {
                    if (sub.getCode() == null || sub.getCode().trim().isEmpty()) {
                        submissionDAO.markAnalyzed(sub.getId());
                        continue;
                    }

                    Analysis analysis = groqAnalyzer.analyzeCode(sub.getCode(), apiKey);
                    analysis.setSubmissionId(sub.getId());
                    analysisDAO.upsert(analysis);
                    submissionDAO.markAnalyzed(sub.getId());
                    count++;

                    System.out.println("[INFO] " + now() + " Analyzed submission " + sub.getId() +
                            " | DS=" + analysis.getDataStructures() +
                            " | Alg=" + analysis.getAlgorithms() +
                            " | AI=" + analysis.getAiDetectionScore());

                    // Pause between API calls to respect rate limits
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("[ERROR] " + now() + " Failed to analyze submission " + sub.getId() + ": " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] " + now() + " Database error during analysis: " + e.getMessage());
        }
        return count;
    }

    /**
     * Re-analyze a specific submission (e.g., after updating API key).
     */
    public Analysis analyzeSubmission(long submissionId) {
        String apiKey = configDAO.getGroqApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("[WARN] Groq API key not configured.");
            return null;
        }
        try {
            Submission sub = submissionDAO.findById(submissionId);
            if (sub == null || sub.getCode() == null || sub.getCode().trim().isEmpty()) return null;

            Analysis analysis = groqAnalyzer.analyzeCode(sub.getCode(), apiKey);
            analysis.setSubmissionId(submissionId);
            analysisDAO.upsert(analysis);
            submissionDAO.markAnalyzed(submissionId);
            return analysis;
        } catch (Exception e) {
            System.err.println("[ERROR] " + now() + " Error re-analyzing submission " + submissionId + ": " + e.getMessage());
            return null;
        }
    }

    private String now() {
        return new java.util.Date().toString();
    }
}
