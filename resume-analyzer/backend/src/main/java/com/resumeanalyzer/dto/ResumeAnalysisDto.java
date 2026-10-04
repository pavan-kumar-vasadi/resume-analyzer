package com.resumeanalyzer.dto;

import java.util.List;

public record ResumeAnalysisDto(Long resumeId, String fileName, String status, String message,
    String candidateName, String email, String phone, List<SkillDto> skills,
    List<String> educationKeywords, List<String> experienceKeywords,
    int overallScore, List<SectionScoreDto> sections, List<String> missingSections,
    List<String> suggestions, String textPreview, String extractedText, String scoreNote) {}
