package com.resumeanalyzer.dto;

import java.time.LocalDateTime;

public record ResumeDto(Long id, String fileName, String status, LocalDateTime uploadedAt, int skillCount, String message) {}
