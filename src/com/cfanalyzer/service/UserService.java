package com.cfanalyzer.service;

import com.cfanalyzer.dao.UserDAO;
import com.cfanalyzer.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic for user management.
 */
public class UserService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * Add a new user by username.
     * @return the created user, or null if username already exists
     */
    public User addUser(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        String trimmed = username.trim();
        if (userDAO.findByUsername(trimmed) != null) {
            throw new IllegalArgumentException("User '" + trimmed + "' already exists.");
        }
        User user = new User(trimmed);
        userDAO.insertUser(user);
        return user;
    }

    /**
     * Delete a user by ID.
     */
    public boolean deleteUser(int userId) throws SQLException {
        return userDAO.deleteUser(userId);
    }

    /**
     * Get all users with their submission counts.
     */
    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAllWithSubmissionCount();
    }

    /**
     * Find a user by username.
     */
    public User findByUsername(String username) throws SQLException {
        return userDAO.findByUsername(username);
    }

    /**
     * Find a user by ID.
     */
    public User findById(int id) throws SQLException {
        return userDAO.findById(id);
    }

    /**
     * Get total user count.
     */
    public int getTotalUserCount() throws SQLException {
        return userDAO.getTotalCount();
    }
}
