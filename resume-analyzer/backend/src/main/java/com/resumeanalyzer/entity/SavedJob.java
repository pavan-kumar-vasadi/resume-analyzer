package com.resumeanalyzer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_jobs")
public class SavedJob {
    @EmbeddedId private SavedJobId id;
    @Column(name = "saved_at", updatable = false) private LocalDateTime savedAt = LocalDateTime.now();
    protected SavedJob() {}
    public SavedJob(SavedJobId id) { this.id = id; }
    public SavedJobId getId() { return id; }
    public LocalDateTime getSavedAt() { return savedAt; }
}
