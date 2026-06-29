package com.company.employeemanager.dto.response;

import com.company.employeemanager.entity.EmployeeStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Read-only response DTO representing a full employee profile.
 * Date and time fields are serialized using ISO-8601 format.
 */
public record EmployeeResponse(

        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String department,
        String jobTitle,
        BigDecimal salary,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate hireDate,

        EmployeeStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {}
