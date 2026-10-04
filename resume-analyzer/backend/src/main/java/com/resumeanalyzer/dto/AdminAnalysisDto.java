package com.resumeanalyzer.dto;

import java.time.LocalDateTime;

/** bestScore / bestJobTitle are null when the resume has no stored recommendation rows. */
public record AdminAnalysisDto(Long resumeId, String fileName, String userName, int jobsMatched, Double bestScore, String bestJobTitle, LocalDateTime analyzedAt) {}
