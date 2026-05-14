package com.cfanalyzer.gui;

import com.cfanalyzer.service.AnalysisService;
import com.cfanalyzer.service.RatingService;
import com.cfanalyzer.service.UserService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame(UserService userService, AnalysisService analysisService, RatingService ratingService) {
        super("Codeforces Analyzer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", new DashboardPanel(userService, ratingService));
        tabs.addTab("User Management", new UserManagementPanel(userService, analysisService, ratingService));
        tabs.addTab("Analysis", new AnalysisPanel(userService, analysisService));
        tabs.addTab("Settings", new SettingsPanel());

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }
}