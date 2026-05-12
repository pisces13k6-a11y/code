package com.cfanalyzer.model;

import java.sql.Timestamp;

/**
 * Represents a Codeforces user in the system.
 */
public class User {

    private int id;
    private String username;
    private int rating;
    private int maxRating;
    private Timestamp createdAt;
    private Timestamp lastCrawled;
    // Transient field for display
    private int submissionCount;

    public User() {}

    public User(String username) {
        this.username = username;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public int getMaxRating() { return maxRating; }
    public void setMaxRating(int maxRating) { this.maxRating = maxRating; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getLastCrawled() { return lastCrawled; }
    public void setLastCrawled(Timestamp lastCrawled) { this.lastCrawled = lastCrawled; }

    public int getSubmissionCount() { return submissionCount; }
    public void setSubmissionCount(int submissionCount) { this.submissionCount = submissionCount; }

    @Override
    public String toString() {
        return username;
    }
}
