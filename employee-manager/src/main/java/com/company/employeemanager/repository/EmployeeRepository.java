package com.company.employeemanager.repository;

import com.company.employeemanager.entity.Employee;
import com.company.employeemanager.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Employee} entities.
 * Provides standard CRUD operations plus custom query methods.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Finds an employee by their unique email address.
     *
     * @param email the email to search for.
     * @return an {@link Optional} containing the employee if found.
     */
    Optional<Employee> findByEmail(String email);

    /**
     * Checks whether an employee with the given email already exists.
     *
     * @param email the email to check.
     * @return {@code true} if an employee with this email exists.
     */
    boolean existsByEmail(String email);

    /**
     * Checks whether an employee with the given email exists, excluding a specific employee ID.
     * Used during update to prevent false-positive duplicate detection.
     *
     * @param email the email to check.
     * @param id    the employee ID to exclude from the check.
     * @return {@code true} if another employee (not the given ID) has this email.
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Returns a paginated list of employees belonging to the given department.
     *
     * @param department the department name to filter by.
     * @param pageable   pagination and sorting parameters.
     * @return a page of matching employees.
     */
    Page<Employee> findByDepartment(String department, Pageable pageable);

    /**
     * Returns a paginated list of employees with the given status.
     *
     * @param status   the {@link EmployeeStatus} to filter by.
     * @param pageable pagination and sorting parameters.
     * @return a page of matching employees.
     */
    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);

    /**
     * Full-text search across first name, last name, and email fields (case-insensitive).
     *
     * @param search   the search term.
     * @param pageable pagination and sorting parameters.
     * @return a page of matching employees.
     */
    @Query("SELECT e FROM Employee e WHERE " +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Employee> searchEmployees(@Param("search") String search, Pageable pageable);
}
