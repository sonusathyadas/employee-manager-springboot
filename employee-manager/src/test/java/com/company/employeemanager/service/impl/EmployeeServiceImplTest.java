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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for {@link EmployeeServiceImpl}.
 * Uses Mockito to mock the repository and mapper dependencies.
 * Follows the Arrange-Act-Assert pattern.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    // -------------------------------------------------------------------------
    // Test data helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a sample {@link Employee} entity for use in tests.
     */
    private Employee buildEmployee() {
        return Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .department("Engineering")
                .jobTitle("Software Engineer")
                .salary(BigDecimal.valueOf(75000))
                .hireDate(LocalDate.of(2022, 1, 15))
                .status(EmployeeStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Builds a sample {@link EmployeeResponse} DTO from the given employee entity.
     */
    private EmployeeResponse buildEmployeeResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getDepartment(),
                employee.getJobTitle(),
                employee.getSalary(),
                employee.getHireDate(),
                employee.getReportingManager(),
                employee.getStatus(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }

    /**
     * Builds a sample {@link CreateEmployeeRequest} for use in tests.
     */
    private CreateEmployeeRequest buildCreateRequest() {
        return new CreateEmployeeRequest(
                "John", "Doe", "john.doe@example.com",
                "1234567890", "Engineering", "Software Engineer",
                BigDecimal.valueOf(75000), LocalDate.of(2022, 1, 15), null
        );
    }

    // -------------------------------------------------------------------------
    // createEmployee tests
    // -------------------------------------------------------------------------

    @Test
    void createEmployee_whenEmailDoesNotExist_returnsEmployeeResponse() {
        // Arrange
        var request = buildCreateRequest();
        var employee = buildEmployee();
        var expectedResponse = buildEmployeeResponse(employee);

        given(employeeRepository.existsByEmail(request.email())).willReturn(false);
        given(employeeMapper.toEntity(request)).willReturn(employee);
        given(employeeRepository.save(employee)).willReturn(employee);
        given(employeeMapper.toResponse(employee)).willReturn(expectedResponse);

        // Act
        var result = employeeService.createEmployee(request);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
        then(employeeRepository).should().existsByEmail(request.email());
        then(employeeRepository).should().save(employee);
    }

    @Test
    void createEmployee_whenEmailAlreadyExists_throwsDuplicateEmailException() {
        // Arrange
        var request = buildCreateRequest();
        given(employeeRepository.existsByEmail(request.email())).willReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining(request.email());

        then(employeeRepository).should(never()).save(any());
    }

    // -------------------------------------------------------------------------
    // getEmployeeById tests
    // -------------------------------------------------------------------------

    @Test
    void getEmployeeById_whenEmployeeExists_returnsEmployeeResponse() {
        // Arrange
        var employee = buildEmployee();
        var expectedResponse = buildEmployeeResponse(employee);

        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
        given(employeeMapper.toResponse(employee)).willReturn(expectedResponse);

        // Act
        var result = employeeService.getEmployeeById(1L);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getEmployeeById_whenEmployeeDoesNotExist_throwsEmployeeNotFoundException() {
        // Arrange
        given(employeeRepository.findById(99L)).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // getEmployeeByEmail tests
    // -------------------------------------------------------------------------

    @Test
    void getEmployeeByEmail_whenEmployeeExists_returnsEmployeeResponse() {
        // Arrange
        var employee = buildEmployee();
        var expectedResponse = buildEmployeeResponse(employee);
        String email = "john.doe@example.com";

        given(employeeRepository.findByEmail(email)).willReturn(Optional.of(employee));
        given(employeeMapper.toResponse(employee)).willReturn(expectedResponse);

        // Act
        var result = employeeService.getEmployeeByEmail(email);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getEmployeeByEmail_whenEmployeeDoesNotExist_throwsEmployeeNotFoundException() {
        // Arrange
        String email = "unknown@example.com";
        given(employeeRepository.findByEmail(email)).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> employeeService.getEmployeeByEmail(email))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining(email);
    }

    // -------------------------------------------------------------------------
    // getAllEmployees tests
    // -------------------------------------------------------------------------

    @Test
    void getAllEmployees_returnsPaginatedEmployeeResponses() {
        // Arrange
        var employee = buildEmployee();
        var response = buildEmployeeResponse(employee);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Employee> employeePage = new PageImpl<>(List.of(employee), pageable, 1);

        given(employeeRepository.findAll(pageable)).willReturn(employeePage);
        given(employeeMapper.toResponse(employee)).willReturn(response);

        // Act
        var result = employeeService.getAllEmployees(pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).containsExactly(response);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getAllEmployees_whenNoEmployeesExist_returnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        given(employeeRepository.findAll(pageable)).willReturn(Page.empty(pageable));

        // Act
        var result = employeeService.getAllEmployees(pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    // -------------------------------------------------------------------------
    // getEmployeesByDepartment tests
    // -------------------------------------------------------------------------

    @Test
    void getEmployeesByDepartment_returnsPaginatedEmployeeResponses() {
        // Arrange
        String department = "Engineering";
        var employee = buildEmployee();
        var response = buildEmployeeResponse(employee);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Employee> employeePage = new PageImpl<>(List.of(employee), pageable, 1);

        given(employeeRepository.findByDepartment(department, pageable)).willReturn(employeePage);
        given(employeeMapper.toResponse(employee)).willReturn(response);

        // Act
        var result = employeeService.getEmployeesByDepartment(department, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).containsExactly(response);
    }

    // -------------------------------------------------------------------------
    // getEmployeesByStatus tests
    // -------------------------------------------------------------------------

    @Test
    void getEmployeesByStatus_whenActiveStatus_returnsPaginatedEmployeeResponses() {
        // Arrange
        var employee = buildEmployee();
        var response = buildEmployeeResponse(employee);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Employee> employeePage = new PageImpl<>(List.of(employee), pageable, 1);

        given(employeeRepository.findByStatus(EmployeeStatus.ACTIVE, pageable)).willReturn(employeePage);
        given(employeeMapper.toResponse(employee)).willReturn(response);

        // Act
        var result = employeeService.getEmployeesByStatus(EmployeeStatus.ACTIVE, pageable);

        // Assert
        assertThat(result.getContent()).containsExactly(response);
    }

    // -------------------------------------------------------------------------
    // searchEmployees tests
    // -------------------------------------------------------------------------

    @Test
    void searchEmployees_withMatchingTerm_returnsPaginatedMatchingEmployeeResponses() {
        // Arrange
        String searchTerm = "john";
        var employee = buildEmployee();
        var response = buildEmployeeResponse(employee);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Employee> employeePage = new PageImpl<>(List.of(employee), pageable, 1);

        given(employeeRepository.searchEmployees(searchTerm, pageable)).willReturn(employeePage);
        given(employeeMapper.toResponse(employee)).willReturn(response);

        // Act
        var result = employeeService.searchEmployees(searchTerm, pageable);

        // Assert
        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void searchEmployees_withNoMatchingTerm_returnsEmptyPage() {
        // Arrange
        String searchTerm = "zzznomatch";
        Pageable pageable = PageRequest.of(0, 20);
        given(employeeRepository.searchEmployees(searchTerm, pageable)).willReturn(Page.empty(pageable));

        // Act
        var result = employeeService.searchEmployees(searchTerm, pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // updateEmployee tests
    // -------------------------------------------------------------------------

    @Test
    void updateEmployee_whenEmployeeExistsAndEmailIsNotDuplicate_returnsUpdatedResponse() {
        // Arrange
        var employee = buildEmployee();
        var request = new UpdateEmployeeRequest(
                "Jane", "Smith", "jane.smith@example.com",
                null, null, null, null, null, null
        );
        var updatedResponse = new EmployeeResponse(
                1L, "Jane", "Smith", "jane.smith@example.com",
                null, "Engineering", "Software Engineer",
                BigDecimal.valueOf(75000), LocalDate.of(2022, 1, 15),
                null, EmployeeStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()
        );

        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
        given(employeeRepository.existsByEmailAndIdNot("jane.smith@example.com", 1L)).willReturn(false);
        given(employeeRepository.save(employee)).willReturn(employee);
        given(employeeMapper.toResponse(employee)).willReturn(updatedResponse);

        // Act
        var result = employeeService.updateEmployee(1L, request);

        // Assert
        assertThat(result).isEqualTo(updatedResponse);
        then(employeeMapper).should().updateEntity(request, employee);
    }

    @Test
    void updateEmployee_withNullEmailInRequest_skipsEmailDuplicateCheck() {
        // Arrange
        var employee = buildEmployee();
        var request = new UpdateEmployeeRequest(
                "Jane", null, null, null, null, null, null, null, null
        );
        var updatedResponse = buildEmployeeResponse(employee);

        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
        given(employeeRepository.save(employee)).willReturn(employee);
        given(employeeMapper.toResponse(employee)).willReturn(updatedResponse);

        // Act
        var result = employeeService.updateEmployee(1L, request);

        // Assert
        assertThat(result).isEqualTo(updatedResponse);
        then(employeeRepository).should(never()).existsByEmailAndIdNot(any(), anyLong());
    }

    @Test
    void updateEmployee_whenEmployeeDoesNotExist_throwsEmployeeNotFoundException() {
        // Arrange
        given(employeeRepository.findById(99L)).willReturn(Optional.empty());
        var request = new UpdateEmployeeRequest(null, null, null, null, null, null, null, null, null);

        // Act & Assert
        assertThatThrownBy(() -> employeeService.updateEmployee(99L, request))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateEmployee_whenEmailBelongsToAnotherEmployee_throwsDuplicateEmailException() {
        // Arrange
        var employee = buildEmployee();
        var request = new UpdateEmployeeRequest(
                null, null, "other@example.com", null, null, null, null, null, null
        );

        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
        given(employeeRepository.existsByEmailAndIdNot("other@example.com", 1L)).willReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> employeeService.updateEmployee(1L, request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("other@example.com");

        then(employeeRepository).should(never()).save(any());
    }

    // -------------------------------------------------------------------------
    // deleteEmployee tests
    // -------------------------------------------------------------------------

    @Test
    void deleteEmployee_whenEmployeeExists_setsStatusToTerminated() {
        // Arrange
        var employee = buildEmployee();
        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.TERMINATED);
        then(employeeRepository).should().save(employee);
    }

    @Test
    void deleteEmployee_whenEmployeeDoesNotExist_throwsEmployeeNotFoundException() {
        // Arrange
        given(employeeRepository.findById(99L)).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");

        then(employeeRepository).should(never()).save(any());
    }

    // -------------------------------------------------------------------------
    // updateEmployeeStatus tests
    // -------------------------------------------------------------------------

    @Test
    void updateEmployeeStatus_whenEmployeeExists_updatesStatusAndReturnsResponse() {
        // Arrange
        var employee = buildEmployee();
        var expectedResponse = new EmployeeResponse(
                1L, "John", "Doe", "john.doe@example.com",
                "1234567890", "Engineering", "Software Engineer",
                BigDecimal.valueOf(75000), LocalDate.of(2022, 1, 15),
                null, EmployeeStatus.INACTIVE, LocalDateTime.now(), LocalDateTime.now()
        );

        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
        given(employeeRepository.save(employee)).willReturn(employee);
        given(employeeMapper.toResponse(employee)).willReturn(expectedResponse);

        // Act
        var result = employeeService.updateEmployeeStatus(1L, EmployeeStatus.INACTIVE);

        // Assert
        assertThat(result.status()).isEqualTo(EmployeeStatus.INACTIVE);
        then(employeeRepository).should().save(employee);
    }

    @Test
    void updateEmployeeStatus_whenEmployeeDoesNotExist_throwsEmployeeNotFoundException() {
        // Arrange
        given(employeeRepository.findById(99L)).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> employeeService.updateEmployeeStatus(99L, EmployeeStatus.ACTIVE))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");

        then(employeeRepository).should(never()).save(any());
    }
}
