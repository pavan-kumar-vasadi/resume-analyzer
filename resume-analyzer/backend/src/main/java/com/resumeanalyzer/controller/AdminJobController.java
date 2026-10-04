package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.*;
import com.resumeanalyzer.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Restricted to ROLE_ADMIN by the /api/admin/** rule in SecurityConfig.
@RestController
@RequestMapping("/api/admin/jobs")
public class AdminJobController {
    private final JobService service;
    public AdminJobController(JobService service) { this.service = service; }

    @GetMapping
    public PageDto<JobDto> list(@RequestParam(required = false) String q, @RequestParam(required = false) String location,
            @RequestParam(required = false) String experienceLevel, @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String skill, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return service.search(new JobFilter(q, location, experienceLevel, employmentType, skill), page, size, null, false);
    }

    @GetMapping("/{id}") public JobDto get(@PathVariable Long id) { return service.get(id, null, true); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public JobDto create(@Valid @RequestBody JobRequest r) { return service.create(r); }

    @PutMapping("/{id}") public JobDto update(@PathVariable Long id, @Valid @RequestBody JobRequest r) { return service.update(id, r); }

    @PatchMapping("/{id}/active") public JobDto active(@PathVariable Long id, @RequestParam boolean value) { return service.setActive(id, value); }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
