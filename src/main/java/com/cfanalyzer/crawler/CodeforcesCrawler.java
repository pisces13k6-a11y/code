package com.cfanalyzer.crawler;

import com.cfanalyzer.config.AppConfig;
import com.cfanalyzer.dao.SubmissionDAO;
import com.cfanalyzer.model.Submission;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.logging.Logger;

public class CodeforcesCrawler {
    private static final Logger logger = Logger.getLogger(CodeforcesCrawler.class.getName());
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final OkHttpClient httpClient = new OkHttpClient();

    /**
     * ✅ NEW: Crawl user submissions via Codeforces HTTP API (JSON)
     * NOT using Selenium anymore
     */
    public int crawlUserSubmissions(String handle, int limit, long userId) {
        if (userId <= 0) {
            logger.warning("Invalid userId: " + userId);
            return 0;
        }
        
        if (handle == null || handle.isEmpty()) {
            logger.warning("Invalid handle: " + handle);
            return 0;
        }
        
        try {
            logger.info("=== CRAWL START ===");
            logger.info("Crawling user: " + handle + " (ID: " + userId + ", limit: " + limit + ")");
            
            // Build API URL
            String url = AppConfig.CF_API_BASE + "/user.status?handle=" + handle + "&from=1&count=" + limit;
            logger.info("API URL: " + url);
            
            // Make HTTP GET request
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Chrome/5.0")
                    .build();
            
            Response response = httpClient.newCall(request).execute();
            
            if (!response.isSuccessful()) {
                logger.warning("❌ API request failed: HTTP " + response.code());
                return 0;
            }
            
            String responseBody = response.body().string();
            logger.info("✅ Response received: " + responseBody.length() + " bytes");
            
            // Parse JSON response
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            
            // Check status
            if (!jsonResponse.get("status").getAsString().equals("OK")) {
                String comment = jsonResponse.get("comment").getAsString();
                logger.warning("❌ API error: " + comment);
                return 0;
            }
            
            // Get submissions array
            JsonArray results = jsonResponse.getAsJsonArray("result");
            logger.info("📊 Found " + results.size() + " total submissions");
            
            int crawledCount = 0;
            int inserted = 0;
            
            // Process each submission
            for (int i = 0; i < results.size() && crawledCount < limit; i++) {
                try {
                    JsonObject submissionJson = results.get(i).getAsJsonObject();
                    
                    // Extract fields
                    long cfSubmissionId = submissionJson.get("id").getAsLong();
                    int contestId = submissionJson.get("contestId").getAsInt();
                    String problemId = submissionJson.getAsJsonObject("problem").get("index").getAsString();
                    String problemName = submissionJson.getAsJsonObject("problem").get("name").getAsString();
                    String language = submissionJson.get("programmingLanguage").getAsString();
                    String verdict = submissionJson.has("verdict") ? submissionJson.get("verdict").getAsString() : "TESTING";
                    long creationTimeSeconds = submissionJson.get("creationTimeSeconds").getAsLong();
                    
                    // Create Submission object
                    Submission submission = new Submission();
                    submission.setUserId(userId);
                    submission.setUserHandle(handle);
                    submission.setCfSubmissionId(cfSubmissionId);
                    submission.setContestId(contestId);
                    submission.setProblemId(problemId);
                    submission.setProblemName(problemName);
                    submission.setLanguage(language);
                    submission.setVerdict(verdict);
                    submission.setSourceCode("");  // API không cung cấp source code
                    submission.setTags("competitive-programming");
                    submission.setSubmittedAt(
                        LocalDateTime.ofEpochSecond(creationTimeSeconds, 0, ZoneOffset.UTC)
                    );
                    submission.setCrawledAt(LocalDateTime.now());
                    
                    // Insert to database
                    submissionDAO.insert(submission);
                    inserted++;
                    crawledCount++;
                    
                    logger.info("✅ [" + crawledCount + "] " + problemName + 
                               " (" + language + ", " + verdict + ")");
                    
                } catch (Exception e) {
                    logger.warning("⚠️  Error parsing submission " + (i+1) + ": " + e.getMessage());
                }
            }
            
            logger.info("=== CRAWL COMPLETE ===");
            logger.info("Crawled: " + crawledCount + " submissions");
            logger.info("Inserted: " + inserted + " submissions");
            
            return crawledCount;
            
        } catch (IOException e) {
            logger.severe("❌ Network error: " + e.getMessage());
            return 0;
        } catch (Exception e) {
            logger.severe("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Close resources (OkHttpClient auto-closes)
     */
    public void closeDriver() {
        logger.info("Crawler resources closed");
    }
}