package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.*;
import com.resumeanalyzer.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class UserController {
    private final UserService service;
    public UserController(UserService service) { this.service = service; }

    @PutMapping public UserDto update(@Valid @RequestBody ProfileRequest r, Authentication a) { return service.updateProfile(a.getName(), r); }

    @PutMapping("/password") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void password(@Valid @RequestBody ChangePasswordRequest r, Authentication a) { service.changePassword(a.getName(), r); }
}
