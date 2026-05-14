package com.cfanalyzer.service;

import com.cfanalyzer.crawler.CodeforcesCrawler;
import com.cfanalyzer.dao.UserDAO;
import com.cfanalyzer.model.User;

import java.util.List;
import java.util.logging.Logger;

public class UserService {
    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    private final UserDAO userDAO = new UserDAO();

    /**
     * Add a new user
     */
    public void addUser(String handle) {
        try {
            User user = new User();
            user.setHandle(handle);
            user.setActive(true);
            userDAO.insert(user);
            logger.info("User added: " + handle);
        } catch (Exception e) {
            logger.severe("Add user failed: " + e.getMessage());
            throw new RuntimeException("Failed to add user: " + handle, e);
        }
    }

    /**
     * Crawl user submissions with limit
     * ✅ UPDATED: Pass userId to crawler
     */
    public int crawlUser(User user) {
        if (user == null || user.getId() <= 0) {
            logger.warning("Invalid user for crawling");
            return 0;
        }
        
        CodeforcesCrawler crawler = new CodeforcesCrawler();
        int limit = 10; // Default limit
        
        try {
            // ✅ Pass userId to crawlUserSubmissions
            int crawledCount = crawler.crawlUserSubmissions(user.getHandle(), limit, user.getId());
            
            // Update last crawled time
            userDAO.updateLastCrawledAt(user.getId());
            
            return crawledCount;
        } catch (Exception e) {
            logger.severe("Crawl failed for user " + user.getHandle() + ": " + e.getMessage());
            return 0;
        } finally {
            crawler.closeDriver();
        }
    }

    /**
     * Delete a user
     */
    public void deleteUser(long userId) {
        try {
            userDAO.delete(userId);
            logger.info("User deleted: ID=" + userId);
        } catch (Exception e) {
            logger.severe("Delete user failed: " + e.getMessage());
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        try {
            return userDAO.findAll();
        } catch (Exception e) {
            logger.warning("Get all users failed: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Get user by ID
     */
    public User getUserById(long userId) {
        try {
            return userDAO.findById(userId);
        } catch (Exception e) {
            logger.warning("Get user by ID failed: " + e.getMessage());
            return null;
        }
    }
}