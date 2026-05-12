package com.cfanalyzer.analyzer;

import com.cfanalyzer.dao.AnalysisDAO;
import com.cfanalyzer.dao.ConfigDAO;
import com.cfanalyzer.model.Analysis;
import com.cfanalyzer.model.Submission;

public class CodeAnalysisEngine {
    private final GroqAIAnalyzer analyzer = new GroqAIAnalyzer();
    private final AnalysisDAO analysisDAO = new AnalysisDAO();
    private final ConfigDAO configDAO = new ConfigDAO();

    public Analysis analyzeSubmission(Submission submission) {
        String apiKey = configDAO.getValue("groq_api_key", "");
        String model = configDAO.getValue("groq_model", "mixtral-8x7b-32768");
        GroqAIAnalyzer.AnalysisResult result = analyzer.analyze(submission.getSourceCode(), apiKey, model);

        Analysis analysis = new Analysis();
        analysis.setSubmissionId(submission.getId());
        analysis.setDataStructures(result.dataStructures);
        analysis.setAlgorithms(result.algorithms);
        analysis.setAiDetectionScore(result.aiDetectionScore);
        analysis.setAiConfidence(result.aiConfidence);
        analysis.setSummary(result.summary);
        analysisDAO.save(analysis);
        return analysis;
    }
}
