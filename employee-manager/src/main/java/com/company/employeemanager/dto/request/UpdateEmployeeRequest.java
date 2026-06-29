package com.company.employeemanager.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for updating an existing employee.
 * All fields are optional — only non-null fields are applied (PATCH semantics).
 */
public record UpdateEmployeeRequest(

        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @Email(message = "Email must be a valid email address")
        String email,

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phone,

        @Size(max = 100, message = "Department must not exceed 100 characters")
        String department,

        @Size(max = 100, message = "Job title must not exceed 100 characters")
        String jobTitle,

        @Positive(message = "Salary must be a positive value")
        BigDecimal salary,

        @PastOrPresent(message = "Hire date must be today or in the past")
        LocalDate hireDate
) {}
