package com.company.employeemanager.security;

import com.company.employeemanager.entity.AppUser;
import com.company.employeemanager.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link JwtUtil}.
 * Verifies token generation, username extraction, and token validation logic.
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    /** A 32-character secret used for HMAC-SHA256 signing in tests. */
    private static final String TEST_SECRET = "test-secret-key-min-32-chars-long!!";

    /** 24-hour validity for tokens generated in tests. */
    private static final long TEST_EXPIRATION_MS = 86_400_000L;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtUtil jwtUtil;

    /** Sample user whose details are used across all token tests. */
    private AppUser testUser;

    @BeforeEach
    void setUp() {
        // Configure the mocked JwtProperties to return the test secret and expiration
        given(jwtProperties.getSecret()).willReturn(TEST_SECRET);
        given(jwtProperties.getExpirationMs()).willReturn(TEST_EXPIRATION_MS);

        testUser = AppUser.builder()
                .id(1L)
                .username("testuser")
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();
    }

    // -------------------------------------------------------------------------
    // generateToken tests
    // -------------------------------------------------------------------------

    @Test
    void generateToken_returnsNonNullToken() {
        // Act
        String token = jwtUtil.generateToken(testUser);

        // Assert
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void generateToken_returnsCompactJwtFormat() {
        // Act
        String token = jwtUtil.generateToken(testUser);

        // Assert — a compact JWT has exactly three dot-separated segments
        assertThat(token.split("\\.")).hasSize(3);
    }

    // -------------------------------------------------------------------------
    // extractUsername tests
    // -------------------------------------------------------------------------

    @Test
    void extractUsername_returnsCorrectUsernameFromToken() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertThat(username).isEqualTo("testuser");
    }

    // -------------------------------------------------------------------------
    // isTokenValid tests
    // -------------------------------------------------------------------------

    @Test
    void isTokenValid_withValidTokenAndMatchingUser_returnsTrue() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        // Act
        boolean valid = jwtUtil.isTokenValid(token, testUser);

        // Assert
        assertThat(valid).isTrue();
    }

    @Test
    void isTokenValid_withValidTokenButDifferentUser_returnsFalse() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        var differentUser = AppUser.builder()
                .id(2L)
                .username("otheruser")
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        // Act
        boolean valid = jwtUtil.isTokenValid(token, differentUser);

        // Assert
        assertThat(valid).isFalse();
    }

    @Test
    void isTokenValid_withExpiredToken_throwsExpiredJwtException() {
        // Arrange — generate a token that expires immediately (1 ms)
        given(jwtProperties.getExpirationMs()).willReturn(1L);
        String token = jwtUtil.generateToken(testUser);

        // Brief sleep to ensure the token has expired before validation
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act & Assert — jjwt 0.9.x throws ExpiredJwtException when parsing expired tokens
        org.junit.jupiter.api.Assertions.assertThrows(
                io.jsonwebtoken.ExpiredJwtException.class,
                () -> jwtUtil.isTokenValid(token, testUser)
        );
    }
}
