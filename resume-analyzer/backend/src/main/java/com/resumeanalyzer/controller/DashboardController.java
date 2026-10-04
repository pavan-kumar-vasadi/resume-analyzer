package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.*;
import com.resumeanalyzer.service.DashboardService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final DashboardService service;
    public DashboardController(DashboardService service) { this.service = service; }

    @GetMapping("/dashboard") public DashboardDto dashboard(Authentication a) { return service.dashboard(a.getName()); }
    @GetMapping("/history") public List<HistoryItemDto> history(Authentication a) { return service.history(a.getName()); }
}
