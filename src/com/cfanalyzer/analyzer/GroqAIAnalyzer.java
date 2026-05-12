package com.cfanalyzer.analyzer;

import com.cfanalyzer.config.AppConfig;
import com.cfanalyzer.model.Analysis;
import com.google.gson.*;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Calls Groq API to analyze source code for data structures, algorithms, and AI usage.
 */
public class GroqAIAnalyzer {

    private static final MediaType JSON_MEDIA = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient httpClient;
    private final Gson gson = new Gson();

    public GroqAIAnalyzer() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Analyze the given source code and return an Analysis object.
     * @param code the source code to analyze
     * @param apiKey the Groq API key
     * @return Analysis result or null if failed
     */
    public Analysis analyzeCode(String code, String apiKey) {
        if (code == null || code.trim().isEmpty()) {
            System.err.println("[WARN] Empty code provided for analysis.");
            return buildFallbackAnalysis();
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("[WARN] No Groq API key configured.");
            return buildFallbackAnalysis();
        }

        String prompt = buildPrompt(code);
        String responseBody = callGroqApi(prompt, apiKey);
        if (responseBody == null) return buildFallbackAnalysis();

        return parseResponse(responseBody);
    }

    /**
     * Build the prompt for Groq API.
     */
    private String buildPrompt(String code) {
        // Truncate very long code to avoid token limits
        String truncatedCode = code.length() > 8000 ? code.substring(0, 8000) + "\n... (truncated)" : code;
        return "Analyze the following competitive programming code and respond with ONLY a valid JSON object (no markdown, no explanation).\n\n" +
               "JSON format:\n" +
               "{\n" +
               "  \"data_structures\": [\"array\", \"stack\", ...],\n" +
               "  \"algorithms\": [\"binary_search\", \"DFS\", ...],\n" +
               "  \"ai_detection_score\": 0-100,\n" +
               "  \"ai_indicators\": [\"reason1\", \"reason2\", ...],\n" +
               "  \"complexity_analysis\": \"O(n log n) time, O(n) space\",\n" +
               "  \"code_quality_score\": 0-100\n" +
               "}\n\n" +
               "Rules:\n" +
               "- data_structures: list EVERY data structure found (array, linked_list, stack, queue, deque, tree, binary_tree, BST, AVL, heap, graph, hash_table, hash_map, hash_set, priority_queue, disjoint_set, trie, segment_tree, fenwick_tree)\n" +
               "- algorithms: list EVERY algorithm found (bubble_sort, merge_sort, quick_sort, heap_sort, linear_search, binary_search, ternary_search, DFS, BFS, dijkstra, bellman_ford, floyd_warshall, kruskal, prim, dynamic_programming, greedy, divide_and_conquer, two_pointers, sliding_window, backtracking, KMP, rabin_karp)\n" +
               "- ai_detection_score: 0=human written, 100=definitely AI generated. Consider: overly consistent formatting, generic variable names (i,j,k for complex logic), unusual comment density, absence of debugging artifacts, perfect indentation\n" +
               "- code_quality_score: 0=poor, 100=excellent\n\n" +
               "Code to analyze:\n```\n" + truncatedCode + "\n```";
    }

    /**
     * Call the Groq API and return the raw response body.
     */
    private String callGroqApi(String prompt, String apiKey) {
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);

        JsonArray messages = new JsonArray();
        messages.add(message);

        JsonObject body = new JsonObject();
        body.addProperty("model", AppConfig.GROQ_MODEL);
        body.add("messages", messages);
        body.addProperty("max_tokens", AppConfig.GROQ_MAX_TOKENS);
        body.addProperty("temperature", AppConfig.GROQ_TEMPERATURE);

        Request request = new Request.Builder()
                .url(AppConfig.GROQ_API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(gson.toJson(body), JSON_MEDIA))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("[ERROR] Groq API error: " + response.code() + " " + response.message());
                return null;
            }
            ResponseBody rb = response.body();
            return rb != null ? rb.string() : null;
        } catch (IOException e) {
            System.err.println("[ERROR] Groq API call failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parse the JSON response from Groq API into an Analysis object.
     */
    private Analysis parseResponse(String responseBody) {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray choices = root.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) return buildFallbackAnalysis();

            String content = choices.get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            // Strip markdown code blocks if present
            content = content.trim();
            if (content.startsWith("```")) {
                content = content.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").trim();
            }

            JsonObject result = JsonParser.parseString(content).getAsJsonObject();
            return buildAnalysisFromJson(result);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to parse Groq response: " + e.getMessage());
            return buildFallbackAnalysis();
        }
    }

    private Analysis buildAnalysisFromJson(JsonObject json) {
        Analysis analysis = new Analysis();

        analysis.setDataStructures(parseStringArray(json, "data_structures"));
        analysis.setAlgorithms(parseStringArray(json, "algorithms"));
        analysis.setAiIndicators(parseStringArray(json, "ai_indicators"));

        if (json.has("ai_detection_score")) {
            analysis.setAiDetectionScore(clamp(json.get("ai_detection_score").getAsDouble(), 0, 100));
        }
        if (json.has("code_quality_score")) {
            analysis.setCodeQualityScore(clamp(json.get("code_quality_score").getAsDouble(), 0, 100));
        }
        if (json.has("complexity_analysis")) {
            analysis.setComplexityAnalysis(json.get("complexity_analysis").getAsString());
        }

        return analysis;
    }

    private List<String> parseStringArray(JsonObject json, String key) {
        List<String> list = new ArrayList<>();
        if (!json.has(key)) return list;
        JsonElement el = json.get(key);
        if (el.isJsonArray()) {
            for (JsonElement item : el.getAsJsonArray()) {
                list.add(item.getAsString());
            }
        }
        return list;
    }

    private Analysis buildFallbackAnalysis() {
        Analysis analysis = new Analysis();
        analysis.setDataStructures(new ArrayList<>());
        analysis.setAlgorithms(new ArrayList<>());
        analysis.setAiIndicators(new ArrayList<>());
        analysis.setAiDetectionScore(0.0);
        analysis.setCodeQualityScore(0.0);
        analysis.setComplexityAnalysis("N/A");
        return analysis;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
