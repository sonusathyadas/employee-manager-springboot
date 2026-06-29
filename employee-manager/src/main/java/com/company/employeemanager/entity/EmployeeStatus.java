package com.company.employeemanager.entity;

/**
 * Represents the current employment status of an employee.
 */
public enum EmployeeStatus {
    /** Employee is actively working. */
    ACTIVE,
    /** Employee is inactive but not terminated. */
    INACTIVE,
    /** Employee is on approved leave. */
    ON_LEAVE,
    /** Employee's contract has been terminated. */
    TERMINATED
}
