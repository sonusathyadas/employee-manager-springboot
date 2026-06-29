package com.company.employeemanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the SpringDoc OpenAPI specification for Swagger UI.
 * Security schemes are intentionally omitted — JWT authentication will be wired in a later step.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Builds the {@link OpenAPI} bean with application metadata.
     *
     * @return the configured OpenAPI instance.
     */
    @Bean
    public OpenAPI employeeManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Employee Manager API")
                        .description("REST API for managing employee profiles. JWT auth will be added.")
                        .version("1.0.0"));
    }
}
