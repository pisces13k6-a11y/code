package com.cfanalyzer.analyzer;

import java.util.ArrayList;
import java.util.List;

public class GroqAIAnalyzer {
    
    /**
     * Result object for code analysis
     */
    public static class AnalysisResult {
        public List<String> dataStructures = new ArrayList<>();
        public List<String> algorithms = new ArrayList<>();
        public double aiDetectionScore = 0;
        public double aiConfidence = 0;
        public String summary = "";
    }

    public AnalysisResult analyze(String sourceCode, String apiKey, String model) {
        if (sourceCode == null || sourceCode.isBlank()) {
            return heuristic(sourceCode);
        }
        if (apiKey == null || apiKey.isBlank()) {
            return heuristic(sourceCode);
        }

        AnalysisResult result = new AnalysisResult();
        
        try {
            // Call Groq API
            result = callGroqAPI(sourceCode, apiKey, model);
        } catch (Exception e) {
            // Fallback to heuristic
            result = heuristic(sourceCode);
        }
        
        return result;
    }

    private AnalysisResult callGroqAPI(String sourceCode, String apiKey, String model) {
        // Implementation for Groq API call
        AnalysisResult result = new AnalysisResult();
        
        // TODO: Implement actual Groq API call
        // For now, return heuristic result
        return heuristic(sourceCode);
    }

    private AnalysisResult heuristic(String sourceCode) {
        AnalysisResult result = new AnalysisResult();
        
        // Simple heuristic analysis
        if (sourceCode == null) {
            return result;
        }

        // Detect data structures
        if (sourceCode.contains("vector") || sourceCode.contains("ArrayList")) 
            result.dataStructures.add("Array/Vector");
        if (sourceCode.contains("map") || sourceCode.contains("HashMap")) 
            result.dataStructures.add("HashMap");
        if (sourceCode.contains("queue") || sourceCode.contains("Queue")) 
            result.dataStructures.add("Queue");
        if (sourceCode.contains("stack") || sourceCode.contains("Stack")) 
            result.dataStructures.add("Stack");
        if (sourceCode.contains("tree") || sourceCode.contains("Tree")) 
            result.dataStructures.add("Tree");
        if (sourceCode.contains("graph") || sourceCode.contains("Graph")) 
            result.dataStructures.add("Graph");

        // Detect algorithms
        if (sourceCode.contains("dfs") || sourceCode.contains("DFS")) 
            result.algorithms.add("DFS");
        if (sourceCode.contains("bfs") || sourceCode.contains("BFS")) 
            result.algorithms.add("BFS");
        if (sourceCode.contains("sort")) 
            result.algorithms.add("Sorting");
        if (sourceCode.contains("binary")) 
            result.algorithms.add("Binary Search");
        if (sourceCode.contains("dijkstra")) 
            result.algorithms.add("Dijkstra");
        if (sourceCode.contains("dp") || sourceCode.contains("dynamic")) 
            result.algorithms.add("Dynamic Programming");

        // Simple AI detection (heuristic)
        result.aiDetectionScore = calculateAIScore(sourceCode);
        result.aiConfidence = 50.0; // Default confidence
        result.summary = "Heuristic analysis - " + result.dataStructures.size() + " structures, " + 
                        result.algorithms.size() + " algorithms detected";

        return result;
    }

    private double calculateAIScore(String sourceCode) {
        // Simple heuristic: check for common AI patterns
        double score = 0;

        // Very high code quality might indicate AI
        if (sourceCode.contains("TODO") || sourceCode.contains("FIXME")) 
            score += 5;
        
        // Perfectly formatted code
        if (sourceCode.lines().allMatch(line -> line.startsWith("  ") || line.isEmpty())) 
            score += 10;
        
        // Comments explaining every function
        long commentLines = sourceCode.lines().filter(line -> line.trim().startsWith("//")).count();
        score += (commentLines / Math.max(1, sourceCode.lines().count())) * 20;

        return Math.min(100, score);
    }
}