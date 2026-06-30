package com.company.employeemanager.service;

import com.company.employeemanager.dto.request.LoginRequest;
import com.company.employeemanager.dto.request.RegisterRequest;
import com.company.employeemanager.dto.response.AuthResponse;

/**
 * Service contract for user registration and authentication operations.
 */
public interface AuthService {

    /**
     * Registers a new user with the ROLE_USER role and returns a JWT token.
     *
     * @param request the registration payload containing username and password.
     * @return an {@link AuthResponse} containing the generated JWT token.
     * @throws com.company.employeemanager.exception.DuplicateUsernameException if the username is already taken.
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates an existing user with their credentials and returns a JWT token.
     *
     * @param request the login payload containing username and password.
     * @return an {@link AuthResponse} containing the generated JWT token.
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid.
     */
    AuthResponse login(LoginRequest request);
}
