package com.cfanalyzer.gui;

import com.cfanalyzer.model.Analysis;
import com.cfanalyzer.model.User;
import com.cfanalyzer.model.UserRating;
import com.cfanalyzer.service.AnalysisService;
import com.cfanalyzer.service.RatingService;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Modal dialog showing detailed analysis for a specific user.
 */
public class UserAnalysisDialog extends JDialog {

    private final User user;
    private final RatingService ratingService = new RatingService();
    private final AnalysisService analysisService = new AnalysisService();

    public UserAnalysisDialog(Frame parent, User user) {
        super(parent, "Analysis: " + user.getUsername(), true);
        this.user = user;
        initComponents();
        loadData();
    }

    private void initComponents() {
        setSize(700, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("📊 " + user.getUsername(), SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        JLabel ratingLabel = new JLabel("Rating: " + user.getRating() + " | Max: " + user.getMaxRating(), SwingConstants.CENTER);
        ratingLabel.setForeground(Color.GRAY);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(ratingLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Content will be set after data loads
        JLabel loading = new JLabel("Loading...", SwingConstants.CENTER);
        add(loading, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadData() {
        SwingWorker<Object[], Void> worker = new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                UserRating rating = ratingService.getUserRating(user.getId());
                List<Analysis> analyses = analysisService.getAnalysesForUser(user.getId());
                return new Object[]{rating, analyses};
            }
            @Override
            protected void done() {
                try {
                    Object[] result = get();
                    UserRating rating = (UserRating) result[0];
                    @SuppressWarnings("unchecked")
                    List<Analysis> analyses = (List<Analysis>) result[1];
                    buildContent(rating, analyses);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(UserAnalysisDialog.this,
                            "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void buildContent(UserRating rating, List<Analysis> analyses) {
        getContentPane().remove(1); // Remove loading label

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        if (rating != null) {
            // Score bars
            content.add(makeSectionTitle("📈 Scores"));
            content.add(makeScoreRow("Data Structure Score", rating.getDataStructureScore(), new Color(70, 130, 180)));
            content.add(makeScoreRow("Algorithm Score", rating.getAlgorithmScore(), new Color(60, 179, 113)));
            double aiPct = rating.getAiUsagePercentage();
            Color aiColor = aiPct >= 70 ? new Color(220, 20, 60) : aiPct >= 40 ? Color.ORANGE : new Color(60, 179, 113);
            content.add(makeScoreRow("AI Usage Percentage", aiPct, aiColor));
            content.add(Box.createVerticalStrut(10));

            // Submission stats
            content.add(makeSectionTitle("📝 Submission Statistics"));
            content.add(makeInfoRow("Total Submissions", String.valueOf(rating.getTotalSubmissions())));
            content.add(makeInfoRow("Accepted Submissions", String.valueOf(rating.getAcceptedSubmissions())));
            if (rating.getTotalSubmissions() > 0) {
                double acceptRate = (double) rating.getAcceptedSubmissions() / rating.getTotalSubmissions() * 100;
                content.add(makeInfoRow("Acceptance Rate", String.format("%.1f%%", acceptRate)));
            }
            content.add(Box.createVerticalStrut(10));
        }

        if (analyses != null && !analyses.isEmpty()) {
            // Top data structures
            Map<String, Long> dsCount = new HashMap<>();
            Map<String, Long> algCount = new HashMap<>();

            for (Analysis a : analyses) {
                if (a.getDataStructures() != null) {
                    for (String ds : a.getDataStructures()) dsCount.merge(ds, 1L, Long::sum);
                }
                if (a.getAlgorithms() != null) {
                    for (String alg : a.getAlgorithms()) algCount.merge(alg, 1L, Long::sum);
                }
            }

            content.add(makeSectionTitle("🏗 Top 5 Data Structures"));
            dsCount.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(e -> content.add(makeInfoRow(e.getKey(), e.getValue() + " submissions")));

            content.add(Box.createVerticalStrut(10));
            content.add(makeSectionTitle("⚙ Top 5 Algorithms"));
            algCount.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(e -> content.add(makeInfoRow(e.getKey(), e.getValue() + " submissions")));
        } else {
            content.add(new JLabel("No analysis data available. Please crawl and analyze submissions first."));
        }

        add(new JScrollPane(content), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JLabel makeSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        return label;
    }

    private JPanel makeScoreRow(String name, double score, Color barColor) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setPreferredSize(new Dimension(200, 25));
        row.add(nameLabel, BorderLayout.WEST);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) Math.round(score));
        bar.setStringPainted(true);
        bar.setString(String.format("%.1f", score));
        bar.setForeground(barColor);
        row.add(bar, BorderLayout.CENTER);

        return row;
    }

    private JPanel makeInfoRow(String key, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        row.add(new JLabel("  • " + key + ":"), BorderLayout.WEST);
        JLabel valLabel = new JLabel(value);
        valLabel.setForeground(new Color(50, 100, 200));
        row.add(valLabel, BorderLayout.CENTER);
        return row;
    }
}
