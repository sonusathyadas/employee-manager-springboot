package com.company.employeemanager.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response returned by the {@link GlobalExceptionHandler}.
 *
 * @param status      HTTP status code.
 * @param error       Short error category label.
 * @param message     Human-readable error description.
 * @param timestamp   When the error occurred.
 * @param path        The request URI that triggered the error.
 * @param fieldErrors List of field-level validation errors, or {@code null} if not applicable.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,

        String path,
        List<FieldError> fieldErrors
) {

    /**
     * Represents a single field-level validation error.
     *
     * @param field   The name of the invalid field.
     * @param message The validation failure message for that field.
     */
    public record FieldError(String field, String message) {}
}
