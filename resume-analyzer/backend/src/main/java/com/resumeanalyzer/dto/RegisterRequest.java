package com.resumeanalyzer.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank(message = "Full name is required") @Size(max = 100) String fullName,
    @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") @Size(max = 150) String email,
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Password must contain at least one letter and one digit") String password,
    @NotBlank(message = "Confirm your password") String confirmPassword) {}
