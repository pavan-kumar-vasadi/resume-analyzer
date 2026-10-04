package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.*;
import com.resumeanalyzer.service.RecommendationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService service;
    public RecommendationController(RecommendationService service) { this.service = service; }

    @PostMapping("/analyze/{resumeId}")
    public RecommendationsDto analyze(@PathVariable Long resumeId, Authentication a) { return service.analyze(resumeId, a.getName()); }

    @GetMapping("/{resumeId}")
    public RecommendationsDto get(@PathVariable Long resumeId, Authentication a) { return service.get(resumeId, a.getName()); }

    @GetMapping("/{resumeId}/skill-gaps")
    public SkillGapsDto gaps(@PathVariable Long resumeId, @RequestParam(defaultValue = "top") String scope, Authentication a) {
        return service.skillGaps(resumeId, a.getName(), scope);
    }
}
