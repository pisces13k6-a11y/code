package com.cfanalyzer.model;

import java.time.LocalDateTime;

public class Submission {
    private long id;
    private long userId;
    private long cfSubmissionId;
    private int contestId;
    private String problemId;
    private String problemName;
    private String language;
    private String verdict;
    private LocalDateTime submittedAt;
    private String sourceCode;
    private String tags;
    private LocalDateTime crawledAt;
    private String userHandle;

    // ALL GETTERS AND SETTERS
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    
    public long getCfSubmissionId() { return cfSubmissionId; }
    public void setCfSubmissionId(long cfSubmissionId) { this.cfSubmissionId = cfSubmissionId; }
    
    public int getContestId() { return contestId; }
    public void setContestId(int contestId) { this.contestId = contestId; }
    
    public String getProblemId() { return problemId; }
    public void setProblemId(String problemId) { this.problemId = problemId; }
    
    public String getProblemName() { return problemName; }
    public void setProblemName(String problemName) { this.problemName = problemName; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }
    
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    
    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    
    public LocalDateTime getCrawledAt() { return crawledAt; }
    public void setCrawledAt(LocalDateTime crawledAt) { this.crawledAt = crawledAt; }
    
    public String getUserHandle() { return userHandle; }
    public void setUserHandle(String userHandle) { this.userHandle = userHandle; }
}