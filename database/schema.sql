CREATE DATABASE IF NOT EXISTS codeforces_analyzer;
USE codeforces_analyzer;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    handle VARCHAR(64) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_crawled_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS submissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    cf_submission_id BIGINT NOT NULL,
    contest_id INT NULL,
    problem_id VARCHAR(64) NULL,
    problem_name VARCHAR(255) NULL,
    language VARCHAR(128) NULL,
    verdict VARCHAR(64) NULL,
    submitted_at TIMESTAMP NULL,
    source_code LONGTEXT NULL,
    crawled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_cf_submission_id (cf_submission_id),
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_sub_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS analysis_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    data_structures TEXT,
    algorithms TEXT,
    ai_detection_score DOUBLE,
    ai_confidence DOUBLE,
    summary TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_analysis_submission (submission_id),
    CONSTRAINT fk_analysis_submission FOREIGN KEY (submission_id) REFERENCES submissions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_ratings (
    user_id BIGINT PRIMARY KEY,
    ds_score DOUBLE NOT NULL DEFAULT 0,
    algo_score DOUBLE NOT NULL DEFAULT 0,
    ai_usage_percent DOUBLE NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_rating_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS config (
    config_key VARCHAR(128) PRIMARY KEY,
    config_value TEXT NULL
);

INSERT INTO config(config_key, config_value)
VALUES
('crawl_interval_hours', '24'),
('groq_api_key', ''),
('groq_model', 'mixtral-8x7b-32768')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);
