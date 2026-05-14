package com.cfanalyzer.gui;

import com.cfanalyzer.model.Analysis;
import com.cfanalyzer.model.User;
import com.cfanalyzer.service.AnalysisService;
import com.cfanalyzer.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AnalysisPanel extends JPanel {
    private final UserService userService;
    private final AnalysisService analysisService;
    private final JComboBox<User> userCombo = new JComboBox<>();
    private final DefaultTableModel tableModel;
    private List<Analysis> current;

    public AnalysisPanel(UserService userService, AnalysisService analysisService) {
        this.userService = userService;
        this.analysisService = analysisService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("Submission Analysis");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 51, 102));

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton refreshUsers = new JButton("Refresh Users");
        JButton load = new JButton("Load Analysis");
        JButton detail = new JButton("View Details");
        
        controlPanel.add(new JLabel("Select User:"));
        controlPanel.add(userCombo);
        controlPanel.add(refreshUsers);
        controlPanel.add(load);
        controlPanel.add(detail);

        // Analysis table - showing all required info
        tableModel = new DefaultTableModel(new Object[]{
            "Problem", "Language", "Verdict", 
            "Data Structures", "Algorithms", 
            "AI Detection Score", "AI Confidence", "Date"
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Add components
        add(titleLabel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.AFTER_LINE_ENDS);
        add(scrollPane, BorderLayout.CENTER);

        // Event handlers
        refreshUsers.addActionListener(e -> reloadUsers());
        load.addActionListener(e -> loadAnalyses());
        detail.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || current == null || row >= current.size()) {
                JOptionPane.showMessageDialog(this, "Please select a submission first.");
                return;
            }
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            new UserAnalysisDialog(parent, current.get(row)).setVisible(true);
        });

        reloadUsers();
    }

    private void reloadUsers() {
        userCombo.removeAllItems();
        List<User> allUsers = userService.getAllUsers();
        if (allUsers != null) {
            for (User u : allUsers) {
                userCombo.addItem(u);
            }
        }
    }

    private void loadAnalyses() {
        try {
            User u = (User) userCombo.getSelectedItem();
            if (u == null) {
                JOptionPane.showMessageDialog(
                    this, 
                    "No users available!\n\n" +
                    "Please go to 'User Management' tab and:\n" +
                    "1. Add a Codeforces username\n" +
                    "2. Click 'Crawl Now' to fetch submissions",
                    "No Data",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }
            
            current = analysisService.getUserAnalyses(u.getId());
            
            if (current == null || current.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "No analysis data found for user: " + u.getHandle() + "\n\n" +
                    "Please:\n" +
                    "1. Go to 'User Management' tab\n" +
                    "2. Select this user and click 'Crawl Now'\n" +
                    "3. Wait for submissions to be fetched and analyzed",
                    "No Data",
                    JOptionPane.INFORMATION_MESSAGE
                );
                tableModel.setRowCount(0);
                return;
            }
            
            tableModel.setRowCount(0);
            
            for (Analysis a : current) {
                if (a == null) continue;
                
                String date = a.getSubmittedAt() != null ? a.getSubmittedAt().toString().split("T")[0] : "N/A";
                String dataStructures = (a.getDataStructures() != null && !a.getDataStructures().isEmpty()) 
                    ? String.join(", ", a.getDataStructures()) 
                    : "N/A";
                String algorithms = (a.getAlgorithms() != null && !a.getAlgorithms().isEmpty()) 
                    ? String.join(", ", a.getAlgorithms()) 
                    : "N/A";
                
                tableModel.addRow(new Object[]{
                    a.getProblemName() != null ? a.getProblemName() : "N/A",
                    a.getLanguage() != null ? a.getLanguage() : "N/A",
                    a.getVerdict() != null ? a.getVerdict() : "N/A",
                    dataStructures,
                    algorithms,
                    String.format("%.1f/100", a.getAiDetectionScore()),
                    String.format("%.1f%%", a.getAiConfidence()),
                    date
                });
            }
            
            JOptionPane.showMessageDialog(
                this,
                "Successfully loaded " + current.size() + " submissions for user: " + u.getHandle(),
                "Load Complete",
                JOptionPane.INFORMATION_MESSAGE
            );
            
        } catch (Exception e) {
            System.err.println("Error loading analyses: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                "Error loading analysis data:\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
