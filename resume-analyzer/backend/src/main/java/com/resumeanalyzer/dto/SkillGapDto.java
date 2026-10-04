package com.resumeanalyzer.dto;
public record SkillGapDto(String skill, String category, int missingInJobs, int percentOfTargets, String suggestion) {}
