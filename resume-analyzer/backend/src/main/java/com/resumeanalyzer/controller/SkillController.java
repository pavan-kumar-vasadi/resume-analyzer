package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.SkillDto;
import com.resumeanalyzer.repository.SkillRepository;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skills")
public class SkillController {
    private final SkillRepository skills;
    public SkillController(SkillRepository skills) { this.skills = skills; }

    @GetMapping
    public List<SkillDto> all() { return skills.findAll().stream().map(s -> new SkillDto(s.getId(), s.getName(), s.getCategory())).toList(); }
}
