package com.cfanalyzer.gui;

import com.cfanalyzer.model.User;
import com.cfanalyzer.model.UserRating;
import com.cfanalyzer.service.RatingService;
import com.cfanalyzer.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final UserService userService;
    private final RatingService ratingService;
    private final DefaultTableModel tableModel;
    private final JPanel statsPanel;

    public DashboardPanel(UserService userService, RatingService ratingService) {
        this.userService = userService;
        this.ratingService = ratingService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("User Rankings & Evaluation System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 51, 102));

        JButton refresh = new JButton("Refresh");
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(refresh, BorderLayout.EAST);

        // Statistics panel
        this.statsPanel = createStatsPanel();

        // Rankings table
        tableModel = new DefaultTableModel(
            new Object[]{"Rank", "Username", "DS Score", "Algorithm Score", "AI Usage %", "Status"}, 
            0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(180);

        add(titlePanel, BorderLayout.NORTH);
        add(statsPanel, BorderLayout.AFTER_LINE_ENDS);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh.addActionListener(e -> reload());
        reload();
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Statistics Overview"));
        panel.setBackground(new Color(245, 250, 255));

        JLabel totalUsersLabel = new JLabel("Total Users: 0", SwingConstants.CENTER);
        JLabel avgDsLabel = new JLabel("Avg DS Score: 0.0", SwingConstants.CENTER);
        JLabel avgAlgoLabel = new JLabel("Avg Algorithm Score: 0.0", SwingConstants.CENTER);
        JLabel avgAiLabel = new JLabel("Avg AI Usage: 0.0%", SwingConstants.CENTER);

        Font font = new Font("Arial", Font.BOLD, 12);
        totalUsersLabel.setFont(font);
        avgDsLabel.setFont(font);
        avgAlgoLabel.setFont(font);
        avgAiLabel.setFont(font);

        totalUsersLabel.setForeground(new Color(0, 102, 204));
        avgDsLabel.setForeground(new Color(0, 153, 76));
        avgAlgoLabel.setForeground(new Color(204, 51, 0));
        avgAiLabel.setForeground(new Color(153, 0, 153));

        panel.add(totalUsersLabel);
        panel.add(avgDsLabel);
        panel.add(avgAlgoLabel);
        panel.add(avgAiLabel);

        return panel;
    }

    public void reload() {
        try {
            List<UserRating> ratings = ratingService.getRankings();
            
            if (ratings == null) {
                ratings = List.of();
            }
            
            Map<Long, User> userMap = new HashMap<>();
            List<User> allUsers = userService.getAllUsers();
            if (allUsers != null) {
                for (User u : allUsers) {
                    userMap.put(u.getId(), u);
                }
            }

            // Calculate statistics
            double totalDsScore = 0, totalAlgoScore = 0, totalAiUsage = 0;
            for (UserRating r : ratings) {
                totalDsScore += r.getDsScore();
                totalAlgoScore += r.getAlgoScore();
                totalAiUsage += r.getAiUsagePercent();
            }
            
            int size = ratings.size();
            double avgDsScore = size > 0 ? totalDsScore / size : 0;
            double avgAlgoScore = size > 0 ? totalAlgoScore / size : 0;
            double avgAiUsage = size > 0 ? totalAiUsage / size : 0;

            // Update stats panel
            Component[] components = statsPanel.getComponents();
            if (components.length >= 4) {
                ((JLabel) components[0]).setText(String.format("Total Users: %d", size));
                ((JLabel) components[1]).setText(String.format("Avg DS Score: %.1f", avgDsScore));
                ((JLabel) components[2]).setText(String.format("Avg Algorithm Score: %.1f", avgAlgoScore));
                ((JLabel) components[3]).setText(String.format("Avg AI Usage: %.1f%%", avgAiUsage));
            }

            // Update rankings table
            tableModel.setRowCount(0);
            int rank = 1;
            for (UserRating r : ratings) {
                User user = userMap.getOrDefault(r.getUserId(), new User());
                if (user.getHandle() == null) user.setHandle("User #" + r.getUserId());
                
                String status = getEvaluationStatus(r.getDsScore(), r.getAlgoScore(), r.getAiUsagePercent());
                
                tableModel.addRow(new Object[]{
                    rank++,
                    user.getHandle(),
                    String.format("%.1f/100", r.getDsScore()),
                    String.format("%.1f/100", r.getAlgoScore()),
                    String.format("%.1f%%", r.getAiUsagePercent()),
                    status
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getEvaluationStatus(double ds, double algo, double ai) {
        StringBuilder status = new StringBuilder();
        
        // Data Structure evaluation
        if (ds >= 80) status.append("[DS: Excellent] ");
        else if (ds >= 60) status.append("[DS: Good] ");
        else status.append("[DS: Needs Work] ");
        
        // Algorithm evaluation
        if (algo >= 80) status.append("[Algo: Excellent] ");
        else if (algo >= 60) status.append("[Algo: Good] ");
        else status.append("[Algo: Needs Work] ");
        
        // AI usage warning
        if (ai > 70) status.append("[HIGH AI USE]");
        else if (ai > 40) status.append("[MEDIUM AI USE]");
        
        return status.toString();
    }
}
