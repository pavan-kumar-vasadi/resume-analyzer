package com.resumeanalyzer.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record JobRequest(
    @NotBlank(message = "Title is required") @Size(max = 150) String title,
    @NotBlank(message = "Company name is required") @Size(max = 150) String companyName,
    @NotBlank(message = "Description is required") @Size(max = 10000) String description,
    @Size(max = 120) String location,
    @NotBlank(message = "Employment type is required")
    @Pattern(regexp = "^(Full-time|Part-time|Internship|Contract)$", message = "Invalid employment type") String employmentType,
    @NotBlank(message = "Experience level is required")
    @Pattern(regexp = "^(Entry|Mid|Senior)$", message = "Invalid experience level") String experienceLevel,
    @Min(value = 0, message = "Minimum experience cannot be negative") @Max(50) Integer minExperienceYears,
    @Size(max = 80) String salaryRange,
    @Size(max = 500) @Pattern(regexp = "^$|^https?://\\S+$", message = "Application URL must start with http:// or https://") String applicationUrl,
    List<Long> requiredSkillIds, List<Long> preferredSkillIds, Boolean active) {}
