package com.cfanalyzer.crawler;

import com.cfanalyzer.service.AnalysisService;
import com.cfanalyzer.service.RatingService;
import com.cfanalyzer.service.UserService;
import com.cfanalyzer.model.User;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class CrawlerScheduler {
    private static final Logger logger = Logger.getLogger(CrawlerScheduler.class.getName());
    private final UserService userService;
    private final AnalysisService analysisService;
    private final RatingService ratingService;
    private Timer timer;
    
    // CONFIGURATION - Điều chỉnh các giá trị này để giới hạn crawl
    private static final int MAX_SUBMISSIONS_PER_USER = 10;      // Giới hạn: crawl tối đa 10 submissions per user
    private static final int CRAWL_INTERVAL_HOURS = 24;          // Crawl sau 24 giờ
    private static final int INITIAL_DELAY_MINUTES = 2;          // Delay 2 phút trước khi bắt đầu
    private static final int MAX_CONCURRENT_CRAWLS = 1;          // Chỉ crawl 1 user cùng 1 lúc

    public CrawlerScheduler(UserService userService, AnalysisService analysisService, RatingService ratingService) {
        this.userService = userService;
        this.analysisService = analysisService;
        this.ratingService = ratingService;
    }

    /**
     * Start the crawler scheduler
     */
    public void start() {
        timer = new Timer("CrawlerScheduler", true);
        
        // Delay ban đầu trước khi crawl
        long initialDelayMs = INITIAL_DELAY_MINUTES * 60 * 1000;
        long intervalMs = CRAWL_INTERVAL_HOURS * 60 * 60 * 1000L;
        
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                performScheduledCrawl();
            }
        }, initialDelayMs, intervalMs);
        
        logger.info("Crawler scheduled: crawl every " + CRAWL_INTERVAL_HOURS + " hours, " +
                   "max " + MAX_SUBMISSIONS_PER_USER + " submissions per user");
    }

    /**
     * Perform scheduled crawl for all active users
     */
    private void performScheduledCrawl() {
        logger.info("Starting scheduled crawl...");
        List<User> users = userService.getAllUsers();
        
        for (User user : users) {
            if (!user.isActive()) {
                logger.info("Skipping inactive user: " + user.getHandle());
                continue;
            }
            
            try {
                logger.info("Crawling user: " + user.getHandle() + 
                           " (max " + MAX_SUBMISSIONS_PER_USER + " submissions)");
                
                // Crawl limited submissions
                int crawledCount = userService.crawlUser(user);
                
                // Analyze submissions
                int analyzedCount = analysisService.analyzeUserSubmissions(user.getId());
                
                // Update rating
                ratingService.recomputeUserRating(user.getId());
                
                logger.info("Completed: crawled=" + crawledCount + ", analyzed=" + analyzedCount + 
                           ", user=" + user.getHandle());
                
            } catch (Exception e) {
                logger.warning("Error crawling user " + user.getHandle() + ": " + e.getMessage());
            }
        }
        
        logger.info("Scheduled crawl completed");
    }

    /**
     * Shutdown the scheduler
     */
    public void shutdown() {
        if (timer != null) {
            timer.cancel();
            logger.info("Crawler scheduler stopped");
        }
    }
}