package com.cfanalyzer.service;

import com.cfanalyzer.analyzer.CodeAnalysisEngine;
import com.cfanalyzer.dao.AnalysisDAO;
import com.cfanalyzer.dao.SubmissionDAO;
import com.cfanalyzer.model.Analysis;
import com.cfanalyzer.model.Submission;

import java.util.List;

public class AnalysisService {
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final AnalysisDAO analysisDAO = new AnalysisDAO();
    private final CodeAnalysisEngine engine = new CodeAnalysisEngine();

    public int analyzeUserSubmissions(long userId) {
        List<Submission> pending = submissionDAO.findUnanalyzedByUser(userId);
        for (Submission s : pending) {
            engine.analyzeSubmission(s);
        }
        return pending.size();
    }

    public List<Analysis> getUserAnalyses(long userId) {
        return analysisDAO.findByUser(userId);
    }
}
