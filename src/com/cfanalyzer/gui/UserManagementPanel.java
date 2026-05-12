package com.cfanalyzer.gui;

import com.cfanalyzer.model.User;
import com.cfanalyzer.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.util.List;

/**
 * Panel for managing Codeforces users.
 */
public class UserManagementPanel extends JPanel {

    private final MainFrame mainFrame;
    private final UserService userService = new UserService();

    private JTextField usernameField;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton addButton, crawlButton, deleteButton, refreshButton, viewButton;

    private static final String[] COLUMNS = {"ID", "Username", "Rating", "Max Rating", "Last Crawled", "Submissions"};

    public UserManagementPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
        refresh();
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top input panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Username:"));
        usernameField = new JTextField(20);
        usernameField.addActionListener(e -> addUser());
        topPanel.add(usernameField);

        addButton = new JButton("➕ Add User");
        addButton.addActionListener(e -> addUser());
        topPanel.add(addButton);

        crawlButton = new JButton("🔄 Crawl Now");
        crawlButton.addActionListener(e -> crawlSelectedUser());
        topPanel.add(crawlButton);

        add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(24);
        userTable.getTableHeader().setReorderingAllowed(false);

        // Column widths
        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(160);
        userTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) viewUserAnalysis();
            }
        });

        add(new JScrollPane(userTable), BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        deleteButton = new JButton("🗑 Delete User");
        deleteButton.addActionListener(e -> deleteUser());
        bottomPanel.add(deleteButton);

        refreshButton = new JButton("🔃 Refresh");
        refreshButton.addActionListener(e -> refresh());
        bottomPanel.add(refreshButton);

        viewButton = new JButton("📋 View Analysis");
        viewButton.addActionListener(e -> viewUserAnalysis());
        bottomPanel.add(viewButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addUser() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        addButton.setEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                userService.addUser(username);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    usernameField.setText("");
                    refresh();
                    JOptionPane.showMessageDialog(UserManagementPanel.this,
                            "User '" + username + "' added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(UserManagementPanel.this,
                            "Failed to add user: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    addButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void deleteUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete user '" + username + "' and all their submissions/analyses?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                userService.deleteUser(id);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    refresh();
                    mainFrame.refreshUserList();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(UserManagementPanel.this,
                            "Failed to delete user: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void crawlSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to crawl.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JDialog progressDialog = new JDialog(mainFrame, "Crawling " + username, true);
        progressDialog.add(new JLabel("  Crawling submissions for " + username + "...  "), BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.setSize(350, 100);
        progressDialog.setLocationRelativeTo(this);

        crawlButton.setEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                User user = userService.findById(id);
                if (user == null) throw new Exception("User not found.");
                mainFrame.getScheduler().crawlUser(user, () -> SwingUtilities.invokeLater(progressDialog::dispose));
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    progressDialog.setVisible(true);
                } catch (Exception e) {
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(UserManagementPanel.this,
                            "Crawl failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    crawlButton.setEnabled(true);
                    refresh();
                    mainFrame.refreshUserList();
                }
            }
        };
        worker.execute();
    }

    private void viewUserAnalysis() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to view.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        try {
            User user = userService.findById(id);
            if (user != null) {
                UserAnalysisDialog dialog = new UserAnalysisDialog(mainFrame, user);
                dialog.setVisible(true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading user: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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
                    tableModel.setRowCount(0);
                    for (User u : users) {
                        String lastCrawled = u.getLastCrawled() != null ? u.getLastCrawled().toString() : "Never";
                        tableModel.addRow(new Object[]{
                                u.getId(), u.getUsername(), u.getRating(), u.getMaxRating(),
                                lastCrawled, u.getSubmissionCount()
                        });
                    }
                } catch (Exception e) {
                    System.err.println("[WARN] Failed to refresh user list: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}
