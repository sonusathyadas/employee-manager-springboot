package com.company.employeemanager.service.impl;

import com.company.employeemanager.dto.request.CreateEmployeeRequest;
import com.company.employeemanager.dto.request.UpdateEmployeeRequest;
import com.company.employeemanager.dto.response.EmployeeResponse;
import com.company.employeemanager.entity.Employee;
import com.company.employeemanager.entity.EmployeeStatus;
import com.company.employeemanager.exception.DuplicateEmailException;
import com.company.employeemanager.exception.EmployeeNotFoundException;
import com.company.employeemanager.mapper.EmployeeMapper;
import com.company.employeemanager.repository.EmployeeRepository;
import com.company.employeemanager.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link EmployeeService} containing all business logic.
 * All write operations are wrapped in transactions; reads use read-only transactions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        log.info("Creating employee with email: {}", request.email());

        if (employeeRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        var employee = employeeMapper.toEntity(request);
        var saved = employeeRepository.save(employee);

        log.info("Employee created successfully with ID: {}", saved.getId());
        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        log.info("Fetching employee with ID: {}", id);
        return employeeMapper.toResponse(findEmployeeById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByEmail(String email) {
        log.info("Fetching employee by email");
        var employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "Employee not found with email: " + email));
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        log.info("Fetching all employees — page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return employeeRepository.findAll(pageable)
                .map(employeeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getEmployeesByDepartment(String department, Pageable pageable) {
        log.info("Fetching employees for department: {}", department);
        return employeeRepository.findByDepartment(department, pageable)
                .map(employeeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getEmployeesByStatus(EmployeeStatus status, Pageable pageable) {
        log.info("Fetching employees with status: {}", status);
        return employeeRepository.findByStatus(status, pageable)
                .map(employeeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> searchEmployees(String search, Pageable pageable) {
        log.info("Searching employees with term: {}", search);
        return employeeRepository.searchEmployees(search, pageable)
                .map(employeeMapper::toResponse);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request) {
        log.info("Updating employee with ID: {}", id);

        var employee = findEmployeeById(id);

        if (request.email() != null &&
                employeeRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateEmailException(request.email());
        }

        employeeMapper.updateEntity(request, employee);
        var updated = employeeRepository.save(employee);

        log.info("Employee updated successfully: {}", id);
        return employeeMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        log.info("Soft-deleting employee with ID: {}", id);

        var employee = findEmployeeById(id);
        employee.setStatus(EmployeeStatus.TERMINATED);
        employeeRepository.save(employee);

        log.info("Employee {} marked as TERMINATED", id);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployeeStatus(Long id, EmployeeStatus status) {
        log.info("Updating status to {} for employee ID: {}", status, id);

        var employee = findEmployeeById(id);
        employee.setStatus(status);
        var updated = employeeRepository.save(employee);

        log.info("Employee {} status updated to {}", id, status);
        return employeeMapper.toResponse(updated);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Retrieves an {@link Employee} by ID or throws {@link EmployeeNotFoundException}.
     *
     * @param id the employee's primary key.
     * @return the found employee entity.
     */
    private Employee findEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "Employee not found with ID: " + id));
    }
}
