package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.JobFilter;
import com.resumeanalyzer.entity.*;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** Parameterized JPA criteria queries (no string-built SQL). */
public final class JobSpecs {
    private JobSpecs() {}

    public static Specification<Job> filter(boolean activeOnly, JobFilter f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (activeOnly) p.add(cb.isTrue(root.<Boolean>get("active")));
            if (has(f.q())) {
                String l = like(f.q());
                p.add(cb.or(cb.like(cb.lower(root.<String>get("title")), l, '\\'), cb.like(cb.lower(root.<String>get("companyName")), l, '\\')));
            }
            if (has(f.location())) p.add(cb.like(cb.lower(root.<String>get("location")), like(f.location()), '\\'));
            if (has(f.experienceLevel())) p.add(cb.equal(cb.lower(root.<String>get("experienceLevel")), f.experienceLevel().trim().toLowerCase()));
            if (has(f.employmentType())) p.add(cb.equal(cb.lower(root.<String>get("employmentType")), f.employmentType().trim().toLowerCase()));
            if (has(f.skill())) {
                Join<Job, JobSkill> js = root.join("jobSkills");
                Join<JobSkill, Skill> sk = js.join("skill");
                p.add(cb.equal(cb.lower(sk.<String>get("name")), f.skill().trim().toLowerCase()));
                query.distinct(true);
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    private static boolean has(String s) { return s != null && !s.isBlank(); }

    private static String like(String s) {
        return "%" + s.trim().toLowerCase().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
