package com.resumeanalyzer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "jobs")
public class Job {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String title;
    @Column(name = "company_name", nullable = false) private String companyName;
    @Column(nullable = false, columnDefinition = "TEXT") private String description;
    private String location;
    @Column(name = "employment_type") private String employmentType;
    @Column(name = "experience_level") private String experienceLevel;
    @Column(name = "min_experience_years", nullable = false) private int minExperienceYears;
    @Column(name = "salary_range") private String salaryRange;
    @Column(name = "application_url") private String applicationUrl;
    @Column(name = "is_demo", nullable = false) private boolean demo;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt = LocalDateTime.now();
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true) private Set<JobSkill> jobSkills = new LinkedHashSet<>();

    public Long getId() { return id; }
    public String getTitle() { return title; } public void setTitle(String v) { title = v; }
    public String getCompanyName() { return companyName; } public void setCompanyName(String v) { companyName = v; }
    public String getDescription() { return description; } public void setDescription(String v) { description = v; }
    public String getLocation() { return location; } public void setLocation(String v) { location = v; }
    public String getEmploymentType() { return employmentType; } public void setEmploymentType(String v) { employmentType = v; }
    public String getExperienceLevel() { return experienceLevel; } public void setExperienceLevel(String v) { experienceLevel = v; }
    public int getMinExperienceYears() { return minExperienceYears; } public void setMinExperienceYears(int v) { minExperienceYears = v; }
    public String getSalaryRange() { return salaryRange; } public void setSalaryRange(String v) { salaryRange = v; }
    public String getApplicationUrl() { return applicationUrl; } public void setApplicationUrl(String v) { applicationUrl = v; }
    public boolean isDemo() { return demo; }
    public boolean isActive() { return active; } public void setActive(boolean v) { active = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Set<JobSkill> getJobSkills() { return jobSkills; }
}
