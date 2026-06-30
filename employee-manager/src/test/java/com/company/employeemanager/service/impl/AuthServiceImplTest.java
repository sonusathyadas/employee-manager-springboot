package com.company.employeemanager.service.impl;

import com.company.employeemanager.dto.request.LoginRequest;
import com.company.employeemanager.dto.request.RegisterRequest;
import com.company.employeemanager.entity.AppUser;
import com.company.employeemanager.entity.Role;
import com.company.employeemanager.exception.DuplicateUsernameException;
import com.company.employeemanager.repository.AppUserRepository;
import com.company.employeemanager.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for {@link AuthServiceImpl}.
 * Uses Mockito to mock all external dependencies including the authentication manager,
 * password encoder, JWT utility, and user repository.
 * Follows the Arrange-Act-Assert pattern.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    // -------------------------------------------------------------------------
    // register tests
    // -------------------------------------------------------------------------

    @Test
    void register_whenUsernameDoesNotExist_returnsAuthResponseWithToken() {
        // Arrange
        var request = new RegisterRequest("newuser", "password123");
        var savedUser = AppUser.builder()
                .id(1L)
                .username("newuser")
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .build();

        given(appUserRepository.existsByUsername("newuser")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encoded-password");
        given(appUserRepository.save(any(AppUser.class))).willReturn(savedUser);
        given(jwtUtil.generateToken(savedUser)).willReturn("mock-jwt-token");

        // Act
        var result = authService.register(request);

        // Assert
        assertThat(result.token()).isEqualTo("mock-jwt-token");
        assertThat(result.username()).isEqualTo("newuser");
        assertThat(result.role()).isEqualTo(Role.ROLE_USER.name());
    }

    @Test
    void register_whenUsernameDoesNotExist_savesUserWithEncodedPassword() {
        // Arrange
        var request = new RegisterRequest("newuser", "password123");
        var savedUser = AppUser.builder()
                .id(1L)
                .username("newuser")
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .build();

        given(appUserRepository.existsByUsername("newuser")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encoded-password");
        given(appUserRepository.save(any(AppUser.class))).willReturn(savedUser);
        given(jwtUtil.generateToken(any())).willReturn("mock-jwt-token");

        // Act
        authService.register(request);

        // Assert
        then(passwordEncoder).should().encode("password123");
        then(appUserRepository).should().save(argThat(user ->
                "newuser".equals(user.getUsername()) &&
                "encoded-password".equals(user.getPassword()) &&
                Role.ROLE_USER.equals(user.getRole())
        ));
    }

    @Test
    void register_whenUsernameAlreadyExists_throwsDuplicateUsernameException() {
        // Arrange
        var request = new RegisterRequest("existinguser", "password123");
        given(appUserRepository.existsByUsername("existinguser")).willReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessageContaining("existinguser");

        then(appUserRepository).should(never()).save(any());
        then(jwtUtil).should(never()).generateToken(any());
    }

    // -------------------------------------------------------------------------
    // login tests
    // -------------------------------------------------------------------------

    @Test
    void login_whenCredentialsAreValid_returnsAuthResponseWithToken() {
        // Arrange
        var request = new LoginRequest("validuser", "correctpassword");
        var appUser = AppUser.builder()
                .id(1L)
                .username("validuser")
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .build();
        var authToken = new UsernamePasswordAuthenticationToken(appUser, null, appUser.getAuthorities());

        given(authenticationManager.authenticate(any())).willReturn(authToken);
        given(jwtUtil.generateToken(appUser)).willReturn("mock-jwt-token");

        // Act
        var result = authService.login(request);

        // Assert
        assertThat(result.token()).isEqualTo("mock-jwt-token");
        assertThat(result.username()).isEqualTo("validuser");
        assertThat(result.role()).isEqualTo(Role.ROLE_USER.name());
    }

    @Test
    void login_whenCredentialsAreValid_delegatesAuthenticationToAuthManager() {
        // Arrange
        var request = new LoginRequest("validuser", "correctpassword");
        var appUser = AppUser.builder()
                .id(1L)
                .username("validuser")
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .build();
        var authToken = new UsernamePasswordAuthenticationToken(appUser, null, appUser.getAuthorities());

        given(authenticationManager.authenticate(any())).willReturn(authToken);
        given(jwtUtil.generateToken(appUser)).willReturn("mock-jwt-token");

        // Act
        authService.login(request);

        // Assert
        then(authenticationManager).should().authenticate(
                argThat(a -> "validuser".equals(a.getPrincipal()) &&
                             "correctpassword".equals(a.getCredentials()))
        );
    }

    @Test
    void login_whenCredentialsAreInvalid_throwsBadCredentialsException() {
        // Arrange
        var request = new LoginRequest("validuser", "wrongpassword");
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        then(jwtUtil).should(never()).generateToken(any());
    }
}
