package com.resumeanalyzer.dto;

import java.time.LocalDateTime;

public record ActivityDto(String type, String text, LocalDateTime at) {}
