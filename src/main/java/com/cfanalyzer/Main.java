package com.cfanalyzer;

import com.cfanalyzer.crawler.CrawlerScheduler;
import com.cfanalyzer.gui.MainFrame;
import com.cfanalyzer.service.AnalysisService;
import com.cfanalyzer.service.RatingService;
import com.cfanalyzer.service.UserService;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        Logger.getLogger("org.openqa.selenium.devtools").setLevel(Level.OFF);
        Logger.getLogger("org.openqa.selenium.chromium").setLevel(Level.OFF);

        UserService userService = new UserService();
        AnalysisService analysisService = new AnalysisService();
        RatingService ratingService = new RatingService();

        CrawlerScheduler scheduler = new CrawlerScheduler(userService, analysisService, ratingService);
        scheduler.start();

        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdown));

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(userService, analysisService, ratingService);
            frame.setVisible(true);
        });
    }
}