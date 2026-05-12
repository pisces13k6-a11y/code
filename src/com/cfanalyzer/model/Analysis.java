package com.cfanalyzer.model;

import java.sql.Timestamp;
import java.util.List;

/**
 * Represents AI analysis results for a submission.
 */
public class Analysis {

    private int id;
    private long submissionId;
    private List<String> dataStructures;
    private List<String> algorithms;
    private double aiDetectionScore;
    private List<String> aiIndicators;
    private String complexityAnalysis;
    private double codeQualityScore;
    private Timestamp analyzedAt;

    public Analysis() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public long getSubmissionId() { return submissionId; }
    public void setSubmissionId(long submissionId) { this.submissionId = submissionId; }

    public List<String> getDataStructures() { return dataStructures; }
    public void setDataStructures(List<String> dataStructures) { this.dataStructures = dataStructures; }

    public List<String> getAlgorithms() { return algorithms; }
    public void setAlgorithms(List<String> algorithms) { this.algorithms = algorithms; }

    public double getAiDetectionScore() { return aiDetectionScore; }
    public void setAiDetectionScore(double aiDetectionScore) { this.aiDetectionScore = aiDetectionScore; }

    public List<String> getAiIndicators() { return aiIndicators; }
    public void setAiIndicators(List<String> aiIndicators) { this.aiIndicators = aiIndicators; }

    public String getComplexityAnalysis() { return complexityAnalysis; }
    public void setComplexityAnalysis(String complexityAnalysis) { this.complexityAnalysis = complexityAnalysis; }

    public double getCodeQualityScore() { return codeQualityScore; }
    public void setCodeQualityScore(double codeQualityScore) { this.codeQualityScore = codeQualityScore; }

    public Timestamp getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(Timestamp analyzedAt) { this.analyzedAt = analyzedAt; }
}
