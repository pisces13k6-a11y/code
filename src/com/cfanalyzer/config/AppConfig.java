package com.cfanalyzer.config;

/**
 * Application-level configuration constants and settings.
 */
public class AppConfig {

    public static final String APP_NAME = "Codeforces Analyzer";
    public static final String APP_VERSION = "1.0.0";

    // Codeforces URLs
    public static final String CF_SUBMISSIONS_URL = "https://codeforces.com/submissions/";
    public static final String CF_SUBMISSION_DETAIL_URL = "https://codeforces.com/contest/";

    // Groq API
    public static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    public static final String GROQ_MODEL = "mixtral-8x7b-32768";
    public static final int GROQ_MAX_TOKENS = 2000;
    public static final double GROQ_TEMPERATURE = 0.1;

    // Crawler settings
    public static final int DEFAULT_CRAWL_INTERVAL_HOURS = 24;
    public static final int CRAWL_PAGE_LOAD_TIMEOUT_SEC = 30;
    public static final int CRAWL_IMPLICIT_WAIT_SEC = 10;
    public static final int CRAWL_SLEEP_BETWEEN_REQUESTS_MS = 2000;

    // AI detection threshold
    public static final double AI_DETECTION_THRESHOLD = 70.0;

    // GUI
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;

    // Scoring weights for data structures (complexity bonus)
    public static final String[] SIMPLE_DATA_STRUCTURES = {
        "array", "linked_list", "stack", "queue"
    };
    public static final String[] COMPLEX_DATA_STRUCTURES = {
        "tree", "binary_tree", "BST", "AVL", "heap",
        "graph", "hash_table", "hash_map", "hash_set",
        "priority_queue", "disjoint_set", "trie", "segment_tree", "fenwick_tree"
    };

    // Scoring weights for algorithms (difficulty bonus)
    public static final String[] SIMPLE_ALGORITHMS = {
        "bubble_sort", "linear_search", "binary_search"
    };
    public static final String[] COMPLEX_ALGORITHMS = {
        "merge_sort", "quick_sort", "heap_sort", "ternary_search",
        "DFS", "BFS", "dijkstra", "bellman_ford", "floyd_warshall",
        "kruskal", "prim", "dynamic_programming", "greedy",
        "divide_and_conquer", "two_pointers", "sliding_window",
        "backtracking", "KMP", "rabin_karp"
    };
}
