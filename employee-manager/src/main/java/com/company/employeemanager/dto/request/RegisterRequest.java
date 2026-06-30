package com.company.employeemanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for registering a new application user.
 */
public record RegisterRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 150, message = "Username must be between 3 and 150 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {}
