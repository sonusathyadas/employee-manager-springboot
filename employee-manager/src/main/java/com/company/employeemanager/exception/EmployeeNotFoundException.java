package com.company.employeemanager.exception;

/**
 * Thrown when an employee is not found by ID or email.
 */
public class EmployeeNotFoundException extends RuntimeException {

    /**
     * Constructs a new exception with a descriptive message.
     *
     * @param message detail about which employee was not found.
     */
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
