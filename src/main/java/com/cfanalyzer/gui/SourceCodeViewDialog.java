package com.cfanalyzer.gui;

import com.cfanalyzer.model.Analysis;

import javax.swing.*;
import java.awt.*;

public class SourceCodeViewDialog extends JDialog {
    public SourceCodeViewDialog(JFrame parent, Analysis analysis) {
        super(parent, "💻 Source Code - " + analysis.getProblemName(), true);
        setSize(950, 750);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        headerPanel.setBorder(BorderFactory.createTitledBorder("Code Information"));
        
        int lineCount = analysis.getSourceCode() != null ? analysis.getSourceCode().split("\n").length : 0;
        headerPanel.add(new JLabel("Language: " + analysis.getLanguage()));
        headerPanel.add(new JLabel("Lines: " + lineCount));
        headerPanel.add(new JLabel("Problem: " + analysis.getProblemName()));

        // Code display
        JTextArea codeArea = new JTextArea();
        codeArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        codeArea.setText(analysis.getSourceCode() != null ? analysis.getSourceCode() : "No source code available");
        codeArea.setEditable(false);
        codeArea.setLineWrap(false);
        codeArea.setTabSize(4);
        codeArea.setBackground(new Color(245, 245, 245));

        // Line numbers
        JTextArea lineNumbers = new JTextArea();
        lineNumbers.setFont(new Font("Courier New", Font.PLAIN, 12));
        lineNumbers.setText(generateLineNumbers(analysis.getSourceCode()));
        lineNumbers.setEditable(false);
        lineNumbers.setBackground(new Color(240, 240, 240));
        lineNumbers.setForeground(new Color(100, 100, 100));

        // Scroll pane with line numbers
        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setRowHeaderView(lineNumbers);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(codeScroll, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(closeBtn);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private String generateLineNumbers(String code) {
        if (code == null || code.isEmpty()) return "1";
        String[] lines = code.split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines.length; i++) {
            sb.append(i).append("\n");
        }
        return sb.toString();
    }
}