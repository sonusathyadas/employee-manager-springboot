package com.company.employeemanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration placeholder.
 * All requests are permitted and the session is stateless to be JWT-ready.
 * JWT filter chain and role-based rules will replace this in a later step.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures a stateless, permit-all filter chain suitable for the pre-JWT development phase.
     *
     * @param http the {@link HttpSecurity} builder provided by Spring Security.
     * @return the built {@link SecurityFilterChain}.
     * @throws Exception if configuration fails.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
