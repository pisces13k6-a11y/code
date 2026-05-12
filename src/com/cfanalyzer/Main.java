package com.cfanalyzer;

import com.cfanalyzer.config.AppConfig;
import com.cfanalyzer.config.DatabaseConfig;
import com.cfanalyzer.gui.MainFrame;

import javax.swing.*;

/**
 * Application entry point for Codeforces Analyzer.
 */
public class Main {

    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("[WARN] Could not set system look and feel: " + e.getMessage());
        }

        System.out.println("[INFO] Starting " + AppConfig.APP_NAME + " v" + AppConfig.APP_VERSION);

        // Test database connection on startup
        if (!DatabaseConfig.testConnection()) {
            SwingUtilities.invokeLater(() -> {
                int choice = JOptionPane.showConfirmDialog(
                        null,
                        "Could not connect to the database.\n" +
                        "Host: " + DatabaseConfig.getHost() + ":" + DatabaseConfig.getPort() + "\n" +
                        "Database: " + DatabaseConfig.getDatabase() + "\n\n" +
                        "Please configure your database settings in the Settings tab.\n\n" +
                        "Continue anyway?",
                        "Database Connection Failed",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (choice == JOptionPane.YES_OPTION) {
                    launchGui();
                } else {
                    System.exit(0);
                }
            });
        } else {
            System.out.println("[INFO] Database connected successfully.");
            SwingUtilities.invokeLater(Main::launchGui);
        }
    }

    private static void launchGui() {
        MainFrame frame = new MainFrame();
        frame.setVisible(true);
        System.out.println("[INFO] GUI launched.");
    }
}
