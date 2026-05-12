package com.cfanalyzer.crawler;

import com.cfanalyzer.dao.ConfigDAO;
import com.cfanalyzer.model.User;
import com.cfanalyzer.service.AnalysisService;
import com.cfanalyzer.service.RatingService;
import com.cfanalyzer.service.UserService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CrawlerScheduler {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final UserService userService;
    private final AnalysisService analysisService;
    private final RatingService ratingService;
    private final ConfigDAO configDAO = new ConfigDAO();

    public CrawlerScheduler(UserService userService, AnalysisService analysisService, RatingService ratingService) {
        this.userService = userService;
        this.analysisService = analysisService;
        this.ratingService = ratingService;
    }

    public void start() {
        int interval = Integer.parseInt(configDAO.getValue("crawl_interval_hours", "24"));
        scheduler.scheduleAtFixedRate(this::crawlAll, 1, interval, TimeUnit.HOURS);
    }

    public void crawlAll() {
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            userService.crawlUser(user);
            analysisService.analyzeUserSubmissions(user.getId());
            ratingService.recomputeUserRating(user.getId());
        }
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
