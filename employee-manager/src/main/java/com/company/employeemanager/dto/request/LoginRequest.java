package com.company.employeemanager.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for authenticating an existing user and obtaining a JWT token.
 */
public record LoginRequest(

        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {}
