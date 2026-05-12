package com.cfanalyzer.service;

import com.cfanalyzer.crawler.CodeforcesCrawler;
import com.cfanalyzer.dao.SubmissionDAO;
import com.cfanalyzer.dao.UserDAO;
import com.cfanalyzer.model.Submission;
import com.cfanalyzer.model.User;

import java.sql.SQLException;
import java.util.List;

public class UserService {
    private final UserDAO userDAO = new UserDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final CodeforcesCrawler crawler = new CodeforcesCrawler();

    public long addUser(String handle) throws SQLException {
        return userDAO.addUser(handle);
    }

    public void deleteUser(long userId) throws SQLException {
        userDAO.deleteUser(userId);
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public int crawlUser(User user) {
        List<Submission> list = crawler.crawlSubmissions(user.getHandle(), user.getId());
        for (Submission s : list) {
            submissionDAO.saveIfNotExists(s);
        }
        userDAO.updateLastCrawled(user.getId());
        return list.size();
    }

    public List<Submission> getSubmissions(long userId) {
        return submissionDAO.findByUser(userId);
    }
}
