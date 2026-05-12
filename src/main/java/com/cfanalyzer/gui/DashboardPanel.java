package com.cfanalyzer.gui;

import com.cfanalyzer.model.UserRating;
import com.cfanalyzer.service.RatingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {
    private final RatingService ratingService;
    private final DefaultTableModel tableModel;

    public DashboardPanel(RatingService ratingService) {
        this.ratingService = ratingService;
        setLayout(new BorderLayout());

        JButton refresh = new JButton("Refresh Rankings");
        tableModel = new DefaultTableModel(new Object[]{"User ID", "DS Score", "Algo Score", "AI Usage %"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);

        add(refresh, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh.addActionListener(e -> reload());
        reload();
    }

    public void reload() {
        List<UserRating> ratings = ratingService.getRankings();
        tableModel.setRowCount(0);
        for (UserRating r : ratings) {
            tableModel.addRow(new Object[]{r.getUserId(), r.getDsScore(), r.getAlgoScore(), r.getAiUsagePercent()});
        }
    }
}
