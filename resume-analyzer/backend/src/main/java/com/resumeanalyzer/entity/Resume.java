package com.resumeanalyzer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "resumes")
public class Resume {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
    @Column(name = "file_name", nullable = false) private String fileName;
    @Column(name = "stored_file_name", nullable = false) private String storedFileName;
    @Column(name = "extracted_text", columnDefinition = "MEDIUMTEXT") private String extractedText;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ResumeStatus status = ResumeStatus.UPLOADED;
    @Column(name = "uploaded_at", updatable = false) private LocalDateTime uploadedAt = LocalDateTime.now();
    @ManyToMany
    @JoinTable(name = "resume_skills", joinColumns = @JoinColumn(name = "resume_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills = new LinkedHashSet<>();

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User v) { this.user = v; }
    public String getFileName() { return fileName; }
    public void setFileName(String v) { this.fileName = v; }
    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String v) { this.storedFileName = v; }
    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String v) { this.extractedText = v; }
    public ResumeStatus getStatus() { return status; }
    public void setStatus(ResumeStatus v) { this.status = v; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public Set<Skill> getSkills() { return skills; }
}
