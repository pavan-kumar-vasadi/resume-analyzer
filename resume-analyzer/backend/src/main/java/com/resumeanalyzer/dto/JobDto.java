package com.resumeanalyzer.dto;

import java.time.LocalDateTime;
import java.util.List;

public record JobDto(Long id, String title, String companyName, String description, String location, String employmentType,
    String experienceLevel, int minExperienceYears, String salaryRange, String applicationUrl, boolean demo, boolean active,
    LocalDateTime createdAt, List<SkillDto> requiredSkills, List<SkillDto> preferredSkills, boolean saved) {}
