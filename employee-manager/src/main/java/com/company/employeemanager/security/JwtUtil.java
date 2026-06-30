package com.company.employeemanager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

/**
 * Utility component for creating, parsing, and validating JWT tokens.
 * Uses HMAC-SHA256 signing with a configurable secret and expiry from {@link JwtProperties}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    /**
     * Generates a signed JWT token for the given user.
     *
     * @param userDetails the authenticated user whose username becomes the token subject.
     * @return a compact, URL-safe JWT string.
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecret())
                .compact();
    }

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token the JWT string to parse.
     * @return the username stored in the token subject.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validates the token by verifying the username matches and the token has not expired.
     *
     * @param token       the JWT string to validate.
     * @param userDetails the user to validate against.
     * @return {@code true} if the token is valid for the given user.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Checks whether the token's expiration date is in the past.
     *
     * @param token the JWT string.
     * @return {@code true} if the token has expired.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the token.
     *
     * @param token the JWT string.
     * @return the expiration {@link Date}.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic claim extractor using a claims resolver function.
     *
     * @param token          the JWT string.
     * @param claimsResolver a function that maps {@link Claims} to the desired value.
     * @param <T>            the return type of the resolver.
     * @return the extracted claim value.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .setSigningKey(jwtProperties.getSecret())
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }
}
