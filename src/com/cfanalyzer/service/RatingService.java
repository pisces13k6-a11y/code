package com.cfanalyzer.service;

import com.cfanalyzer.config.AppConfig;
import com.cfanalyzer.dao.AnalysisDAO;
import com.cfanalyzer.dao.SubmissionDAO;
import com.cfanalyzer.dao.UserRatingDAO;
import com.cfanalyzer.model.Analysis;
import com.cfanalyzer.model.Submission;
import com.cfanalyzer.model.UserRating;

import java.sql.SQLException;
import java.util.*;

/**
 * Business logic for computing and updating user ratings.
 */
public class RatingService {

    private final AnalysisDAO analysisDAO = new AnalysisDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final UserRatingDAO userRatingDAO = new UserRatingDAO();

    /**
     * Compute and persist the rating for a user.
     */
    public UserRating updateUserRating(int userId) throws SQLException {
        List<Analysis> analyses = analysisDAO.findByUserId(userId);
        List<Submission> submissions = submissionDAO.findByUserId(userId);

        int total = submissions.size();
        int accepted = (int) submissions.stream()
                .filter(s -> "Accepted".equalsIgnoreCase(s.getVerdict()))
                .count();

        double dsScore = calculateDataStructureScore(analyses);
        double algScore = calculateAlgorithmScore(analyses);
        double aiPct = calculateAIUsagePercentage(analyses);

        UserRating rating = new UserRating();
        rating.setUserId(userId);
        rating.setDataStructureScore(dsScore);
        rating.setAlgorithmScore(algScore);
        rating.setAiUsagePercentage(aiPct);
        rating.setTotalSubmissions(total);
        rating.setAcceptedSubmissions(accepted);

        userRatingDAO.upsert(rating);
        return rating;
    }

    /**
     * Calculate data structure diversity score (0-100).
     * Bonus for complex data structures.
     */
    public double calculateDataStructureScore(List<Analysis> analyses) {
        Set<String> uniqueDS = new HashSet<>();
        for (Analysis a : analyses) {
            if (a.getDataStructures() != null) uniqueDS.addAll(a.getDataStructures());
        }

        if (uniqueDS.isEmpty()) return 0.0;

        double score = 0;
        Set<String> simpleSet = new HashSet<>(Arrays.asList(AppConfig.SIMPLE_DATA_STRUCTURES));
        Set<String> complexSet = new HashSet<>(Arrays.asList(AppConfig.COMPLEX_DATA_STRUCTURES));

        for (String ds : uniqueDS) {
            if (complexSet.contains(ds)) score += 8.0;
            else if (simpleSet.contains(ds)) score += 3.0;
            else score += 2.0; // unknown DS still counts
        }

        // Diversity bonus: more unique = higher score
        score += uniqueDS.size() * 2.0;
        return Math.min(100.0, score);
    }

    /**
     * Calculate algorithm diversity score (0-100).
     * Bonus for complex algorithms.
     */
    public double calculateAlgorithmScore(List<Analysis> analyses) {
        Set<String> uniqueAlg = new HashSet<>();
        for (Analysis a : analyses) {
            if (a.getAlgorithms() != null) uniqueAlg.addAll(a.getAlgorithms());
        }

        if (uniqueAlg.isEmpty()) return 0.0;

        double score = 0;
        Set<String> simpleSet = new HashSet<>(Arrays.asList(AppConfig.SIMPLE_ALGORITHMS));
        Set<String> complexSet = new HashSet<>(Arrays.asList(AppConfig.COMPLEX_ALGORITHMS));

        for (String alg : uniqueAlg) {
            if (complexSet.contains(alg)) score += 8.0;
            else if (simpleSet.contains(alg)) score += 3.0;
            else score += 2.0;
        }

        score += uniqueAlg.size() * 2.0;
        return Math.min(100.0, score);
    }

    /**
     * Calculate the percentage of submissions that likely used AI.
     * AI threshold is AppConfig.AI_DETECTION_THRESHOLD.
     */
    public double calculateAIUsagePercentage(List<Analysis> analyses) {
        if (analyses.isEmpty()) return 0.0;
        long aiCount = analyses.stream()
                .filter(a -> a.getAiDetectionScore() >= AppConfig.AI_DETECTION_THRESHOLD)
                .count();
        // Compute percentage rounded to 2 decimal places
        double percentage = ((double) aiCount / analyses.size()) * 100.0;
        return Math.round(percentage * 100.0) / 100.0;
    }

    /**
     * Get user rating from DB.
     */
    public UserRating getUserRating(int userId) throws SQLException {
        return userRatingDAO.findByUserId(userId);
    }

    /**
     * Get all user ratings.
     */
    public List<UserRating> getAllRatings() throws SQLException {
        return userRatingDAO.findAll();
    }
}
