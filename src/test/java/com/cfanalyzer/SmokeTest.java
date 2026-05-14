package com.cfanalyzer;

import com.cfanalyzer.config.AppConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SmokeTest {
    @Test
    void appCompiles() {
        assertEquals("https://codeforces.com/enter", AppConfig.CF_LOGIN_URL);
        assertEquals(10, AppConfig.CRAWLER_PAGE_LOAD_WAIT_SECONDS);
        assertEquals(1500L, AppConfig.CRAWLER_MIN_DELAY_MS);
        assertEquals(3000L, AppConfig.CRAWLER_MAX_DELAY_MS);
    }
}
