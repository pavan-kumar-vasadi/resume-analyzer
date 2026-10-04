package com.resumeanalyzer.dto;

import com.resumeanalyzer.entity.User;
import java.time.LocalDateTime;

// Never includes the password hash.
public record UserDto(Long id, String fullName, String email, String role, LocalDateTime createdAt) {
    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getFullName(), u.getEmail(), u.getRole().name(), u.getCreatedAt());
    }
}
