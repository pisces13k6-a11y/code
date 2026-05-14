package com.cfanalyzer.gui;

import com.cfanalyzer.model.User;
import com.cfanalyzer.service.AnalysisService;
import com.cfanalyzer.service.RatingService;
import com.cfanalyzer.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel {
    private final UserService userService;
    private final AnalysisService analysisService;
    private final RatingService ratingService;
    private final DefaultTableModel tableModel;

    public UserManagementPanel(UserService userService, AnalysisService analysisService, RatingService ratingService) {
        this.userService = userService;
        this.analysisService = analysisService;
        this.ratingService = ratingService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("User Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 51, 102));

        // Input panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JTextField handleInput = new JTextField(20);
        JButton addBtn = new JButton("Add User");
        JButton crawlBtn = new JButton("Crawl Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        
        inputPanel.add(new JLabel("Codeforces Handle:"));
        inputPanel.add(handleInput);
        inputPanel.add(addBtn);
        inputPanel.add(crawlBtn);
        inputPanel.add(deleteBtn);

        // User table
        tableModel = new DefaultTableModel(new Object[]{"ID", "Handle", "Active", "Last Crawled", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);

        // Add components
        add(titleLabel, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.BEFORE_FIRST_LINE);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Event handlers
        addBtn.addActionListener(e -> {
            String handle = handleInput.getText().trim();
            if (handle.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a handle");
                return;
            }
            try {
                userService.addUser(handle);
                handleInput.setText("");
                refresh();
                JOptionPane.showMessageDialog(this, "User added successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Add user failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        crawlBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a user first");
                return;
            }
            long userId = ((Number) tableModel.getValueAt(row, 0)).longValue();
            String handle = String.valueOf(tableModel.getValueAt(row, 1));
            User user = new User();
            user.setId(userId);
            user.setHandle(handle);
            
            try {
                int crawled = userService.crawlUser(user);
                int analyzed = analysisService.analyzeUserSubmissions(userId);
                ratingService.recomputeUserRating(userId);
                refresh();
                JOptionPane.showMessageDialog(this, "Crawled: " + crawled + " submissions\nAnalyzed: " + analyzed);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Crawl failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a user first");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                long userId = ((Number) tableModel.getValueAt(row, 0)).longValue();
                try {
                    userService.deleteUser(userId);
                    refresh();
                    JOptionPane.showMessageDialog(this, "User deleted successfully");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        refresh();
    }

    public void refresh() {
        List<User> users = userService.getAllUsers();
        tableModel.setRowCount(0);
        for (User user : users) {
            String status = user.isActive() ? "Active" : "Inactive";
            tableModel.addRow(new Object[]{
                user.getId(),
                user.getHandle(),
                user.isActive() ? "Yes" : "No",
                user.getLastCrawledAt() != null ? user.getLastCrawledAt().toString().split("T")[0] : "Never",
                status
            });
        }
    }
}