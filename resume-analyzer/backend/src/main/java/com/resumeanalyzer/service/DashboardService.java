package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.*;
import com.resumeanalyzer.entity.*;
import com.resumeanalyzer.exception.ApiException;
import com.resumeanalyzer.repository.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Every figure is derived from the signed-in user's own database records. */
@Service
public class DashboardService {
    private final ResumeRepository resumes;
    private final UserRepository users;
    private final RecommendationRepository recs;
    private final SavedJobRepository saved;
    private final JobRepository jobs;
    private final ResumeAnalysisService analysis;

    public DashboardService(ResumeRepository resumes, UserRepository users, RecommendationRepository recs, SavedJobRepository saved,
                            JobRepository jobs, ResumeAnalysisService analysis) {
        this.resumes = resumes; this.users = users; this.recs = recs; this.saved = saved; this.jobs = jobs; this.analysis = analysis;
    }

    @Transactional(readOnly = true)
    public DashboardDto dashboard(String email) {
        Long uid = uid(email);
        List<Resume> rs = resumes.findByUserIdOrderByUploadedAtDesc(uid);
        Resume latest = rs.stream().filter(r -> r.getStatus() == ResumeStatus.ANALYZED).findFirst().orElse(null);
        int totalSkills = (int) rs.stream().flatMap(r -> r.getSkills().stream()).map(Skill::getName).distinct().count();

        Map<Long, List<Recommendation>> recMap = new LinkedHashMap<>();
        for (Resume r : rs) recMap.put(r.getId(), activeRecs(r.getId()));
        List<Recommendation> positive = rs.stream().map(r -> recMap.get(r.getId()))
            .map(l -> l.stream().filter(x -> x.getMatchScore().signum() > 0).toList()).filter(l -> !l.isEmpty()).findFirst().orElse(List.of());
        List<TopMatchDto> top = positive.stream().limit(3)
            .map(x -> new TopMatchDto(x.getJob().getId(), x.getJob().getTitle(), x.getJob().getCompanyName(), x.getMatchScore().doubleValue())).toList();

        List<CategoryCountDto> cats = latest == null ? List.of() : latest.getSkills().stream()
            .collect(Collectors.groupingBy(Skill::getCategory, Collectors.counting())).entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
            .map(e -> new CategoryCountDto(e.getKey(), e.getValue().intValue())).toList();

        List<ActivityDto> activity = new ArrayList<>();
        for (Resume r : rs) {
            activity.add(new ActivityDto("RESUME_UPLOADED", "Uploaded " + r.getFileName(), r.getUploadedAt()));
            List<Recommendation> l = recMap.get(r.getId());
            if (!l.isEmpty()) activity.add(new ActivityDto("JOB_ANALYSIS", "Matched " + r.getFileName() + " against " + l.size() + " jobs", l.get(0).getCreatedAt()));
        }
        List<SavedJob> sj = saved.findByUser(uid);
        Map<Long, Job> jm = jobs.findAllById(sj.stream().map(s -> s.getId().getJobId()).toList()).stream().collect(Collectors.toMap(Job::getId, j -> j));
        for (SavedJob s : sj) { Job j = jm.get(s.getId().getJobId()); if (j != null) activity.add(new ActivityDto("JOB_SAVED", "Saved job: " + j.getTitle(), s.getSavedAt())); }
        List<ActivityDto> recent = activity.stream().filter(a -> a.at() != null)
            .sorted(Comparator.comparing(ActivityDto::at).reversed()).limit(8).toList();

        return new DashboardDto(rs.size(), latest == null ? null : score(latest), latest == null ? null : latest.getFileName(), totalSkills, positive.size(),
            rs.stream().limit(5).map(r -> new ResumeDto(r.getId(), r.getFileName(), r.getStatus().name(), r.getUploadedAt(), r.getSkills().size(), null)).toList(),
            top, recent, cats);
    }

    @Transactional(readOnly = true)
    public List<HistoryItemDto> history(String email) {
        List<HistoryItemDto> out = new ArrayList<>();
        for (Resume r : resumes.findByUserIdOrderByUploadedAtDesc(uid(email))) {
            List<Recommendation> l = activeRecs(r.getId());
            Recommendation best = l.isEmpty() ? null : l.get(0);
            out.add(new HistoryItemDto(r.getId(), r.getFileName(), r.getStatus().name(), r.getUploadedAt(), r.getSkills().size(),
                r.getStatus() == ResumeStatus.ANALYZED ? score(r) : null, best == null ? null : best.getCreatedAt(),
                best == null ? null : best.getMatchScore().doubleValue(), best == null ? null : best.getJob().getTitle()));
        }
        return out;
    }

    private List<Recommendation> activeRecs(Long resumeId) {
        return recs.findByResumeIdOrderByMatchScoreDesc(resumeId).stream().filter(x -> x.getJob().isActive()).toList();
    }

    private int score(Resume r) {
        List<SkillDto> sk = r.getSkills().stream().map(s -> new SkillDto(s.getId(), s.getName(), s.getCategory())).toList();
        return analysis.analyze(r.getId(), r.getFileName(), r.getStatus().name(), r.getExtractedText(), sk).overallScore();
    }

    private Long uid(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required")).getId();
    }
}
