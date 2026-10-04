package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.JobDto;
import com.resumeanalyzer.service.JobService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/saved-jobs")
public class SavedJobController {
    private final JobService service;
    public SavedJobController(JobService service) { this.service = service; }

    @PostMapping("/{jobId}") @ResponseStatus(HttpStatus.CREATED)
    public void save(@PathVariable Long jobId, Authentication a) { service.save(jobId, a.getName()); }

    @GetMapping public List<JobDto> list(Authentication a) { return service.savedJobs(a.getName()); }

    @DeleteMapping("/{jobId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long jobId, Authentication a) { service.unsave(jobId, a.getName()); }
}
