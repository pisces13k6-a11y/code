package com.cfanalyzer.gui;

import com.cfanalyzer.model.Analysis;
import com.cfanalyzer.model.Submission;
import com.cfanalyzer.model.User;
import com.cfanalyzer.service.AnalysisService;
import com.cfanalyzer.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel for viewing submission analysis results per user.
 */
public class AnalysisPanel extends JPanel {

    private final UserService userService = new UserService();
    private final AnalysisService analysisService = new AnalysisService();

    private JComboBox<User> userComboBox;
    private JTable submissionTable;
    private DefaultTableModel tableModel;
    private JTextArea codeArea;
    private JLabel dsLabel, algLabel, aiScoreLabel, complexityLabel, qualityLabel;
    private JTextArea aiIndicatorsArea;

    private static final String[] COLS = {"ID", "Problem", "Language", "Verdict", "Date", "DS Detected", "Algorithms", "AI Score"};

    public AnalysisPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: user selector
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select User:"));
        userComboBox = new JComboBox<>();
        userComboBox.setPreferredSize(new Dimension(200, 25));
        userComboBox.addActionListener(e -> loadSubmissions());
        topPanel.add(userComboBox);

        JButton refreshUsersBtn = new JButton("🔄 Refresh");
        refreshUsersBtn.addActionListener(e -> refresh());
        topPanel.add(refreshUsersBtn);

        add(topPanel, BorderLayout.NORTH);

        // Center: split between table and detail view
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplit.setResizeWeight(0.45);

        // Submission table
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        submissionTable = new JTable(tableModel);
        submissionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        submissionTable.setRowHeight(22);
        submissionTable.getTableHeader().setReorderingAllowed(false);
        submissionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showSubmissionDetail();
        });

        // Column widths
        int[] widths = {80, 180, 100, 100, 140, 160, 160, 70};
        for (int i = 0; i < widths.length && i < submissionTable.getColumnCount(); i++) {
            submissionTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        mainSplit.setTopComponent(new JScrollPane(submissionTable));

        // Detail panel
        JPanel detailPanel = new JPanel(new BorderLayout(5, 5));
        detailPanel.setBorder(BorderFactory.createTitledBorder("Submission Detail"));

        JSplitPane detailSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        detailSplit.setResizeWeight(0.5);

        // Source code area
        codeArea = new JTextArea();
        codeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        codeArea.setEditable(false);
        codeArea.setBackground(new Color(30, 30, 30));
        codeArea.setForeground(new Color(220, 220, 220));
        codeArea.setCaretColor(Color.WHITE);
        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setBorder(BorderFactory.createTitledBorder("Source Code"));
        detailSplit.setLeftComponent(codeScroll);

        // Analysis details panel
        JPanel analysisInfo = new JPanel();
        analysisInfo.setLayout(new BoxLayout(analysisInfo, BoxLayout.Y_AXIS));
        analysisInfo.setBorder(BorderFactory.createTitledBorder("Analysis Results"));

        dsLabel = makeLabel("Data Structures: -");
        algLabel = makeLabel("Algorithms: -");
        aiScoreLabel = makeLabel("AI Detection Score: -");
        complexityLabel = makeLabel("Complexity: -");
        qualityLabel = makeLabel("Code Quality Score: -");

        analysisInfo.add(dsLabel);
        analysisInfo.add(Box.createVerticalStrut(5));
        analysisInfo.add(algLabel);
        analysisInfo.add(Box.createVerticalStrut(5));
        analysisInfo.add(aiScoreLabel);
        analysisInfo.add(Box.createVerticalStrut(5));
        analysisInfo.add(complexityLabel);
        analysisInfo.add(Box.createVerticalStrut(5));
        analysisInfo.add(qualityLabel);
        analysisInfo.add(Box.createVerticalStrut(10));
        analysisInfo.add(new JLabel("AI Indicators:"));
        aiIndicatorsArea = new JTextArea(4, 20);
        aiIndicatorsArea.setEditable(false);
        aiIndicatorsArea.setLineWrap(true);
        aiIndicatorsArea.setWrapStyleWord(true);
        aiIndicatorsArea.setBackground(new Color(248, 248, 248));
        analysisInfo.add(new JScrollPane(aiIndicatorsArea));

        detailSplit.setRightComponent(new JScrollPane(analysisInfo));
        detailPanel.add(detailSplit, BorderLayout.CENTER);
        mainSplit.setBottomComponent(detailPanel);

        add(mainSplit, BorderLayout.CENTER);
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void loadSubmissions() {
        User selected = (User) userComboBox.getSelectedItem();
        if (selected == null) return;

        SwingWorker<List<Submission>, Void> worker = new SwingWorker<List<Submission>, Void>() {
            @Override
            protected List<Submission> doInBackground() throws Exception {
                return analysisService.getSubmissionsForUser(selected.getId());
            }
            @Override
            protected void done() {
                try {
                    List<Submission> subs = get();
                    tableModel.setRowCount(0);
                    for (Submission s : subs) {
                        try {
                            Analysis a = analysisService.getAnalysisForSubmission(s.getId());
                            String ds = "-";
                            String alg = "-";
                            String aiScore = "-";
                            if (a != null) {
                                ds = (a.getDataStructures() != null && !a.getDataStructures().isEmpty())
                                        ? String.join(", ", a.getDataStructures()) : "None";
                                alg = (a.getAlgorithms() != null && !a.getAlgorithms().isEmpty())
                                        ? String.join(", ", a.getAlgorithms()) : "None";
                                aiScore = String.format("%.0f%%", a.getAiDetectionScore());
                            }
                            tableModel.addRow(new Object[]{
                                    s.getId(), s.getProblemName(), s.getLanguage(), s.getVerdict(),
                                    s.getSubmissionDate(), ds, alg, aiScore
                            });
                        } catch (Exception ignored) {
                            tableModel.addRow(new Object[]{
                                    s.getId(), s.getProblemName(), s.getLanguage(), s.getVerdict(),
                                    s.getSubmissionDate(), "-", "-", "-"
                            });
                        }
                    }
                    clearDetail();
                } catch (Exception e) {
                    System.err.println("[WARN] Error loading submissions: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showSubmissionDetail() {
        int row = submissionTable.getSelectedRow();
        if (row < 0) return;

        long subId = (long) tableModel.getValueAt(row, 0);

        SwingWorker<Object[], Void> worker = new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                Submission sub = null;
                Analysis a = null;
                try {
                    // Use the analysis service to get both
                    User selected = (User) userComboBox.getSelectedItem();
                    if (selected != null) {
                        List<Submission> subs = analysisService.getSubmissionsForUser(selected.getId());
                        sub = subs.stream().filter(s -> s.getId() == subId).findFirst().orElse(null);
                    }
                    a = analysisService.getAnalysisForSubmission(subId);
                } catch (Exception e) {
                    System.err.println("[WARN] Error loading submission detail: " + e.getMessage());
                }
                return new Object[]{sub, a};
            }
            @Override
            protected void done() {
                try {
                    Object[] result = get();
                    Submission sub = (Submission) result[0];
                    Analysis a = (Analysis) result[1];

                    if (sub != null) {
                        codeArea.setText(sub.getCode() != null ? sub.getCode() : "(No source code available)");
                        codeArea.setCaretPosition(0);
                    }

                    if (a != null) {
                        String ds = (a.getDataStructures() != null && !a.getDataStructures().isEmpty())
                                ? String.join(", ", a.getDataStructures()) : "None detected";
                        String alg = (a.getAlgorithms() != null && !a.getAlgorithms().isEmpty())
                                ? String.join(", ", a.getAlgorithms()) : "None detected";
                        dsLabel.setText("<html><b>Data Structures:</b> " + ds + "</html>");
                        algLabel.setText("<html><b>Algorithms:</b> " + alg + "</html>");

                        double aiScore = a.getAiDetectionScore();
                        String aiColor = aiScore >= 70 ? "red" : aiScore >= 40 ? "orange" : "green";
                        aiScoreLabel.setText("<html><b>AI Detection Score:</b> <font color='" + aiColor + "'>" + String.format("%.1f", aiScore) + "/100</font></html>");

                        complexityLabel.setText("<html><b>Complexity:</b> " + a.getComplexityAnalysis() + "</html>");
                        qualityLabel.setText("<html><b>Code Quality:</b> " + String.format("%.1f", a.getCodeQualityScore()) + "/100</html>");

                        if (a.getAiIndicators() != null && !a.getAiIndicators().isEmpty()) {
                            aiIndicatorsArea.setText(String.join("\n• ", a.getAiIndicators()));
                        } else {
                            aiIndicatorsArea.setText("No AI indicators detected.");
                        }
                    } else {
                        clearDetail();
                        dsLabel.setText("Data Structures: (not yet analyzed)");
                    }
                } catch (Exception e) {
                    System.err.println("[WARN] Error displaying submission detail: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void clearDetail() {
        codeArea.setText("");
        dsLabel.setText("Data Structures: -");
        algLabel.setText("Algorithms: -");
        aiScoreLabel.setText("AI Detection Score: -");
        complexityLabel.setText("Complexity: -");
        qualityLabel.setText("Code Quality Score: -");
        aiIndicatorsArea.setText("");
    }

    public void refresh() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return userService.getAllUsers();
            }
            @Override
            protected void done() {
                try {
                    List<User> users = get();
                    userComboBox.removeAllItems();
                    for (User u : users) userComboBox.addItem(u);
                } catch (Exception e) {
                    System.err.println("[WARN] Error refreshing analysis panel: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}
