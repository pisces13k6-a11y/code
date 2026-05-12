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
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField handleInput = new JTextField(20);
        JButton addBtn = new JButton("Add User");
        JButton crawlBtn = new JButton("Crawl Now");
        JButton deleteBtn = new JButton("Delete User");
        top.add(new JLabel("Codeforces Handle:"));
        top.add(handleInput);
        top.add(addBtn);
        top.add(crawlBtn);
        top.add(deleteBtn);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Handle", "Active", "Last Crawled"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        addBtn.addActionListener(e -> {
            String handle = handleInput.getText().trim();
            if (handle.isEmpty()) return;
            try {
                userService.addUser(handle);
                handleInput.setText("");
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Add user failed: " + ex.getMessage());
            }
        });

        crawlBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            long userId = ((Number) tableModel.getValueAt(row, 0)).longValue();
            String handle = String.valueOf(tableModel.getValueAt(row, 1));
            User user = new User();
            user.setId(userId);
            user.setHandle(handle);
            int crawled = userService.crawlUser(user);
            int analyzed = analysisService.analyzeUserSubmissions(userId);
            ratingService.recomputeUserRating(userId);
            refresh();
            JOptionPane.showMessageDialog(this, "Crawled: " + crawled + " submissions, analyzed: " + analyzed);
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            long userId = ((Number) tableModel.getValueAt(row, 0)).longValue();
            try {
                userService.deleteUser(userId);
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage());
            }
        });

        refresh();
    }

    public void refresh() {
        List<User> users = userService.getAllUsers();
        tableModel.setRowCount(0);
        for (User user : users) {
            tableModel.addRow(new Object[]{user.getId(), user.getHandle(), user.isActive(), user.getLastCrawledAt()});
        }
    }
}
