package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.*;
import com.resumeanalyzer.service.ResumeService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    private final ResumeService service;
    public ResumeController(ResumeService service) { this.service = service; }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.CREATED)
    public ResumeDto upload(@RequestParam("file") MultipartFile file, Authentication a) { return service.upload(file, a.getName()); }

    @GetMapping("/my") public List<ResumeDto> my(Authentication a) { return service.mine(a.getName()); }
    @GetMapping("/{id}") public ResumeDto get(@PathVariable Long id, Authentication a) { return service.get(id, a.getName()); }
    @GetMapping("/{id}/analysis") public ResumeAnalysisDto analysis(@PathVariable Long id, Authentication a) { return service.analysis(id, a.getName()); }
    @GetMapping("/{id}/skills") public List<SkillDto> skills(@PathVariable Long id, Authentication a) { return service.skills(id, a.getName()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication a) { service.delete(id, a.getName()); }
}
