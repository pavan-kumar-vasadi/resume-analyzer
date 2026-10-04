package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Recommendation;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByResumeIdOrderByMatchScoreDesc(Long resumeId);

    // No clearAutomatically: clearing the session would detach already-loaded jobs and break lazy loading of their skills.
    @Modifying(flushAutomatically = true)
    @Query("delete from Recommendation r where r.resume.id = :rid")
    void deleteByResume(@Param("rid") Long resumeId);

    // Admin dashboard
    @Query("select count(distinct r.resume.id) from Recommendation r")
    long countAnalyzedResumes();

    /** Rows of {resume id, time of its latest job analysis}, newest first. */
    @Query("select r.resume.id, max(r.createdAt) from Recommendation r group by r.resume.id order by max(r.createdAt) desc")
    List<Object[]> recentAnalyses(Pageable page);
}
