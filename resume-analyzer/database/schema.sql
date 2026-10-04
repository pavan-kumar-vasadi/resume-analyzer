CREATE DATABASE IF NOT EXISTS resume_analyzer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE resume_analyzer;

CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  role ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE resumes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  stored_file_name VARCHAR(100) NOT NULL,
  extracted_text MEDIUMTEXT,
  status ENUM('UPLOADED','ANALYZED','NO_TEXT','FAILED') NOT NULL DEFAULT 'UPLOADED',
  uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_resumes_stored UNIQUE (stored_file_name),
  CONSTRAINT fk_resumes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_resumes_user (user_id)
);

CREATE TABLE skills (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80) NOT NULL,
  category VARCHAR(40) NOT NULL,
  CONSTRAINT uq_skills_name UNIQUE (name),
  INDEX idx_skills_category (category)
);

CREATE TABLE resume_skills (
  resume_id BIGINT NOT NULL,
  skill_id BIGINT NOT NULL,
  detection_method VARCHAR(40) NOT NULL DEFAULT 'DICTIONARY',
  PRIMARY KEY (resume_id, skill_id),
  FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
  FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

CREATE TABLE jobs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(150) NOT NULL,
  company_name VARCHAR(150) NOT NULL,
  description TEXT NOT NULL,
  location VARCHAR(120),
  employment_type VARCHAR(40),
  experience_level VARCHAR(40),
  min_experience_years INT NOT NULL DEFAULT 0,
  salary_range VARCHAR(80),
  application_url VARCHAR(500),
  is_demo BOOLEAN NOT NULL DEFAULT FALSE,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_jobs_active (active),
  INDEX idx_jobs_title (title),
  INDEX idx_jobs_company (company_name),
  INDEX idx_jobs_filters (location, experience_level, employment_type)
);

CREATE TABLE job_skills (
  job_id BIGINT NOT NULL,
  skill_id BIGINT NOT NULL,
  is_required BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (job_id, skill_id),
  FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
  FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

CREATE TABLE recommendations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  resume_id BIGINT NOT NULL,
  job_id BIGINT NOT NULL,
  match_score DECIMAL(5,2) NOT NULL,
  matched_skills TEXT,
  missing_skills TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
  FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
  INDEX idx_reco_resume_score (resume_id, match_score)
);

CREATE TABLE saved_jobs (
  user_id BIGINT NOT NULL,
  job_id BIGINT NOT NULL,
  saved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, job_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
);
