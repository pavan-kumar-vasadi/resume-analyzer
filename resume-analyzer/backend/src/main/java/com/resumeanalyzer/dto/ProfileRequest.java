package com.resumeanalyzer.dto;

import jakarta.validation.constraints.*;

public record ProfileRequest(@NotBlank(message = "Full name is required") @Size(max = 100, message = "Name is too long") String fullName) {}
