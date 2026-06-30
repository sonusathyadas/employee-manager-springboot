package com.company.employeemanager.controller;

import com.company.employeemanager.dto.request.LoginRequest;
import com.company.employeemanager.dto.request.RegisterRequest;
import com.company.employeemanager.dto.response.AuthResponse;
import com.company.employeemanager.exception.DuplicateUsernameException;
import com.company.employeemanager.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.test.context.support.WithMockUser;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller-layer tests for {@link AuthController}.
 * Uses {@link WebMvcTest} to test the HTTP contract for register and login endpoints.
 * {@link WithMockUser} supplies an authenticated security context so requests pass
 * through the security filter chain to the controller. Endpoint-level security
 * (public vs protected) is verified in integration tests.
 */
@WebMvcTest(AuthController.class)
@WithMockUser
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** Satisfies JPA auditing dependency that is triggered by {@code @EnableJpaAuditing} on the main class. */
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    /** Mocked service — all business logic is tested separately in service-level tests. */
    @MockBean
    private AuthService authService;

    /** Required by {@link com.company.employeemanager.security.JwtAuthenticationFilter}. */
    @MockBean
    private com.company.employeemanager.security.JwtUtil jwtUtil;

    /** Required by {@link com.company.employeemanager.security.JwtAuthenticationFilter}. */
    @MockBean
    private UserDetailsService userDetailsService;

    // -------------------------------------------------------------------------
    // POST /api/v1/auth/register — register
    // -------------------------------------------------------------------------

    @Test
    void register_withValidRequest_returns200WithAuthResponse() throws Exception {
        // Arrange
        var request = new RegisterRequest("newuser", "password123");
        var authResponse = new AuthResponse("mock-jwt-token", "newuser", "ROLE_USER");
        given(authService.register(request)).willReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void register_withBlankUsername_returns400() throws Exception {
        // Arrange — username is blank, which violates @NotBlank
        var request = new RegisterRequest("", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withPasswordTooShort_returns400() throws Exception {
        // Arrange — password has fewer than 8 characters
        var request = new RegisterRequest("validuser", "short");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_whenUsernameAlreadyInUse_returns409() throws Exception {
        // Arrange
        var request = new RegisterRequest("existinguser", "password123");
        given(authService.register(request))
                .willThrow(new DuplicateUsernameException("existinguser"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/auth/login — login
    // -------------------------------------------------------------------------

    @Test
    void login_withValidCredentials_returns200WithAuthResponse() throws Exception {
        // Arrange
        var request = new LoginRequest("validuser", "correctpassword");
        var authResponse = new AuthResponse("mock-jwt-token", "validuser", "ROLE_USER");
        given(authService.login(request)).willReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.username").value("validuser"));
    }

    @Test
    void login_withBlankCredentials_returns400() throws Exception {
        // Arrange — both fields blank violates @NotBlank
        var request = new LoginRequest("", "");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        // Arrange
        var request = new LoginRequest("validuser", "wrongpassword");
        given(authService.login(request))
                .willThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
