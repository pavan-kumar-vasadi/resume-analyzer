package com.resumeanalyzer.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RecommendationDto(Long jobId, String title, String companyName, String location, String employmentType,
    String experienceLevel, boolean demo, boolean saved, double matchScore, Integer requiredCoverage,
    List<String> matchedSkills, List<String> missingSkills, List<String> preferredMatched, List<String> preferredMissing,
    List<LearningTopicDto> learningTopics, LocalDateTime analyzedAt, String note) {}
