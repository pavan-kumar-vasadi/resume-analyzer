package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.SavedJob;
import com.resumeanalyzer.entity.SavedJobId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SavedJobRepository extends JpaRepository<SavedJob, SavedJobId> {
    @Query("select s from SavedJob s where s.id.userId = :uid order by s.savedAt desc")
    List<SavedJob> findByUser(@Param("uid") Long userId);
}
