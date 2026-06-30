package com.company.employeemanager.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Strongly-typed configuration properties for JWT token generation.
 * Values are bound from the {@code jwt.*} namespace in {@code application.yml}.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * HMAC-SHA256 signing secret. Must be at least 32 characters in production.
     * Injected via environment variable {@code JWT_SECRET} in prod profile.
     */
    @NotBlank(message = "JWT secret must not be blank")
    private String secret;

    /**
     * Token validity in milliseconds. Defaults to 86400000 (24 hours).
     */
    @Positive(message = "JWT expiration must be a positive value")
    private long expirationMs = 86_400_000L;
}
