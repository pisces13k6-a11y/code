package com.cfanalyzer.gui;

import com.cfanalyzer.config.DatabaseConfig;
import com.cfanalyzer.dao.ConfigDAO;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private final ConfigDAO configDAO = new ConfigDAO();

    public SettingsPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField host = new JTextField(DatabaseConfig.getHost(), 18);
        JTextField port = new JTextField(String.valueOf(DatabaseConfig.getPort()), 18);
        JTextField database = new JTextField(DatabaseConfig.getDatabase(), 18);
        JTextField user = new JTextField(DatabaseConfig.getUsername(), 18);
        JPasswordField pass = new JPasswordField(DatabaseConfig.getPassword(), 18);
        JTextField groqKey = new JTextField(configDAO.getValue("groq_api_key", ""), 24);
        JTextField cfUsername = new JTextField(configDAO.getValue("cf_username", ""), 24);
        JPasswordField cfPassword = new JPasswordField(configDAO.getValue("cf_password", ""), 24);
        JTextField interval = new JTextField(configDAO.getValue("crawl_interval_hours", "24"), 6);

        int y = 0;
        addField("DB Host", host, gbc, y++);
        addField("DB Port", port, gbc, y++);
        addField("DB Name", database, gbc, y++);
        addField("DB User", user, gbc, y++);
        addField("DB Password", pass, gbc, y++);
        addField("Groq API Key", groqKey, gbc, y++);
        addField("Codeforces Username", cfUsername, gbc, y++);
        addField("Codeforces Password (để crawl source code)", cfPassword, gbc, y++);
        addField("Crawl Interval (hours)", interval, gbc, y++);

        JButton save = new JButton("Save Settings");
        gbc.gridx = 1;
        gbc.gridy = y;
        add(save, gbc);

        save.addActionListener(e -> {
            try {
                DatabaseConfig.setHost(host.getText().trim());
                DatabaseConfig.setPort(Integer.parseInt(port.getText().trim()));
                DatabaseConfig.setDatabase(database.getText().trim());
                DatabaseConfig.setUsername(user.getText().trim());
                DatabaseConfig.setPassword(new String(pass.getPassword()));
                configDAO.putValue("groq_api_key", groqKey.getText().trim());
                configDAO.putValue("cf_username", cfUsername.getText().trim());
                configDAO.putValue("cf_password", new String(cfPassword.getPassword()));
                configDAO.putValue("crawl_interval_hours", interval.getText().trim());
                JOptionPane.showMessageDialog(this, "Saved. Restart app to apply scheduler interval changes.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
            }
        });
    }

    private void addField(String label, JComponent field, GridBagConstraints gbc, int y) {
        gbc.gridx = 0;
        gbc.gridy = y;
        add(new JLabel(label), gbc);
        gbc.gridx = 1;
        add(field, gbc);
    }
}
