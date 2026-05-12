package com.cfanalyzer.gui;

import com.cfanalyzer.dao.SubmissionDAO;
import com.cfanalyzer.model.Submission;
import com.cfanalyzer.model.UserRating;
import com.cfanalyzer.service.AnalysisService;
import com.cfanalyzer.service.RatingService;
import com.cfanalyzer.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dashboard panel showing overall statistics and top users.
 */
public class DashboardPanel extends JPanel {

    private final UserService userService = new UserService();
    private final AnalysisService analysisService = new AnalysisService();
    private final RatingService ratingService = new RatingService();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();

    private JLabel totalUsersLabel, totalSubmissionsLabel, avgDsLabel, avgAlgLabel, avgAiLabel;
    private JTable recentTable, rankingTable;
    private DefaultTableModel recentModel, rankingModel;

    public DashboardPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Stats cards at top
        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        statsPanel.setBorder(BorderFactory.createTitledBorder("📈 Overall Statistics"));

        totalUsersLabel = makeStatCard("Total Users", "0");
        totalSubmissionsLabel = makeStatCard("Analyzed Submissions", "0");
        avgDsLabel = makeStatCard("Avg DS Score", "0.00");
        avgAlgLabel = makeStatCard("Avg Algorithm Score", "0.00");
        avgAiLabel = makeStatCard("Avg AI Usage %", "0.00%");

        statsPanel.add(wrapCard("👥 Users", totalUsersLabel));
        statsPanel.add(wrapCard("📝 Analyzed", totalSubmissionsLabel));
        statsPanel.add(wrapCard("🏗 DS Score", avgDsLabel));
        statsPanel.add(wrapCard("⚙ Alg Score", avgAlgLabel));
        statsPanel.add(wrapCard("🤖 AI Usage", avgAiLabel));

        add(statsPanel, BorderLayout.NORTH);

        // Center: split between recent activity and ranking table
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.5);

        // Recent submissions
        recentModel = new DefaultTableModel(new String[]{"User", "Problem", "Language", "Verdict", "Date"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        recentTable = new JTable(recentModel);
        recentTable.setRowHeight(22);
        JScrollPane recentScroll = new JScrollPane(recentTable);
        recentScroll.setBorder(BorderFactory.createTitledBorder("🕒 Recent Submissions (last 10)"));
        split.setLeftComponent(recentScroll);

        // User rankings
        rankingModel = new DefaultTableModel(
                new String[]{"#", "Username", "DS Score", "Alg Score", "AI Usage%", "Total Subs"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        rankingTable = new JTable(rankingModel);
        rankingTable.setRowHeight(22);
        JScrollPane rankingScroll = new JScrollPane(rankingTable);
        rankingScroll.setBorder(BorderFactory.createTitledBorder("🏆 User Rankings"));
        split.setRightComponent(rankingScroll);

        add(split, BorderLayout.CENTER);

        // Refresh button
        JButton refreshBtn = new JButton("🔄 Refresh Dashboard");
        refreshBtn.addActionListener(e -> refresh());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(refreshBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JLabel makeStatCard(String title, String value) {
        JLabel label = new JLabel(value, SwingConstants.CENTER);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        label.setForeground(new Color(30, 100, 200));
        return label;
    }

    private JPanel wrapCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        card.setBackground(new Color(245, 250, 255));
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void refresh() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            int totalUsers, analyzedSubs;
            double avgDs, avgAlg, avgAi;
            List<Submission> recent;
            List<UserRating> rankings;

            @Override
            protected Void doInBackground() throws Exception {
                try { totalUsers = userService.getTotalUserCount(); } catch (Exception ignored) {}
                try { analyzedSubs = analysisService.getTotalAnalyzedCount(); } catch (Exception ignored) {}
                try { avgDs = analysisService.getAverageDataStructureScore(); } catch (Exception ignored) {}
                try { avgAlg = analysisService.getAverageAlgorithmScore(); } catch (Exception ignored) {}
                try { avgAi = analysisService.getAverageAiUsagePercentage(); } catch (Exception ignored) {}
                try { recent = submissionDAO.findRecent(10); } catch (Exception ignored) {}
                try { rankings = ratingService.getAllRatings(); } catch (Exception ignored) {}
                return null;
            }

            @Override
            protected void done() {
                totalUsersLabel.setText(String.valueOf(totalUsers));
                totalSubmissionsLabel.setText(String.valueOf(analyzedSubs));
                avgDsLabel.setText(String.format("%.2f", avgDs));
                avgAlgLabel.setText(String.format("%.2f", avgAlg));
                avgAiLabel.setText(String.format("%.2f%%", avgAi));

                recentModel.setRowCount(0);
                if (recent != null) {
                    for (Submission s : recent) {
                        recentModel.addRow(new Object[]{
                                s.getUserId(), s.getProblemName(), s.getLanguage(),
                                s.getVerdict(), s.getSubmissionDate()
                        });
                    }
                }

                rankingModel.setRowCount(0);
                if (rankings != null) {
                    int rank = 1;
                    for (UserRating r : rankings) {
                        rankingModel.addRow(new Object[]{
                                rank++, r.getUsername(),
                                String.format("%.2f", r.getDataStructureScore()),
                                String.format("%.2f", r.getAlgorithmScore()),
                                String.format("%.2f%%", r.getAiUsagePercentage()),
                                r.getTotalSubmissions()
                        });
                    }
                }
            }
        };
        worker.execute();
    }
}
