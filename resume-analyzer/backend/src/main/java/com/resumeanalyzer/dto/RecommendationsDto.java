package com.resumeanalyzer.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RecommendationsDto(Long resumeId, String fileName, boolean analyzed, LocalDateTime analyzedAt, int resumeSkillCount,
    String formula, String disclaimer, List<RecommendationDto> items) {}
