package com.cfanalyzer.config;

public final class AppConfig {
    public static final String CF_API_BASE = "https://codeforces.com/api";
    public static final String CF_LOGIN_URL = "https://codeforces.com/enter";
    public static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    public static final int CRAWLER_PAGE_LOAD_WAIT_SECONDS = 10;
    public static final long CRAWLER_MIN_DELAY_MS = 1500;
    public static final long CRAWLER_MAX_DELAY_MS = 3000;

    private AppConfig() {
    }
}
