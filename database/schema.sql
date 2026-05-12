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
