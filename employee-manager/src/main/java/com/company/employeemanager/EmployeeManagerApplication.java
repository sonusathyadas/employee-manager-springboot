package com.company.employeemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main entry point for the Employee Manager application.
 * JPA Auditing is enabled here to populate {@code createdAt} and {@code updatedAt}
 * fields automatically on all auditable entities.
 */
@SpringBootApplication
@EnableJpaAuditing
public class EmployeeManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagerApplication.class, args);
    }
}
