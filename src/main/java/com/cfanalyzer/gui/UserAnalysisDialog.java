package com.cfanalyzer.gui;

import com.cfanalyzer.model.Analysis;

import javax.swing.*;
import java.awt.*;

public class UserAnalysisDialog extends JDialog {
    public UserAnalysisDialog(JFrame parent, Analysis analysis) {
        super(parent, "Analysis Detail", true);
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setText(
                "Data Structures: " + analysis.getDataStructures() + "\n" +
                "Algorithms: " + analysis.getAlgorithms() + "\n" +
                "AI Detection Score: " + analysis.getAiDetectionScore() + "\n" +
                "AI Confidence: " + analysis.getAiConfidence() + "\n\n" +
                "Summary:\n" + analysis.getSummary()
        );
        setLayout(new BorderLayout());
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        setSize(520, 360);
        setLocationRelativeTo(parent);
    }
}
