package com.cfanalyzer.gui;

import com.cfanalyzer.config.AppConfig;
import com.cfanalyzer.config.DatabaseConfig;
import com.cfanalyzer.crawler.CrawlerScheduler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main application window with tabbed interface.
 */
public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;
    private DashboardPanel dashboardPanel;
    private UserManagementPanel userManagementPanel;
    private AnalysisPanel analysisPanel;
    private SettingsPanel settingsPanel;
    private JLabel statusLabel;
    private final CrawlerScheduler scheduler = new CrawlerScheduler();

    public MainFrame() {
        super(AppConfig.APP_NAME + " v" + AppConfig.APP_VERSION);
        initComponents();
        startScheduler();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(AppConfig.WINDOW_WIDTH, AppConfig.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Menu bar
        setJMenuBar(buildMenuBar());

        // Tabs
        tabbedPane = new JTabbedPane();
        dashboardPanel = new DashboardPanel();
        userManagementPanel = new UserManagementPanel(this);
        analysisPanel = new AnalysisPanel();
        settingsPanel = new SettingsPanel();

        tabbedPane.addTab("📊 Dashboard", dashboardPanel);
        tabbedPane.addTab("👥 User Management", userManagementPanel);
        tabbedPane.addTab("🔍 Analysis", analysisPanel);
        tabbedPane.addTab("⚙ Settings", settingsPanel);

        tabbedPane.addChangeListener(e -> onTabChanged());

        add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusLabel = new JLabel("  Connecting to database...");
        statusLabel.setFont(statusLabel.getFont().deriveFont(11f));
        statusBar.add(statusLabel, BorderLayout.WEST);
        add(statusBar, BorderLayout.SOUTH);

        // Window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });

        // Update status bar
        SwingUtilities.invokeLater(this::updateConnectionStatus);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem refreshItem = new JMenuItem("Refresh All");
        refreshItem.addActionListener(e -> refreshAll());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> shutdown());
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private void startScheduler() {
        try {
            scheduler.start();
        } catch (Exception e) {
            System.err.println("[WARN] Could not start scheduler: " + e.getMessage());
        }
    }

    private void onTabChanged() {
        int idx = tabbedPane.getSelectedIndex();
        if (idx == 0) dashboardPanel.refresh();
        else if (idx == 1) userManagementPanel.refresh();
        else if (idx == 2) analysisPanel.refresh();
    }

    private void updateConnectionStatus() {
        if (DatabaseConfig.testConnection()) {
            statusLabel.setText("  ✅ Database connected: " + DatabaseConfig.getHost() + ":" + DatabaseConfig.getPort() + "/" + DatabaseConfig.getDatabase());
            statusLabel.setForeground(new Color(0, 128, 0));
        } else {
            statusLabel.setText("  ❌ Database not connected. Please configure in Settings.");
            statusLabel.setForeground(Color.RED);
        }
    }

    public void refreshAll() {
        dashboardPanel.refresh();
        userManagementPanel.refresh();
        analysisPanel.refresh();
        updateConnectionStatus();
    }

    public void refreshUserList() {
        userManagementPanel.refresh();
        analysisPanel.refresh();
        dashboardPanel.refresh();
    }

    public CrawlerScheduler getScheduler() {
        return scheduler;
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                AppConfig.APP_NAME + " v" + AppConfig.APP_VERSION + "\n\n" +
                "A Java Swing application to analyze Codeforces submissions\n" +
                "using AI (Groq API) to detect data structures, algorithms,\n" +
                "and AI-generated code.\n\n" +
                "Technologies: Java Swing, MySQL, Selenium, Groq AI",
                "About " + AppConfig.APP_NAME,
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void shutdown() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            scheduler.stop();
            DatabaseConfig.closeConnection();
            dispose();
            System.exit(0);
        }
    }
}
