USE resume_analyzer;
-- OPTIONAL: more DEMONSTRATION jobs (fictional, not real vacancies). Run once.
INSERT INTO jobs (title, company_name, description, location, employment_type, experience_level, min_experience_years, is_demo) VALUES
('[DEMO] Backend Developer','Demo Company C','Demonstration record. Build APIs with Spring Boot.','Bengaluru','Full-time','Mid',2,TRUE),
('[DEMO] Full Stack Developer','Demo Company D','Demonstration record. React and Node.js applications.','Remote','Full-time','Mid',2,TRUE),
('[DEMO] Data Analyst Intern','Demo Company E','Demonstration record. Analyze datasets with Python and SQL.','Hyderabad','Internship','Entry',0,TRUE),
('[DEMO] QA Engineer','Demo Company F','Demonstration record. Test automation with Selenium.','Chennai','Full-time','Entry',0,TRUE),
('[DEMO] DevOps Engineer','Demo Company G','Demonstration record. CI/CD pipelines and containers.','Pune','Full-time','Senior',4,TRUE),
('[DEMO] Frontend Developer','Demo Company H','Demonstration record. Responsive UIs with React.','Remote','Contract','Mid',1,TRUE);

INSERT INTO job_skills (job_id, skill_id, is_required)
SELECT j.id, s.id, m.req FROM (
 SELECT '[DEMO] Backend Developer' t,'Java' sk,1 req UNION ALL SELECT '[DEMO] Backend Developer','Spring Boot',1 UNION ALL SELECT '[DEMO] Backend Developer','MySQL',1
 UNION ALL SELECT '[DEMO] Backend Developer','REST API',1 UNION ALL SELECT '[DEMO] Backend Developer','Docker',0 UNION ALL SELECT '[DEMO] Backend Developer','Git',0
 UNION ALL SELECT '[DEMO] Full Stack Developer','JavaScript',1 UNION ALL SELECT '[DEMO] Full Stack Developer','React',1 UNION ALL SELECT '[DEMO] Full Stack Developer','Node.js',1
 UNION ALL SELECT '[DEMO] Full Stack Developer','MongoDB',1 UNION ALL SELECT '[DEMO] Full Stack Developer','Git',0 UNION ALL SELECT '[DEMO] Full Stack Developer','Docker',0
 UNION ALL SELECT '[DEMO] Data Analyst Intern','Python',1 UNION ALL SELECT '[DEMO] Data Analyst Intern','SQL',1 UNION ALL SELECT '[DEMO] Data Analyst Intern','Data Analysis',1
 UNION ALL SELECT '[DEMO] Data Analyst Intern','Machine Learning',0
 UNION ALL SELECT '[DEMO] QA Engineer','Manual Testing',1 UNION ALL SELECT '[DEMO] QA Engineer','Selenium',1 UNION ALL SELECT '[DEMO] QA Engineer','JUnit',1 UNION ALL SELECT '[DEMO] QA Engineer','Postman',0
 UNION ALL SELECT '[DEMO] DevOps Engineer','Docker',1 UNION ALL SELECT '[DEMO] DevOps Engineer','Jenkins',1 UNION ALL SELECT '[DEMO] DevOps Engineer','CI/CD',1
 UNION ALL SELECT '[DEMO] DevOps Engineer','Git',1 UNION ALL SELECT '[DEMO] DevOps Engineer','AWS',0
 UNION ALL SELECT '[DEMO] Frontend Developer','HTML',1 UNION ALL SELECT '[DEMO] Frontend Developer','CSS',1 UNION ALL SELECT '[DEMO] Frontend Developer','JavaScript',1
 UNION ALL SELECT '[DEMO] Frontend Developer','React',1 UNION ALL SELECT '[DEMO] Frontend Developer','Bootstrap',0 UNION ALL SELECT '[DEMO] Frontend Developer','Git',0
) m JOIN jobs j ON j.title = m.t JOIN skills s ON s.name = m.sk;
