package com.cfanalyzer.gui;

import com.cfanalyzer.config.DatabaseConfig;
import com.cfanalyzer.dao.ConfigDAO;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for configuring database connection, Groq API key, and crawl interval.
 */
public class SettingsPanel extends JPanel {

    private final ConfigDAO configDAO = new ConfigDAO();

    // Database settings
    private JTextField dbHostField, dbPortField, dbNameField, dbUserField;
    private JPasswordField dbPassField;

    // Groq settings
    private JPasswordField groqApiKeyField;

    // Crawl settings
    private JSpinner crawlIntervalSpinner;

    public SettingsPanel() {
        initComponents();
        loadSettings();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        // --- Database Section ---
        formPanel.add(makeSectionTitle("🗄 Database Connection"));

        JPanel dbPanel = new JPanel(new GridBagLayout());
        dbPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        GridBagConstraints gbc = makeGbc();

        dbHostField = new JTextField(20);
        dbPortField = new JTextField(6);
        dbNameField = new JTextField(20);
        dbUserField = new JTextField(20);
        dbPassField = new JPasswordField(20);

        addFormRow(dbPanel, gbc, 0, "Host:", dbHostField);
        addFormRow(dbPanel, gbc, 1, "Port:", dbPortField);
        addFormRow(dbPanel, gbc, 2, "Database:", dbNameField);
        addFormRow(dbPanel, gbc, 3, "Username:", dbUserField);
        addFormRow(dbPanel, gbc, 4, "Password:", dbPassField);

        JButton testConnButton = new JButton("🔌 Test Connection");
        testConnButton.addActionListener(e -> testConnection());
        gbc.gridx = 1; gbc.gridy = 5;
        dbPanel.add(testConnButton, gbc);

        formPanel.add(dbPanel);
        formPanel.add(Box.createVerticalStrut(15));

        // --- Groq API Section ---
        formPanel.add(makeSectionTitle("🤖 Groq AI Settings"));

        JPanel groqPanel = new JPanel(new GridBagLayout());
        groqPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        GridBagConstraints gbc2 = makeGbc();

        groqApiKeyField = new JPasswordField(40);
        addFormRow(groqPanel, gbc2, 0, "Groq API Key:", groqApiKeyField);

        JButton showKeyBtn = new JButton("👁 Show/Hide");
        showKeyBtn.addActionListener(e -> togglePasswordVisibility(groqApiKeyField));
        gbc2.gridx = 2; gbc2.gridy = 0;
        groqPanel.add(showKeyBtn, gbc2);

        JLabel groqHint = new JLabel("<html><small>Get your API key from <a href='https://console.groq.com'>console.groq.com</a></small></html>");
        gbc2.gridx = 1; gbc2.gridy = 1; gbc2.gridwidth = 2;
        groqPanel.add(groqHint, gbc2);

        formPanel.add(groqPanel);
        formPanel.add(Box.createVerticalStrut(15));

        // --- Crawl Settings ---
        formPanel.add(makeSectionTitle("⏰ Crawl Settings"));

        JPanel crawlPanel = new JPanel(new GridBagLayout());
        crawlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        GridBagConstraints gbc3 = makeGbc();

        crawlIntervalSpinner = new JSpinner(new SpinnerNumberModel(24, 1, 168, 1));
        addFormRow(crawlPanel, gbc3, 0, "Crawl Interval (hours):", crawlIntervalSpinner);

        formPanel.add(crawlPanel);
        formPanel.add(Box.createVerticalStrut(20));

        add(new JScrollPane(formPanel), BorderLayout.CENTER);

        // Save button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("💾 Save Settings");
        saveButton.setFont(saveButton.getFont().deriveFont(Font.BOLD));
        saveButton.addActionListener(e -> saveSettings());
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JLabel makeSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private GridBagConstraints makeGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        return gbc;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private void loadSettings() {
        dbHostField.setText(DatabaseConfig.getHost());
        dbPortField.setText(String.valueOf(DatabaseConfig.getPort()));
        dbNameField.setText(DatabaseConfig.getDatabase());
        dbUserField.setText(DatabaseConfig.getUsername());
        dbPassField.setText(DatabaseConfig.getPassword());

        try {
            String apiKey = configDAO.getGroqApiKey();
            groqApiKeyField.setText(apiKey != null ? apiKey : "");
            int interval = configDAO.getCrawlIntervalHours();
            crawlIntervalSpinner.setValue(interval);
        } catch (Exception e) {
            System.err.println("[WARN] Could not load settings from DB: " + e.getMessage());
        }
    }

    private void saveSettings() {
        // Validate port
        int port;
        try {
            port = Integer.parseInt(dbPortField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Port must be a number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update DatabaseConfig
        DatabaseConfig.setHost(dbHostField.getText().trim());
        DatabaseConfig.setPort(port);
        DatabaseConfig.setDatabase(dbNameField.getText().trim());
        DatabaseConfig.setUsername(dbUserField.getText().trim());
        DatabaseConfig.setPassword(new String(dbPassField.getPassword()));
        DatabaseConfig.closeConnection();

        // Save to database (if connection works)
        try {
            String apiKey = new String(groqApiKeyField.getPassword()).trim();
            int interval = (int) crawlIntervalSpinner.getValue();
            configDAO.setValue("groq_api_key", apiKey);
            configDAO.setValue("crawl_interval_hours", String.valueOf(interval));
            JOptionPane.showMessageDialog(this,
                    "Settings saved successfully!", "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Database settings applied locally, but could not save to DB:\n" + e.getMessage(),
                    "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void testConnection() {
        // Apply current field values temporarily
        String origHost = DatabaseConfig.getHost();
        int origPort = DatabaseConfig.getPort();
        String origDb = DatabaseConfig.getDatabase();
        String origUser = DatabaseConfig.getUsername();
        String origPass = DatabaseConfig.getPassword();

        try {
            int port = Integer.parseInt(dbPortField.getText().trim());
            DatabaseConfig.setHost(dbHostField.getText().trim());
            DatabaseConfig.setPort(port);
            DatabaseConfig.setDatabase(dbNameField.getText().trim());
            DatabaseConfig.setUsername(dbUserField.getText().trim());
            DatabaseConfig.setPassword(new String(dbPassField.getPassword()));
            DatabaseConfig.closeConnection();

            if (DatabaseConfig.testConnection()) {
                JOptionPane.showMessageDialog(this, "✅ Connection successful!", "Connection Test", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "❌ Connection failed. Check your settings.", "Connection Test", JOptionPane.ERROR_MESSAGE);
                // Restore original settings
                DatabaseConfig.setHost(origHost);
                DatabaseConfig.setPort(origPort);
                DatabaseConfig.setDatabase(origDb);
                DatabaseConfig.setUsername(origUser);
                DatabaseConfig.setPassword(origPass);
                DatabaseConfig.closeConnection();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid port number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void togglePasswordVisibility(JPasswordField field) {
        if (field.getEchoChar() == 0) {
            field.setEchoChar('•');
        } else {
            field.setEchoChar((char) 0);
        }
    }
}
