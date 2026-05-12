package com.cfanalyzer.dao;

import com.cfanalyzer.model.UserRating;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserRatingDAO {
    private static final Logger LOGGER = Logger.getLogger(UserRatingDAO.class.getName());

    public void upsert(UserRating r) {
        String sql = "INSERT INTO user_ratings(user_id, ds_score, algo_score, ai_usage_percent) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE ds_score=VALUES(ds_score), algo_score=VALUES(algo_score), ai_usage_percent=VALUES(ai_usage_percent)";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, r.getUserId());
            ps.setDouble(2, r.getDsScore());
            ps.setDouble(3, r.getAlgoScore());
            ps.setDouble(4, r.getAiUsagePercent());
            ps.executeUpdate();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to upsert user rating for user: " + r.getUserId(), ex);
        }
    }

    public List<UserRating> findAll() {
        List<UserRating> list = new ArrayList<>();
        String sql = "SELECT user_id, ds_score, algo_score, ai_usage_percent FROM user_ratings ORDER BY (ds_score + algo_score) DESC";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UserRating r = new UserRating();
                r.setUserId(rs.getLong("user_id"));
                r.setDsScore(rs.getDouble("ds_score"));
                r.setAlgoScore(rs.getDouble("algo_score"));
                r.setAiUsagePercent(rs.getDouble("ai_usage_percent"));
                list.add(r);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to fetch user rankings", ex);
        }
        return list;
    }
}
