package com.company.employeemanager.entity;

/**
 * Enumeration of application roles assigned to users.
 * Role names follow the Spring Security convention of prefixing with {@code ROLE_}.
 */
public enum Role {
    /** Standard application user — can read employee data. */
    ROLE_USER,
    /** Administrative user — can create, update, and delete employee records. */
    ROLE_ADMIN
}
