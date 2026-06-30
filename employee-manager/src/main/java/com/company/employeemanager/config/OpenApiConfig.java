package com.company.employeemanager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the SpringDoc OpenAPI specification for Swagger UI,
 * including a ****** security scheme so the Swagger UI can include
 * the {@code Authorization} header in test requests.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    /**
     * Builds the {@link OpenAPI} bean with application metadata and JWT security scheme.
     *
     * @return the configured OpenAPI instance.
     */
    @Bean
    public OpenAPI employeeManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Employee Manager API")
                        .description("REST API for managing employee profiles. Secured with JWT ******")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
