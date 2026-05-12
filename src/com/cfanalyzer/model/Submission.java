package com.cfanalyzer.model;

import java.sql.Timestamp;

/**
 * Represents a Codeforces submission.
 */
public class Submission {

    private long id;
    private int userId;
    private String problemName;
    private int problemRating;
    private String language;
    private String verdict;
    private int timeConsumed;
    private int memoryConsumed;
    private Timestamp submissionDate;
    private String code;
    private boolean analyzed;

    public Submission() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getProblemName() { return problemName; }
    public void setProblemName(String problemName) { this.problemName = problemName; }

    public int getProblemRating() { return problemRating; }
    public void setProblemRating(int problemRating) { this.problemRating = problemRating; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }

    public int getTimeConsumed() { return timeConsumed; }
    public void setTimeConsumed(int timeConsumed) { this.timeConsumed = timeConsumed; }

    public int getMemoryConsumed() { return memoryConsumed; }
    public void setMemoryConsumed(int memoryConsumed) { this.memoryConsumed = memoryConsumed; }

    public Timestamp getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(Timestamp submissionDate) { this.submissionDate = submissionDate; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public boolean isAnalyzed() { return analyzed; }
    public void setAnalyzed(boolean analyzed) { this.analyzed = analyzed; }
}
