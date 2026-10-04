package com.resumeanalyzer.dto;

import java.util.List;

public record DashboardDto(int totalResumes, Integer latestScore, String latestResumeName, int totalSkills, int recommendedJobs,
    List<ResumeDto> recentResumes, List<TopMatchDto> topMatches, List<ActivityDto> recentActivity, List<CategoryCountDto> skillsByCategory) {}
