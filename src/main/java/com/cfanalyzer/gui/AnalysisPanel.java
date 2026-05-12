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
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshUsers = new JButton("Refresh Users");
        JButton load = new JButton("Load Analysis");
        JButton detail = new JButton("View Detail");
        top.add(new JLabel("User:"));
        top.add(userCombo);
        top.add(refreshUsers);
        top.add(load);
        top.add(detail);

        tableModel = new DefaultTableModel(new Object[]{"Submission ID", "DS Count", "Algo Count", "AI Score"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshUsers.addActionListener(e -> reloadUsers());
        load.addActionListener(e -> loadAnalyses());
        detail.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || current == null || row >= current.size()) return;
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            new UserAnalysisDialog(parent, current.get(row)).setVisible(true);
        });

        reloadUsers();
    }

    private void reloadUsers() {
        userCombo.removeAllItems();
        for (User u : userService.getAllUsers()) userCombo.addItem(u);
    }

    private void loadAnalyses() {
        User u = (User) userCombo.getSelectedItem();
        if (u == null) return;
        current = analysisService.getUserAnalyses(u.getId());
        tableModel.setRowCount(0);
        for (Analysis a : current) {
            tableModel.addRow(new Object[]{a.getSubmissionId(), a.getDataStructures().size(), a.getAlgorithms().size(), a.getAiDetectionScore()});
        }
    }
}
