package com.resumeanalyzer.dto;

import java.time.LocalDateTime;

public record AdminResumeDto(Long id, String fileName, String userName, String status, int skillCount, LocalDateTime uploadedAt) {}
