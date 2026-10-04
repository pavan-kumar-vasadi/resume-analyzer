package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.AdminDashboardDto;
import com.resumeanalyzer.dto.UserDto;
import com.resumeanalyzer.repository.UserRepository;
import com.resumeanalyzer.service.AdminDashboardService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository users;
    private final AdminDashboardService dashboard;
    public AdminController(UserRepository users, AdminDashboardService dashboard) { this.users = users; this.dashboard = dashboard; }

    // URL rule in SecurityConfig restricts /api/admin/** to ROLE_ADMIN.
    @GetMapping("/users")
    public List<UserDto> users() { return users.findAll().stream().map(UserDto::from).toList(); }

    @GetMapping("/dashboard")
    public AdminDashboardDto dashboard() { return dashboard.dashboard(); }
}
