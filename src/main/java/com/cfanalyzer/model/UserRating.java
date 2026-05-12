package com.cfanalyzer.model;

public class UserRating {
    private long userId;
    private double dsScore;
    private double algoScore;
    private double aiUsagePercent;

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public double getDsScore() { return dsScore; }
    public void setDsScore(double dsScore) { this.dsScore = dsScore; }
    public double getAlgoScore() { return algoScore; }
    public void setAlgoScore(double algoScore) { this.algoScore = algoScore; }
    public double getAiUsagePercent() { return aiUsagePercent; }
    public void setAiUsagePercent(double aiUsagePercent) { this.aiUsagePercent = aiUsagePercent; }
}
