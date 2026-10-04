package com.resumeanalyzer.dto;

import java.util.List;

public record AdminDashboardDto(long totalUsers, long totalAdmins, long totalResumes, long activeJobs, long totalJobs, long analyzedResumes,
    List<DayCountDto> registrationsByDay, List<LabelCountDto> topSkills, List<LabelCountDto> resumesByStatus, List<LabelCountDto> jobsByLevel,
    List<UserDto> recentUsers, List<AdminResumeDto> recentResumes, List<AdminAnalysisDto> recentAnalyses) {}
