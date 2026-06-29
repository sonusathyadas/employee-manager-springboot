package com.company.employeemanager.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating a new employee.
 * All fields are required and validated via Bean Validation.
 */
public record CreateEmployeeRequest(

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phone,

        @NotBlank(message = "Department is required")
        @Size(max = 100, message = "Department must not exceed 100 characters")
        String department,

        @NotBlank(message = "Job title is required")
        @Size(max = 100, message = "Job title must not exceed 100 characters")
        String jobTitle,

        @NotNull(message = "Salary is required")
        @Positive(message = "Salary must be a positive value")
        BigDecimal salary,

        @NotNull(message = "Hire date is required")
        @PastOrPresent(message = "Hire date must be today or in the past")
        LocalDate hireDate
) {}
