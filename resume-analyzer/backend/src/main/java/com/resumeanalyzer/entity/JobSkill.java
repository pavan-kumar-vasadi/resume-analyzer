package com.resumeanalyzer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "job_skills")
public class JobSkill {
    @EmbeddedId private JobSkillId id = new JobSkillId();
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("jobId") @JoinColumn(name = "job_id") private Job job;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("skillId") @JoinColumn(name = "skill_id") private Skill skill;
    @Column(name = "is_required", nullable = false) private boolean required;

    protected JobSkill() {}
    public JobSkill(Job job, Skill skill, boolean required) { this.job = job; this.skill = skill; this.required = required; }
    public Job getJob() { return job; }
    public Skill getSkill() { return skill; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean v) { this.required = v; }
}
