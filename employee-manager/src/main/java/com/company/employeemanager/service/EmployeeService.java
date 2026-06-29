package com.company.employeemanager.service;

import com.company.employeemanager.dto.request.CreateEmployeeRequest;
import com.company.employeemanager.dto.request.UpdateEmployeeRequest;
import com.company.employeemanager.dto.response.EmployeeResponse;
import com.company.employeemanager.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service contract for all employee management operations.
 * Implementations must enforce business rules such as email uniqueness
 * and soft-delete semantics.
 */
public interface EmployeeService {

    /**
     * Creates a new employee record.
     *
     * @param request the validated creation request.
     * @return the persisted employee as a response DTO.
     * @throws com.company.employeemanager.exception.DuplicateEmailException if the email is already in use.
     */
    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    /**
     * Retrieves an employee by their unique ID.
     *
     * @param id the employee's primary key.
     * @return the employee response DTO.
     * @throws com.company.employeemanager.exception.EmployeeNotFoundException if no employee exists with that ID.
     */
    EmployeeResponse getEmployeeById(Long id);

    /**
     * Retrieves an employee by their unique email address.
     *
     * @param email the employee's email.
     * @return the employee response DTO.
     * @throws com.company.employeemanager.exception.EmployeeNotFoundException if no employee exists with that email.
     */
    EmployeeResponse getEmployeeByEmail(String email);

    /**
     * Returns a paginated list of all employees.
     *
     * @param pageable pagination and sorting parameters.
     * @return a page of employee response DTOs.
     */
    Page<EmployeeResponse> getAllEmployees(Pageable pageable);

    /**
     * Returns a paginated list of employees in the specified department.
     *
     * @param department the department name.
     * @param pageable   pagination and sorting parameters.
     * @return a page of employee response DTOs.
     */
    Page<EmployeeResponse> getEmployeesByDepartment(String department, Pageable pageable);

    /**
     * Returns a paginated list of employees matching the specified status.
     *
     * @param status   the {@link EmployeeStatus} to filter by.
     * @param pageable pagination and sorting parameters.
     * @return a page of employee response DTOs.
     */
    Page<EmployeeResponse> getEmployeesByStatus(EmployeeStatus status, Pageable pageable);

    /**
     * Searches employees by first name, last name, or email (case-insensitive, partial match).
     *
     * @param search   the search term.
     * @param pageable pagination and sorting parameters.
     * @return a page of matching employee response DTOs.
     */
    Page<EmployeeResponse> searchEmployees(String search, Pageable pageable);

    /**
     * Updates an employee's details. Non-null fields in the request are applied (PATCH semantics).
     *
     * @param id      the employee's primary key.
     * @param request the update payload.
     * @return the updated employee response DTO.
     * @throws com.company.employeemanager.exception.EmployeeNotFoundException if no employee exists with that ID.
     * @throws com.company.employeemanager.exception.DuplicateEmailException   if the new email is already in use.
     */
    EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request);

    /**
     * Soft-deletes an employee by setting their status to {@link EmployeeStatus#TERMINATED}.
     * No rows are physically removed from the database.
     *
     * @param id the employee's primary key.
     * @throws com.company.employeemanager.exception.EmployeeNotFoundException if no employee exists with that ID.
     */
    void deleteEmployee(Long id);

    /**
     * Updates only the status of an existing employee.
     *
     * @param id     the employee's primary key.
     * @param status the new {@link EmployeeStatus}.
     * @return the updated employee response DTO.
     * @throws com.company.employeemanager.exception.EmployeeNotFoundException if no employee exists with that ID.
     */
    EmployeeResponse updateEmployeeStatus(Long id, EmployeeStatus status);
}
