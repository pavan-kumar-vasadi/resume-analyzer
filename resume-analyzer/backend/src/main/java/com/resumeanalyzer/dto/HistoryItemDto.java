package com.resumeanalyzer.dto;

import java.time.LocalDateTime;

public record HistoryItemDto(Long resumeId, String fileName, String status, LocalDateTime uploadedAt, int skillCount,
    Integer completenessScore, LocalDateTime jobAnalysisAt, Double bestMatchScore, String bestMatchTitle) {}
