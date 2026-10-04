package com.resumeanalyzer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SavedJobId implements Serializable {
    @Column(name = "user_id") private Long userId;
    @Column(name = "job_id") private Long jobId;
    public SavedJobId() {}
    public SavedJobId(Long userId, Long jobId) { this.userId = userId; this.jobId = jobId; }
    public Long getUserId() { return userId; }
    public Long getJobId() { return jobId; }
    @Override public boolean equals(Object o) {
        return o instanceof SavedJobId x && Objects.equals(userId, x.userId) && Objects.equals(jobId, x.jobId);
    }
    @Override public int hashCode() { return Objects.hash(userId, jobId); }
}
