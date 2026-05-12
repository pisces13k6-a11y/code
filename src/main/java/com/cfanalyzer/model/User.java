package com.cfanalyzer.model;

import java.time.LocalDateTime;

public class User {
    private long id;
    private String handle;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastCrawledAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getHandle() { return handle; }
    public void setHandle(String handle) { this.handle = handle; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastCrawledAt() { return lastCrawledAt; }
    public void setLastCrawledAt(LocalDateTime lastCrawledAt) { this.lastCrawledAt = lastCrawledAt; }

    @Override
    public String toString() {
        return handle;
    }
}
