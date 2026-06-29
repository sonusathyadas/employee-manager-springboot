package com.company.employeemanager.exception;

/**
 * Thrown when an attempt is made to register or update an employee
 * with an email address that is already in use by another employee.
 */
public class DuplicateEmailException extends RuntimeException {

    /**
     * Constructs a new exception with a descriptive message.
     *
     * @param email the duplicate email address that caused the conflict.
     */
    public DuplicateEmailException(String email) {
        super("Email already in use: " + email);
    }
}
