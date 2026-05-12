package com.cfanalyzer.crawler;

import com.cfanalyzer.dao.ConfigDAO;
import com.cfanalyzer.dao.UserDAO;
import com.cfanalyzer.model.User;
import com.cfanalyzer.service.AnalysisService;
import com.cfanalyzer.service.RatingService;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Schedules periodic crawling of all Codeforces users.
 */
public class CrawlerScheduler {

    private Timer timer;
    private final UserDAO userDAO = new UserDAO();
    private final ConfigDAO configDAO = new ConfigDAO();
    private final CodeforcesCrawler crawler = new CodeforcesCrawler();
    private final AnalysisService analysisService = new AnalysisService();
    private final RatingService ratingService = new RatingService();

    private boolean running = false;

    /**
     * Start the scheduler with the interval configured in the database.
     */
    public void start() {
        if (running) return;
        int hours = configDAO.getCrawlIntervalHours();
        long intervalMs = (long) hours * 60 * 60 * 1000;

        timer = new Timer("CrawlerScheduler", true);
        timer.scheduleAtFixedRate(new CrawlTask(), intervalMs, intervalMs);
        running = true;
        System.out.println("[INFO] " + now() + " CrawlerScheduler started. Interval: " + hours + " hours.");
    }

    /**
     * Stop the scheduler.
     */
    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        running = false;
        System.out.println("[INFO] " + now() + " CrawlerScheduler stopped.");
    }

    /**
     * Trigger an immediate crawl of all users (runs in a background thread).
     */
    public void crawlNow() {
        Thread t = new Thread(new CrawlTask(), "ManualCrawlThread");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Trigger a crawl for a single user (runs in a background thread).
     */
    public void crawlUser(User user, Runnable onComplete) {
        Thread t = new Thread(() -> {
            try {
                System.out.println("[INFO] " + now() + " Manual crawl started for: " + user.getUsername());
                int count = crawler.crawlUser(user);
                System.out.println("[INFO] " + now() + " Crawled " + count + " submissions for " + user.getUsername());
                if (count > 0) {
                    analysisService.analyzeUser(user.getId());
                    ratingService.updateUserRating(user.getId());
                }
            } catch (Exception e) {
                System.err.println("[ERROR] " + now() + " Error during manual crawl for " + user.getUsername() + ": " + e.getMessage());
            } finally {
                if (onComplete != null) onComplete.run();
            }
        }, "ManualCrawl-" + user.getUsername());
        t.setDaemon(true);
        t.start();
    }

    public boolean isRunning() {
        return running;
    }

    private class CrawlTask extends TimerTask {
        @Override
        public void run() {
            System.out.println("[INFO] " + now() + " === Scheduled crawl session started ===");
            try {
                List<User> users = userDAO.findAll();
                System.out.println("[INFO] " + now() + " Crawling " + users.size() + " users...");

                for (User user : users) {
                    try {
                        int count = crawler.crawlUser(user);
                        if (count > 0) {
                            analysisService.analyzeUser(user.getId());
                            ratingService.updateUserRating(user.getId());
                        }
                    } catch (Exception e) {
                        System.err.println("[ERROR] " + now() + " Error crawling user " + user.getUsername() + ": " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("[ERROR] " + now() + " Crawl session failed: " + e.getMessage());
            }
            System.out.println("[INFO] " + now() + " === Scheduled crawl session finished ===");
        }
    }

    private String now() {
        return new java.util.Date().toString();
    }
}
