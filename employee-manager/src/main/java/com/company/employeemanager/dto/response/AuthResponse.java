package com.company.employeemanager.dto.response;

/**
 * Response DTO returned after a successful login or registration.
 * Contains the JWT bearer token and basic user information.
 */
public record AuthResponse(
        /** The JWT bearer token to include in subsequent requests. */
        String token,
        /** The authenticated user's username. */
        String username,
        /** The role assigned to the user. */
        String role
) {}
