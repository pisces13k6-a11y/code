-- Codeforces Analyzer Database Schema
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS codeforces_analyzer
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE codeforces_analyzer;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE NOT NULL,
    rating INT DEFAULT 0,
    max_rating INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_crawled TIMESTAMP NULL
);

-- Submissions table
CREATE TABLE IF NOT EXISTS submissions (
    id BIGINT PRIMARY KEY,
    user_id INT NOT NULL,
    problem_name VARCHAR(255),
    problem_rating INT DEFAULT 0,
    language VARCHAR(100),
    verdict VARCHAR(50),
    time_consumed INT DEFAULT 0,
    memory_consumed INT DEFAULT 0,
    submission_date TIMESTAMP NULL,
    code MEDIUMTEXT,
    analyzed BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- AI Analysis table
CREATE TABLE IF NOT EXISTS ai_analysis (
    id INT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    data_structures JSON,
    algorithms JSON,
    ai_detection_score DECIMAL(5,2) DEFAULT 0.00,
    ai_indicators JSON,
    complexity_analysis VARCHAR(255),
    code_quality_score DECIMAL(5,2) DEFAULT 0.00,
    analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES submissions(id) ON DELETE CASCADE
);

-- User ratings table
CREATE TABLE IF NOT EXISTS user_ratings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    data_structure_score DECIMAL(5,2) DEFAULT 0.00,
    algorithm_score DECIMAL(5,2) DEFAULT 0.00,
    ai_usage_percentage DECIMAL(5,2) DEFAULT 0.00,
    total_submissions INT DEFAULT 0,
    accepted_submissions INT DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Config table
CREATE TABLE IF NOT EXISTS config (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Default config values
INSERT INTO config (config_key, config_value) VALUES
    ('crawl_interval_hours', '24'),
    ('groq_api_key', '')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- Index for performance
CREATE INDEX IF NOT EXISTS idx_submissions_user_id ON submissions(user_id);
CREATE INDEX IF NOT EXISTS idx_submissions_analyzed ON submissions(analyzed);
CREATE INDEX IF NOT EXISTS idx_ai_analysis_submission_id ON ai_analysis(submission_id);

-- ─────────────────────────────────────────────────────────────────────────────
-- Sample data (optional – useful for testing the UI without running the crawler)
-- ─────────────────────────────────────────────────────────────────────────────

-- Sample users
INSERT IGNORE INTO users (id, username, rating, max_rating) VALUES
    (1, 'tourist',   3779, 3979),
    (2, 'Petr',      3516, 3720),
    (3, 'sample_user', 1200, 1400);

-- Sample submissions (no actual source code to keep the file small)
INSERT IGNORE INTO submissions
    (id, user_id, problem_name, problem_rating, language, verdict, time_consumed, memory_consumed, submission_date, code, analyzed)
VALUES
    (100001, 1, '1A - Theatre Square',  800, 'C++17', 'Accepted',   46, 3600, '2024-01-10 08:00:00', NULL, FALSE),
    (100002, 1, '1B - Spreadsheets',   1700, 'C++17', 'Accepted',  124, 5200, '2024-01-11 09:30:00', NULL, FALSE),
    (100003, 2, '2A - Winner',         1400, 'C++17', 'Accepted',   78, 4800, '2024-01-12 10:00:00', NULL, FALSE),
    (100004, 3, '71A - Way Too Long Words', 800, 'Python 3', 'Accepted', 200, 10240, '2024-01-13 11:00:00', NULL, FALSE);

-- Sample AI analysis for submission 100001
INSERT IGNORE INTO ai_analysis
    (submission_id, data_structures, algorithms, ai_detection_score, ai_indicators, complexity_analysis, code_quality_score)
VALUES
    (100001,
     '["array"]',
     '["greedy"]',
     12.00,
     '["Short solution with minimal comments"]',
     'O(1) time, O(1) space',
     75.00);

-- Update analyzed flag for submissions that have analysis
UPDATE submissions SET analyzed = TRUE WHERE id = 100001;

-- Sample user ratings
-- Uses ON DUPLICATE KEY UPDATE (instead of INSERT IGNORE) so that re-running
-- the script refreshes computed scores rather than silently skipping them.
INSERT INTO user_ratings (user_id, data_structure_score, algorithm_score, ai_usage_percentage, total_submissions, accepted_submissions)
VALUES
    (1, 65.00, 72.50, 8.00,  2, 2),
    (2, 48.00, 55.00, 5.00,  1, 1),
    (3, 15.00, 12.00, 0.00,  1, 1)
ON DUPLICATE KEY UPDATE
    data_structure_score = VALUES(data_structure_score),
    algorithm_score      = VALUES(algorithm_score),
    ai_usage_percentage  = VALUES(ai_usage_percentage),
    total_submissions    = VALUES(total_submissions),
    accepted_submissions = VALUES(accepted_submissions);

