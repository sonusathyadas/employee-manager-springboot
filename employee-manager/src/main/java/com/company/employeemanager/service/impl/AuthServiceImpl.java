package com.company.employeemanager.service.impl;

import com.company.employeemanager.dto.request.LoginRequest;
import com.company.employeemanager.dto.request.RegisterRequest;
import com.company.employeemanager.dto.response.AuthResponse;
import com.company.employeemanager.entity.AppUser;
import com.company.employeemanager.entity.Role;
import com.company.employeemanager.exception.DuplicateUsernameException;
import com.company.employeemanager.repository.AppUserRepository;
import com.company.employeemanager.security.JwtUtil;
import com.company.employeemanager.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link AuthService}.
 * Handles user registration (with BCrypt password encoding) and JWT-based login.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * {@inheritDoc}
     *
     * <p>The new user is assigned {@link Role#ROLE_USER} by default.
     * The password is BCrypt-encoded before persistence.</p>
     *
     * @throws DuplicateUsernameException if the username is already taken.
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new DuplicateUsernameException(request.username());
        }

        var user = AppUser.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .build();

        var saved = appUserRepository.save(user);
        log.info("Registered new user: {}", saved.getUsername());

        String token = jwtUtil.generateToken(saved);
        return new AuthResponse(token, saved.getUsername(), saved.getRole().name());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates credential verification to Spring Security's {@link AuthenticationManager}.
     * Throws {@link org.springframework.security.authentication.BadCredentialsException}
     * if the credentials are invalid.</p>
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        var user = (AppUser) auth.getPrincipal();
        log.info("User '{}' logged in successfully", user.getUsername());

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}
