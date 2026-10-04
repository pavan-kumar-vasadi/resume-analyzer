package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.*;
import com.resumeanalyzer.entity.*;
import com.resumeanalyzer.exception.ApiException;
import com.resumeanalyzer.repository.*;
import com.resumeanalyzer.util.LearningTopics;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationService {
    private final ResumeRepository resumes;
    private final UserRepository users;
    private final JobRepository jobs;
    private final RecommendationRepository recs;
    private final SavedJobRepository saved;
    private final SkillRepository skills;
    private final double requiredWeight;

    public RecommendationService(ResumeRepository resumes, UserRepository users, JobRepository jobs, RecommendationRepository recs,
            SavedJobRepository saved, SkillRepository skills, @Value("${app.recommendation.required-weight:0.8}") double requiredWeight) {
        this.resumes = resumes; this.users = users; this.jobs = jobs; this.recs = recs; this.saved = saved; this.skills = skills;
        this.requiredWeight = requiredWeight;
    }

    @Transactional
    public RecommendationsDto analyze(Long resumeId, String email) {
        Resume r = owned(resumeId, email);
        if (r.getStatus() != ResumeStatus.ANALYZED)
            throw new ApiException(HttpStatus.BAD_REQUEST, "This resume has no readable text, so its skills cannot be matched to jobs.");
        List<String> have = r.getSkills().stream().map(Skill::getName).toList();
        List<Job> active = jobs.findAll(JobSpecs.filter(true, new JobFilter(null, null, null, null, null)));
        recs.deleteByResume(resumeId); // latest analysis replaces the previous one
        LocalDateTime now = LocalDateTime.now().withNano(0);
        List<Recommendation> rows = new ArrayList<>();
        for (Job j : active) {
            MatchCalculator.Result m = MatchCalculator.compute(have, names(j, true), names(j, false), requiredWeight);
            rows.add(new Recommendation(r, j, BigDecimal.valueOf(m.score()).setScale(2, RoundingMode.HALF_UP),
                String.join("|", m.matchedRequired()), String.join("|", m.missingRequired()), now));
        }
        recs.saveAll(rows);
        return build(r, email, rows);
    }

    @Transactional(readOnly = true)
    public RecommendationsDto get(Long resumeId, String email) {
        Resume r = owned(resumeId, email);
        return build(r, email, recs.findByResumeIdOrderByMatchScoreDesc(resumeId));
    }

    @Transactional(readOnly = true)
    public SkillGapsDto skillGaps(Long resumeId, String email, String scope) {
        Resume r = owned(resumeId, email);
        boolean savedScope = "saved".equalsIgnoreCase(scope);
        List<Recommendation> rows = sorted(recs.findByResumeIdOrderByMatchScoreDesc(r.getId()));
        List<Recommendation> targets;
        if (savedScope) {
            Set<Long> ids = savedIds(email);
            targets = rows.stream().filter(x -> ids.contains(x.getJob().getId())).toList();
        } else targets = rows.stream().limit(10).toList();

        Map<String, Integer> counts = new HashMap<>();
        for (Recommendation x : targets) split(x.getMissingSkills()).forEach(s -> counts.merge(s, 1, Integer::sum));
        Map<String, String> cats = categories();
        List<SkillGapDto> items = counts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
            .map(e -> new SkillGapDto(e.getKey(), cats.get(e.getKey()), e.getValue(),
                (int) Math.round(100.0 * e.getValue() / targets.size()), LearningTopics.suggest(e.getKey(), cats.get(e.getKey()))))
            .toList();
        String note = rows.isEmpty() ? "Run the job analysis for this resume first."
            : targets.isEmpty() ? "None of your saved jobs were part of the latest analysis. Save some jobs, then re-analyze."
            : items.isEmpty() ? "No missing required skills across these jobs." : null;
        return new SkillGapsDto(savedScope ? "saved" : "top", targets.size(), items, note);
    }

    // ---- helpers ----
    private RecommendationsDto build(Resume r, String email, List<Recommendation> raw) {
        List<Recommendation> rows = sorted(raw);
        Set<String> have = r.getSkills().stream().map(s -> MatchCalculator.norm(s.getName())).collect(Collectors.toSet());
        Set<Long> savedIds = savedIds(email);
        Map<String, String> cats = categories();
        List<RecommendationDto> items = rows.stream().map(x -> toDto(x, have, savedIds, cats)).toList();
        return new RecommendationsDto(r.getId(), r.getFileName(), !raw.isEmpty(), raw.isEmpty() ? null : raw.get(0).getCreatedAt(),
            r.getSkills().size(),
            "Match score = 80% x required-skill coverage + 20% x preferred-skill coverage (if a job lists no preferred skills, required coverage alone is used).",
            "This is a skill-overlap estimate between your resume and a job's listed skills. It is not a probability of being hired.", items);
    }

    private RecommendationDto toDto(Recommendation x, Set<String> have, Set<Long> savedIds, Map<String, String> cats) {
        Job j = x.getJob();
        List<String> matched = split(x.getMatchedSkills()), missing = split(x.getMissingSkills());
        List<String> pref = names(j, false);
        List<String> pm = pref.stream().filter(s -> have.contains(MatchCalculator.norm(s))).toList();
        List<String> px = pref.stream().filter(s -> !have.contains(MatchCalculator.norm(s))).toList();
        int reqTotal = matched.size() + missing.size();
        Integer cov = reqTotal == 0 ? null : (int) Math.round(100.0 * matched.size() / reqTotal);
        String note = reqTotal == 0 && pref.isEmpty() ? "This job lists no skills, so no match score can be calculated (shown as 0)."
            : reqTotal == 0 ? "This job lists no required skills, so the score uses preferred skills only."
            : pref.isEmpty() ? "This job lists no preferred skills, so the score is required-skill coverage only." : null;
        List<LearningTopicDto> topics = missing.stream().map(s -> new LearningTopicDto(s, LearningTopics.suggest(s, cats.get(s)))).toList();
        return new RecommendationDto(j.getId(), j.getTitle(), j.getCompanyName(), j.getLocation(), j.getEmploymentType(),
            j.getExperienceLevel(), j.isDemo(), savedIds.contains(j.getId()), x.getMatchScore().doubleValue(), cov,
            matched, missing, pm, px, topics, x.getCreatedAt(), note);
    }

    private List<Recommendation> sorted(List<Recommendation> rows) {
        return rows.stream().filter(x -> x.getJob().isActive())
            .sorted(Comparator.comparing(Recommendation::getMatchScore).reversed().thenComparing(x -> x.getJob().getTitle())).toList();
    }

    private static List<String> names(Job j, boolean required) {
        return j.getJobSkills().stream().filter(js -> js.isRequired() == required).map(js -> js.getSkill().getName()).sorted().toList();
    }

    private static List<String> split(String s) { return s == null || s.isBlank() ? List.of() : Arrays.asList(s.split("\\|")); }

    private Map<String, String> categories() {
        return skills.findAll().stream().collect(Collectors.toMap(Skill::getName, Skill::getCategory, (a, b) -> a));
    }

    private Set<Long> savedIds(String email) {
        return saved.findByUser(uid(email)).stream().map(s -> s.getId().getJobId()).collect(Collectors.toSet());
    }

    private Long uid(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required")).getId();
    }

    private Resume owned(Long id, String email) {
        return resumes.findByIdAndUserId(id, uid(email)).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Resume not found"));
    }
}
