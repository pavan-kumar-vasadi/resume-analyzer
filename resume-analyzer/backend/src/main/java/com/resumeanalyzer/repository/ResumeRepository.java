package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.entity.ResumeStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUserIdOrderByUploadedAtDesc(Long userId);
    Optional<Resume> findByIdAndUserId(Long id, Long userId); // ownership enforced in the query

    // Admin dashboard (aggregates across all users)
    long countByStatus(ResumeStatus status);
    List<Resume> findTop5ByOrderByUploadedAtDesc();

    /** Rows of {skill name, number of resumes containing it}, most common first. */
    @Query("select s.name, count(r) from Resume r join r.skills s group by s.name order by count(r) desc, s.name asc")
    List<Object[]> topSkills(Pageable page);
}
