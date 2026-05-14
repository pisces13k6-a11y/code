package com.cfanalyzer.gui;

import com.cfanalyzer.model.Analysis;

import javax.swing.*;
import java.awt.*;

public class UserAnalysisDialog extends JDialog {
    public UserAnalysisDialog(JFrame parent, Analysis analysis) {
        super(parent, "Analysis Details - " + analysis.getProblemName(), true);
        setSize(800, 700);
        setLocationRelativeTo(parent);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Basic Information
        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Basic Information"));
        
        addInfoRow(infoPanel, "Problem:", analysis.getProblemName());
        addInfoRow(infoPanel, "Language:", analysis.getLanguage());
        addInfoRow(infoPanel, "Verdict:", analysis.getVerdict());
        addInfoRow(infoPanel, "Submitted:", analysis.getSubmittedAt() != null ? analysis.getSubmittedAt().toString() : "N/A");

        // Analysis Results
        JPanel analysisPanel = new JPanel(new BorderLayout(10, 10));
        analysisPanel.setBorder(BorderFactory.createTitledBorder("Code Analysis Results"));
        
        JTextArea analysisText = new JTextArea();
        analysisText.setEditable(false);
        analysisText.setLineWrap(true);
        analysisText.setWrapStyleWord(true);
        analysisText.setFont(new Font("Monospaced", Font.PLAIN, 11));
        analysisText.setBackground(new Color(245, 245, 245));
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== DATA STRUCTURES DETECTED ===\n");
        if (analysis.getDataStructures().isEmpty()) {
            sb.append("None detected\n");
        } else {
            for (String ds : analysis.getDataStructures()) {
                sb.append("  • ").append(ds).append("\n");
            }
        }
        
        sb.append("\n=== ALGORITHMS DETECTED ===\n");
        if (analysis.getAlgorithms().isEmpty()) {
            sb.append("None detected\n");
        } else {
            for (String algo : analysis.getAlgorithms()) {
                sb.append("  • ").append(algo).append("\n");
            }
        }
        
        sb.append("\n=== AI DETECTION ANALYSIS ===\n");
        sb.append("AI Detection Score: ").append(String.format("%.1f", analysis.getAiDetectionScore())).append("/100\n");
        sb.append("AI Confidence: ").append(String.format("%.1f", analysis.getAiConfidence())).append("%\n");
        sb.append("Assessment: ");
        if (analysis.getAiDetectionScore() > 70) {
            sb.append("LIKELY AI-GENERATED CODE\n");
        } else if (analysis.getAiDetectionScore() > 40) {
            sb.append("POSSIBLY AI-ASSISTED\n");
        } else {
            sb.append("LIKELY HUMAN-WRITTEN CODE\n");
        }
        
        sb.append("\n=== ANALYSIS SUMMARY ===\n");
        sb.append(analysis.getSummary() != null && !analysis.getSummary().isEmpty() 
            ? analysis.getSummary() 
            : "No summary available");
        
        analysisText.setText(sb.toString());
        analysisPanel.add(new JScrollPane(analysisText), BorderLayout.CENTER);

        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(analysisPanel, BorderLayout.CENTER);

        // Close button
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(closeBtn);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.BOLD, 12));
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Arial", Font.PLAIN, 12));
        valueComponent.setForeground(new Color(0, 102, 204));
        panel.add(labelComponent);
        panel.add(valueComponent);
    }
}