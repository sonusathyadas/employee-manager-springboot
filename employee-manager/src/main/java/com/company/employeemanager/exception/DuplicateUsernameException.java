package com.company.employeemanager.exception;

/**
 * Thrown when an attempt is made to register a new user with a username
 * that is already taken by another account.
 */
public class DuplicateUsernameException extends RuntimeException {

    /**
     * Constructs a new exception with a descriptive message.
     *
     * @param username the duplicate username that caused the conflict.
     */
    public DuplicateUsernameException(String username) {
        super("Username already in use: " + username);
    }
}
