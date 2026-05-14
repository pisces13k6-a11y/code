package com.cfanalyzer.analyzer;

import com.cfanalyzer.config.DatabaseConfig;
import com.cfanalyzer.dao.AnalysisDAO;
import com.cfanalyzer.dao.SubmissionDAO;
import com.cfanalyzer.model.Analysis;
import com.cfanalyzer.model.Submission;

import java.util.List;
import java.util.logging.Logger;

public class CodeAnalysisEngine {
    private static final Logger logger = Logger.getLogger(CodeAnalysisEngine.class.getName());
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final AnalysisDAO analysisDAO = new AnalysisDAO();
    private final GroqAIAnalyzer aiAnalyzer = new GroqAIAnalyzer();

    public void analyzeSubmission(Submission submission) {
        try {
            String sourceCode = submission.getSourceCode();
            
            if (sourceCode == null || sourceCode.trim().isEmpty()) {
                logger.warning("No source code for submission: " + submission.getId());
                return;
            }

            String apiKey = getGroqApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                logger.warning("Groq API key not configured");
                return;
            }

            GroqAIAnalyzer.AnalysisResult result = aiAnalyzer.analyze(
                sourceCode, 
                apiKey, 
                "mixtral-8x7b-32768"
            );

            // Tạo Analysis object
            Analysis analysis = new Analysis();
            analysis.setSubmissionId(submission.getId());
            analysis.setDataStructures(result.dataStructures);
            analysis.setAlgorithms(result.algorithms);
            analysis.setAiDetectionScore(result.aiDetectionScore);
            analysis.setAiConfidence(result.aiConfidence);
            analysis.setSummary(result.summary);
            analysis.setProblemName(submission.getProblemName());
            analysis.setLanguage(submission.getLanguage());
            analysis.setVerdict(submission.getVerdict());
            analysis.setSourceCode(submission.getSourceCode());
            analysis.setSubmittedAt(submission.getSubmittedAt());

            // Save to database
            analysisDAO.insert(analysis);
            logger.info("Analysis completed for submission: " + submission.getId());

        } catch (Exception e) {
            logger.severe("Analysis failed for submission " + submission.getId() + 
                         ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getGroqApiKey() {
        return DatabaseConfig.getGroqApiKey();
    }
}