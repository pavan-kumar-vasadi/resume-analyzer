package com.resumeanalyzer.dto;

import java.util.List;

public record JobSkillsDto(List<SkillDto> required, List<SkillDto> preferred) {}
