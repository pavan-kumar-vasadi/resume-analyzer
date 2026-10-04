USE resume_analyzer;
-- Skill dictionary seed
INSERT IGNORE INTO skills (name, category) VALUES
('Java','Programming'),('Python','Programming'),('C','Programming'),('C++','Programming'),('JavaScript','Programming'),
('HTML','Frontend'),('CSS','Frontend'),('React','Frontend'),('Angular','Frontend'),('Bootstrap','Frontend'),
('Spring Boot','Backend'),('Spring MVC','Backend'),('REST API','Backend'),('Node.js','Backend'),('Express','Backend'),
('MySQL','Database'),('PostgreSQL','Database'),('MongoDB','Database'),('SQL','Database'),
('Git','Tools'),('GitHub','Tools'),('Docker','Tools'),('Maven','Tools'),('Postman','Tools'),
('OOP','Concepts'),('DBMS','Concepts'),('Operating Systems','Concepts'),('Computer Networks','Concepts'),('Data Structures','Concepts'),('Algorithms','Concepts'),
('JUnit','Testing'),('Selenium','Testing'),('Manual Testing','Testing'),
('AWS','Cloud/DevOps'),('Azure','Cloud/DevOps'),('CI/CD','Cloud/DevOps'),('Jenkins','Cloud/DevOps'),
('Machine Learning','Data/AI'),('NLP','Data/AI'),('Data Analysis','Data/AI');

-- DEMONSTRATION JOBS ONLY: fictional, not real vacancies (is_demo = TRUE)
INSERT INTO jobs (title, company_name, description, location, employment_type, experience_level, min_experience_years, is_demo) VALUES
('[DEMO] Junior Java Developer','Demo Company A','Demonstration record. Build REST services with Spring Boot and MySQL.','Remote','Full-time','Entry',0,TRUE),
('[DEMO] Frontend Developer Intern','Demo Company B','Demonstration record. Build React interfaces.','Hyderabad','Internship','Entry',0,TRUE);

INSERT INTO job_skills (job_id, skill_id, is_required)
SELECT 1, id, TRUE FROM skills WHERE name IN ('Java','Spring Boot','MySQL','REST API');
INSERT INTO job_skills (job_id, skill_id, is_required)
SELECT 1, id, FALSE FROM skills WHERE name IN ('Docker','JUnit');
INSERT INTO job_skills (job_id, skill_id, is_required)
SELECT 2, id, TRUE FROM skills WHERE name IN ('HTML','CSS','JavaScript','React');
INSERT INTO job_skills (job_id, skill_id, is_required)
SELECT 2, id, FALSE FROM skills WHERE name IN ('Git');
-- Admin/test users are created in Phase 2 via the registration/seed runner (BCrypt hashes are never hand-written).
