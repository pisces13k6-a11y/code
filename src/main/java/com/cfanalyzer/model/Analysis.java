package com.cfanalyzer.model;

import java.util.ArrayList;
import java.util.List;

public class Analysis {
    private long id;
    private long submissionId;
    private List<String> dataStructures = new ArrayList<>();
    private List<String> algorithms = new ArrayList<>();
    private double aiDetectionScore;
    private double aiConfidence;
    private String summary;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getSubmissionId() { return submissionId; }
    public void setSubmissionId(long submissionId) { this.submissionId = submissionId; }
    public List<String> getDataStructures() { return dataStructures; }
    public void setDataStructures(List<String> dataStructures) { this.dataStructures = dataStructures; }
    public List<String> getAlgorithms() { return algorithms; }
    public void setAlgorithms(List<String> algorithms) { this.algorithms = algorithms; }
    public double getAiDetectionScore() { return aiDetectionScore; }
    public void setAiDetectionScore(double aiDetectionScore) { this.aiDetectionScore = aiDetectionScore; }
    public double getAiConfidence() { return aiConfidence; }
    public void setAiConfidence(double aiConfidence) { this.aiConfidence = aiConfidence; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
