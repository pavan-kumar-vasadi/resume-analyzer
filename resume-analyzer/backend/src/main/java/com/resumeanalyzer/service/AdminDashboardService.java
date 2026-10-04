package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.*;
import com.resumeanalyzer.entity.*;
import com.resumeanalyzer.repository.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Platform-wide figures for admins. Every number is read from the database; nothing is hardcoded. */
@Service
public class AdminDashboardService {
    static final int DAYS = 14;
    static final int TOP_SKILLS = 8;

    private final UserRepository users;
    private final ResumeRepository resumes;
    private final JobRepository jobs;
    private final RecommendationRepository recs;

    public AdminDashboardService(UserRepository users, ResumeRepository resumes, JobRepository jobs, RecommendationRepository recs) {
        this.users = users; this.resumes = resumes; this.jobs = jobs; this.recs = recs;
    }

    @Transactional(readOnly = true)
    public AdminDashboardDto dashboard() {
        LocalDate today = LocalDate.now();
        List<AdminAnalysisDto> analyses = recentAnalyses();
        return new AdminDashboardDto(
            users.count(), users.countByRole(Role.ADMIN), resumes.count(), jobs.countByActive(true), jobs.count(), recs.countAnalyzedResumes(),
            registrations(today),
            resumes.topSkills(PageRequest.of(0, TOP_SKILLS)).stream().map(r -> new LabelCountDto((String) r[0], ((Number) r[1]).longValue())).toList(),
            Arrays.stream(ResumeStatus.values()).map(s -> new LabelCountDto(s.name(), resumes.countByStatus(s))).toList(),
            jobsByLevel(),
            users.findTop5ByOrderByCreatedAtDesc().stream().map(UserDto::from).toList(),
            resumes.findTop5ByOrderByUploadedAtDesc().stream()
                .map(r -> new AdminResumeDto(r.getId(), r.getFileName(), r.getUser().getFullName(), r.getStatus().name(), r.getSkills().size(), r.getUploadedAt())).toList(),
            analyses);
    }

    /** New accounts per day for the last DAYS days (days with no sign-ups are included as 0). */
    public List<DayCountDto> registrations(LocalDate today) {
        LocalDate from = today.minusDays(DAYS - 1L);
        Map<LocalDate, Integer> byDay = new LinkedHashMap<>();
        for (int i = 0; i < DAYS; i++) byDay.put(from.plusDays(i), 0);
        for (User u : users.findByCreatedAtGreaterThanEqual(from.atStartOfDay())) {
            if (u.getCreatedAt() == null) continue;
            LocalDate d = u.getCreatedAt().toLocalDate();
            if (byDay.containsKey(d)) byDay.merge(d, 1, Integer::sum);
        }
        return byDay.entrySet().stream().map(e -> new DayCountDto(e.getKey().toString(), e.getValue())).toList();
    }

    /** Jobs per experience level in a fixed Entry, Mid, Senior order; anything else is grouped as Unspecified. */
    private List<LabelCountDto> jobsByLevel() {
        Map<String, Long> m = new LinkedHashMap<>();
        for (String k : List.of("Entry", "Mid", "Senior")) m.put(k, 0L);
        for (Object[] row : jobs.countByLevel()) {
            String k = row[0] == null || !m.containsKey(row[0].toString()) ? "Unspecified" : row[0].toString();
            m.merge(k, ((Number) row[1]).longValue(), Long::sum);
        }
        if (m.getOrDefault("Unspecified", 0L) == 0) m.remove("Unspecified");
        return m.entrySet().stream().map(e -> new LabelCountDto(e.getKey(), e.getValue())).toList();
    }

    private List<AdminAnalysisDto> recentAnalyses() {
        List<AdminAnalysisDto> out = new ArrayList<>();
        for (Object[] row : recs.recentAnalyses(PageRequest.of(0, 5))) {
            Long resumeId = ((Number) row[0]).longValue();
            Resume r = resumes.findById(resumeId).orElse(null);
            if (r == null) continue;
            List<Recommendation> l = recs.findByResumeIdOrderByMatchScoreDesc(resumeId);
            Recommendation best = l.isEmpty() ? null : l.get(0);
            out.add(new AdminAnalysisDto(resumeId, r.getFileName(), r.getUser().getFullName(), l.size(),
                best == null ? null : best.getMatchScore().doubleValue(), best == null ? null : best.getJob().getTitle(), toDateTime(row[1])));
        }
        return out;
    }

    private static LocalDateTime toDateTime(Object o) {
        if (o instanceof LocalDateTime t) return t;
        if (o instanceof Timestamp t) return t.toLocalDateTime();
        return null;
    }
}
