package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.*;
import com.resumeanalyzer.service.JobService;
import com.resumeanalyzer.util.AuthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService service;
    public JobController(JobService service) { this.service = service; }

    @GetMapping({"", "/search"})
    public PageDto<JobDto> search(@RequestParam(required = false) String q, @RequestParam(required = false) String location,
            @RequestParam(required = false) String experienceLevel, @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String skill, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, Authentication a) {
        return service.search(new JobFilter(q, location, experienceLevel, employmentType, skill), page, size, AuthUtil.emailOrNull(a), true);
    }

    @GetMapping("/{id}") public JobDto get(@PathVariable Long id, Authentication a) { return service.get(id, AuthUtil.emailOrNull(a), false); }
    @GetMapping("/{id}/skills") public JobSkillsDto skills(@PathVariable Long id) { return service.skillsOf(id); }
}
