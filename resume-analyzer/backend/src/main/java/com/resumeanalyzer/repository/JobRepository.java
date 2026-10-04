package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    long countByActive(boolean active);

    /** Rows of {experience level, number of jobs}; level is null for jobs without one. */
    @org.springframework.data.jpa.repository.Query("select j.experienceLevel, count(j) from Job j group by j.experienceLevel")
    java.util.List<Object[]> countByLevel();
}
