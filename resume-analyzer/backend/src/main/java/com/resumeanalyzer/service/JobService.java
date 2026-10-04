package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.*;
import com.resumeanalyzer.entity.*;
import com.resumeanalyzer.exception.ApiException;
import com.resumeanalyzer.repository.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService {
    private final JobRepository jobs;
    private final SkillRepository skills;
    private final SavedJobRepository saved;
    private final UserRepository users;

    public JobService(JobRepository jobs, SkillRepository skills, SavedJobRepository saved, UserRepository users) {
        this.jobs = jobs; this.skills = skills; this.saved = saved; this.users = users;
    }

    @Transactional(readOnly = true)
    public PageDto<JobDto> search(JobFilter f, int page, int size, String email, boolean activeOnly) {
        int s = Math.max(1, Math.min(size, 50));
        Page<Job> res = jobs.findAll(JobSpecs.filter(activeOnly, f),
            PageRequest.of(Math.max(0, page), s, Sort.by(Sort.Direction.DESC, "createdAt", "id")));
        Set<Long> savedIds = savedIds(email);
        return new PageDto<>(res.getContent().stream().map(j -> toDto(j, savedIds.contains(j.getId()))).toList(),
            res.getNumber(), res.getSize(), res.getTotalElements(), res.getTotalPages());
    }

    @Transactional(readOnly = true)
    public JobDto get(Long id, String email, boolean adminView) {
        Job j = find(id);
        if (!adminView && !j.isActive()) throw new ApiException(HttpStatus.NOT_FOUND, "Job not found");
        return toDto(j, savedIds(email).contains(id));
    }

    @Transactional(readOnly = true)
    public JobSkillsDto skillsOf(Long id) {
        JobDto d = get(id, null, false);
        return new JobSkillsDto(d.requiredSkills(), d.preferredSkills());
    }

    @Transactional
    public JobDto create(JobRequest r) {
        Job j = new Job();
        apply(j, r);
        syncSkills(j, r);
        return toDto(jobs.save(j), false);
    }

    @Transactional
    public JobDto update(Long id, JobRequest r) {
        Job j = find(id);
        apply(j, r);
        syncSkills(j, r);
        return toDto(j, false);
    }

    @Transactional
    public JobDto setActive(Long id, boolean active) {
        Job j = find(id);
        j.setActive(active);
        return toDto(j, false);
    }

    @Transactional
    public void delete(Long id) { jobs.delete(find(id)); }

    // ---- saved jobs ----
    @Transactional
    public void save(Long jobId, String email) {
        Job j = find(jobId);
        if (!j.isActive()) throw new ApiException(HttpStatus.NOT_FOUND, "Job not found");
        SavedJobId sid = new SavedJobId(uid(email), jobId);
        if (!saved.existsById(sid)) saved.save(new SavedJob(sid)); // idempotent
    }

    @Transactional
    public void unsave(Long jobId, String email) { saved.deleteById(new SavedJobId(uid(email), jobId)); }

    @Transactional(readOnly = true)
    public List<JobDto> savedJobs(String email) {
        List<Long> ids = saved.findByUser(uid(email)).stream().map(s -> s.getId().getJobId()).toList();
        Map<Long, Job> byId = jobs.findAllById(ids).stream().collect(Collectors.toMap(Job::getId, x -> x));
        return ids.stream().map(byId::get).filter(Objects::nonNull).map(j -> toDto(j, true)).toList();
    }

    // ---- helpers ----
    private Job find(Long id) { return jobs.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Job not found")); }

    private Long uid(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required")).getId();
    }

    private Set<Long> savedIds(String email) {
        if (email == null) return Set.of();
        return users.findByEmail(email).map(u -> saved.findByUser(u.getId()).stream().map(s -> s.getId().getJobId()).collect(Collectors.toSet())).orElse(Set.of());
    }

    private static String clean(String s) { return s == null || s.isBlank() ? null : s.trim(); }

    private void apply(Job j, JobRequest r) {
        j.setTitle(r.title().trim()); j.setCompanyName(r.companyName().trim()); j.setDescription(r.description().trim());
        j.setLocation(clean(r.location())); j.setEmploymentType(r.employmentType()); j.setExperienceLevel(r.experienceLevel());
        j.setMinExperienceYears(r.minExperienceYears() == null ? 0 : r.minExperienceYears());
        j.setSalaryRange(clean(r.salaryRange())); j.setApplicationUrl(clean(r.applicationUrl()));
        j.setActive(r.active() == null || r.active());
    }

    /** Diff-based update (avoids insert/delete primary-key clashes on job_skills). Required wins over preferred. */
    private void syncSkills(Job j, JobRequest r) {
        Map<Long, Boolean> want = new LinkedHashMap<>();
        if (r.preferredSkillIds() != null) r.preferredSkillIds().forEach(id -> want.put(id, false));
        if (r.requiredSkillIds() != null) r.requiredSkillIds().forEach(id -> want.put(id, true));
        Map<Long, Skill> found = skills.findAllById(want.keySet()).stream().collect(Collectors.toMap(Skill::getId, s -> s));
        if (found.size() != want.size()) throw new ApiException(HttpStatus.BAD_REQUEST, "One or more selected skills do not exist");
        j.getJobSkills().removeIf(js -> !want.containsKey(js.getSkill().getId()));
        for (JobSkill js : j.getJobSkills()) js.setRequired(want.get(js.getSkill().getId()));
        Set<Long> existing = j.getJobSkills().stream().map(js -> js.getSkill().getId()).collect(Collectors.toSet());
        want.forEach((sid, req) -> { if (!existing.contains(sid)) j.getJobSkills().add(new JobSkill(j, found.get(sid), req)); });
    }

    private JobDto toDto(Job j, boolean isSaved) {
        return new JobDto(j.getId(), j.getTitle(), j.getCompanyName(), j.getDescription(), j.getLocation(), j.getEmploymentType(),
            j.getExperienceLevel(), j.getMinExperienceYears(), j.getSalaryRange(), j.getApplicationUrl(), j.isDemo(), j.isActive(),
            j.getCreatedAt(), skillList(j, true), skillList(j, false), isSaved);
    }

    private List<SkillDto> skillList(Job j, boolean required) {
        return j.getJobSkills().stream().filter(js -> js.isRequired() == required).map(JobSkill::getSkill)
            .sorted(Comparator.comparing(Skill::getName)).map(s -> new SkillDto(s.getId(), s.getName(), s.getCategory())).toList();
    }
}
