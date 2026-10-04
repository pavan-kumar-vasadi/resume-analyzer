package com.resumeanalyzer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class JobSkillId implements Serializable {
    @Column(name = "job_id") private Long jobId;
    @Column(name = "skill_id") private Long skillId;
    public JobSkillId() {}
    @Override public boolean equals(Object o) {
        return o instanceof JobSkillId x && Objects.equals(jobId, x.jobId) && Objects.equals(skillId, x.skillId);
    }
    @Override public int hashCode() { return Objects.hash(jobId, skillId); }
}
