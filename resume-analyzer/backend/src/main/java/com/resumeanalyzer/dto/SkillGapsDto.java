package com.resumeanalyzer.dto;

import java.util.List;

public record SkillGapsDto(String scope, int targetJobCount, List<SkillGapDto> items, String note) {}
