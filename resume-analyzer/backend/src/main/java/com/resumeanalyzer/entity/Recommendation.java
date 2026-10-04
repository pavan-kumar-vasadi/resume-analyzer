package com.resumeanalyzer.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations")
public class Recommendation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "resume_id") private Resume resume;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "job_id") private Job job;
    @Column(name = "match_score", nullable = false) private BigDecimal matchScore;
    @Column(name = "matched_skills", columnDefinition = "TEXT") private String matchedSkills; // required skills found, "|"-separated
    @Column(name = "missing_skills", columnDefinition = "TEXT") private String missingSkills; // required skills missing
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;

    protected Recommendation() {}
    public Recommendation(Resume resume, Job job, BigDecimal score, String matched, String missing, LocalDateTime at) {
        this.resume = resume; this.job = job; this.matchScore = score; this.matchedSkills = matched; this.missingSkills = missing; this.createdAt = at;
    }
    public Job getJob() { return job; }
    public BigDecimal getMatchScore() { return matchScore; }
    public String getMatchedSkills() { return matchedSkills; }
    public String getMissingSkills() { return missingSkills; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
