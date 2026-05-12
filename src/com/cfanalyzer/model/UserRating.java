package com.cfanalyzer.model;

import java.sql.Timestamp;

/**
 * Represents computed rating scores for a user.
 */
public class UserRating {

    private int id;
    private int userId;
    private String username;
    private double dataStructureScore;
    private double algorithmScore;
    private double aiUsagePercentage;
    private int totalSubmissions;
    private int acceptedSubmissions;
    private Timestamp updatedAt;

    public UserRating() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public double getDataStructureScore() { return dataStructureScore; }
    public void setDataStructureScore(double dataStructureScore) { this.dataStructureScore = dataStructureScore; }

    public double getAlgorithmScore() { return algorithmScore; }
    public void setAlgorithmScore(double algorithmScore) { this.algorithmScore = algorithmScore; }

    public double getAiUsagePercentage() { return aiUsagePercentage; }
    public void setAiUsagePercentage(double aiUsagePercentage) { this.aiUsagePercentage = aiUsagePercentage; }

    public int getTotalSubmissions() { return totalSubmissions; }
    public void setTotalSubmissions(int totalSubmissions) { this.totalSubmissions = totalSubmissions; }

    public int getAcceptedSubmissions() { return acceptedSubmissions; }
    public void setAcceptedSubmissions(int acceptedSubmissions) { this.acceptedSubmissions = acceptedSubmissions; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
