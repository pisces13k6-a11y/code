package com.cfanalyzer.service;

import com.cfanalyzer.dao.AnalysisDAO;
import com.cfanalyzer.dao.UserRatingDAO;
import com.cfanalyzer.model.Analysis;
import com.cfanalyzer.model.UserRating;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RatingService {
    private final AnalysisDAO analysisDAO = new AnalysisDAO();
    private final UserRatingDAO userRatingDAO = new UserRatingDAO();

    public UserRating recomputeUserRating(long userId) {
        List<Analysis> analyses = analysisDAO.findByUser(userId);
        Set<String> ds = new HashSet<>();
        Set<String> alg = new HashSet<>();
        int aiCount = 0;
        for (Analysis a : analyses) {
            ds.addAll(a.getDataStructures());
            alg.addAll(a.getAlgorithms());
            if (a.getAiDetectionScore() > 70) aiCount++;
        }

        UserRating r = new UserRating();
        r.setUserId(userId);
        r.setDsScore(Math.min(100, ds.size() * 10));
        r.setAlgoScore(Math.min(100, alg.size() * 10));
        r.setAiUsagePercent(analyses.isEmpty() ? 0 : (100.0 * aiCount / analyses.size()));
        userRatingDAO.upsert(r);
        return r;
    }

    public List<UserRating> getRankings() {
        return userRatingDAO.findAll();
    }
}
