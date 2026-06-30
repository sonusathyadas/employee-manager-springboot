package com.company.employeemanager.controller;

import com.company.employeemanager.dto.request.CreateEmployeeRequest;
import com.company.employeemanager.dto.request.UpdateEmployeeRequest;
import com.company.employeemanager.dto.response.EmployeeResponse;
import com.company.employeemanager.entity.EmployeeStatus;
import com.company.employeemanager.exception.DuplicateEmailException;
import com.company.employeemanager.exception.EmployeeNotFoundException;
import com.company.employeemanager.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-layer tests for {@link EmployeeController}.
 * Uses {@link WebMvcTest} with a mocked {@link EmployeeService} to verify HTTP contract:
 * request mapping, status codes, and response serialisation.
 * {@link WithMockUser} simulates an authenticated user to satisfy the JWT security filter.
 */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** Satisfies JPA auditing dependency that is triggered by {@code @EnableJpaAuditing} on the main class. */
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    /** Mocked service — all business logic is tested separately in service-level tests. */
    @MockBean
    private EmployeeService employeeService;

    /** Required by {@link com.company.employeemanager.security.JwtAuthenticationFilter}. */
    @MockBean
    private com.company.employeemanager.security.JwtUtil jwtUtil;

    /** Required by {@link com.company.employeemanager.security.JwtAuthenticationFilter}. */
    @MockBean
    private UserDetailsService userDetailsService;

    // -------------------------------------------------------------------------
    // Test data helper
    // -------------------------------------------------------------------------

    /**
     * Builds a sample {@link EmployeeResponse} DTO for stubbing service calls.
     */
    private EmployeeResponse buildEmployeeResponse() {
        return new EmployeeResponse(
                1L, "John", "Doe", "john.doe@example.com",
                "1234567890", "Engineering", "Software Engineer",
                BigDecimal.valueOf(75000), LocalDate.of(2022, 1, 15),
                null, EmployeeStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/employees — createEmployee
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    void createEmployee_withValidRequest_returns201WithLocationHeader() throws Exception {
        // Arrange
        var request = new CreateEmployeeRequest(
                "John", "Doe", "john.doe@example.com",
                "1234567890", "Engineering", "Software Engineer",
                BigDecimal.valueOf(75000), LocalDate.of(2022, 1, 15), null
        );
        var response = buildEmployeeResponse();
        given(employeeService.createEmployee(any())).willReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser
    void createEmployee_withMissingRequiredFields_returns400() throws Exception {
        // Arrange — empty request body triggers Bean Validation
        var invalidRequest = new CreateEmployeeRequest(
                "", "", "", null, "", "", null, null, null
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void createEmployee_whenEmailAlreadyInUse_returns409() throws Exception {
        // Arrange
        var request = new CreateEmployeeRequest(
                "John", "Doe", "duplicate@example.com",
                null, "Engineering", "Software Engineer",
                BigDecimal.valueOf(75000), LocalDate.of(2022, 1, 15), null
        );
        given(employeeService.createEmployee(any()))
                .willThrow(new DuplicateEmailException("duplicate@example.com"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/employees/{id} — getEmployeeById
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    void getEmployeeById_whenEmployeeExists_returns200WithBody() throws Exception {
        // Arrange
        given(employeeService.getEmployeeById(1L)).willReturn(buildEmployeeResponse());

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser
    void getEmployeeById_whenEmployeeDoesNotExist_returns404() throws Exception {
        // Arrange
        given(employeeService.getEmployeeById(99L))
                .willThrow(new EmployeeNotFoundException("Employee not found with ID: 99"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/employees/email/{email} — getEmployeeByEmail
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    void getEmployeeByEmail_whenEmployeeExists_returns200WithBody() throws Exception {
        // Arrange
        given(employeeService.getEmployeeByEmail("john.doe@example.com"))
                .willReturn(buildEmployeeResponse());

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/email/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser
    void getEmployeeByEmail_whenEmployeeDoesNotExist_returns404() throws Exception {
        // Arrange
        given(employeeService.getEmployeeByEmail("unknown@example.com"))
                .willThrow(new EmployeeNotFoundException("Employee not found with email: unknown@example.com"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/email/unknown@example.com"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/employees — getAllEmployees
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    void getAllEmployees_returns200WithPaginatedResults() throws Exception {
        // Arrange
        var page = new PageImpl<>(List.of(buildEmployeeResponse()));
        given(employeeService.getAllEmployees(any(Pageable.class))).willReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/employees/department/{department}
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    void getEmployeesByDepartment_returns200WithPaginatedResults() throws Exception {
        // Arrange
        var page = new PageImpl<>(List.of(buildEmployeeResponse()));
        given(employeeService.getEmployeesByDepartment(eq("Engineering"), any(Pageable.class)))
                .willReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/department/Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].department").value("Engineering"));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/employees/status/{status}
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    void getEmployeesByStatus_returns200WithPaginatedResults() throws Exception {
        // Arrange
        var page = new PageImpl<>(List.of(buildEmployeeResponse()));
        given(employeeService.getEmployeesByStatus(eq(EmployeeStatus.ACTIVE), any(Pageable.class)))
                .willReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/employees/search?q=
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    void searchEmployees_returns200WithMatchingResults() throws Exception {
        // Arrange
        var page = new PageImpl<>(List.of(buildEmployeeResponse()));
        given(employeeService.searchEmployees(eq("john"), any(Pageable.class))).willReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/search").param("q", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("John"));
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/employees/{id} — updateEmployee
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    void updateEmployee_whenEmployeeExists_returns200WithUpdatedBody() throws Exception {
        // Arrange
        var request = new UpdateEmployeeRequest(
                "Jane", "Smith", null, null, null, null, null, null, null
        );
        var updated = new EmployeeResponse(
                1L, "Jane", "Smith", "john.doe@example.com",
                "1234567890", "Engineering", "Software Engineer",
                BigDecimal.valueOf(75000), LocalDate.of(2022, 1, 15),
                null, EmployeeStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()
        );
        given(employeeService.updateEmployee(eq(1L), any())).willReturn(updated);

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    @WithMockUser
    void updateEmployee_whenEmployeeDoesNotExist_returns404() throws Exception {
        // Arrange
        var request = new UpdateEmployeeRequest(
                "Jane", null, null, null, null, null, null, null, null
        );
        given(employeeService.updateEmployee(eq(99L), any()))
                .willThrow(new EmployeeNotFoundException("Employee not found with ID: 99"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateEmployee_whenEmailAlreadyInUse_returns409() throws Exception {
        // Arrange
        var request = new UpdateEmployeeRequest(
                null, null, "taken@example.com", null, null, null, null, null, null
        );
        given(employeeService.updateEmployee(eq(1L), any()))
                .willThrow(new DuplicateEmailException("taken@example.com"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // -------------------------------------------------------------------------
    // PATCH /api/v1/employees/{id} — patchEmployee
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    void patchEmployee_whenEmployeeExists_returns200() throws Exception {
        // Arrange
        var request = new UpdateEmployeeRequest(
                null, null, null, "9876543210", null, null, null, null, null
        );
        given(employeeService.updateEmployee(eq(1L), any())).willReturn(buildEmployeeResponse());

        // Act & Assert
        mockMvc.perform(patch("/api/v1/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // PATCH /api/v1/employees/{id}/status — updateEmployeeStatus
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    void updateEmployeeStatus_whenEmployeeExists_returns200WithUpdatedStatus() throws Exception {
        // Arrange
        var updatedResponse = new EmployeeResponse(
                1L, "John", "Doe", "john.doe@example.com",
                "1234567890", "Engineering", "Software Engineer",
                BigDecimal.valueOf(75000), LocalDate.of(2022, 1, 15),
                null, EmployeeStatus.INACTIVE, LocalDateTime.now(), LocalDateTime.now()
        );
        given(employeeService.updateEmployeeStatus(eq(1L), eq(EmployeeStatus.INACTIVE)))
                .willReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/employees/1/status")
                        .with(csrf())
                        .param("status", "INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser
    void updateEmployeeStatus_whenEmployeeDoesNotExist_returns404() throws Exception {
        // Arrange
        given(employeeService.updateEmployeeStatus(eq(99L), any()))
                .willThrow(new EmployeeNotFoundException("Employee not found with ID: 99"));

        // Act & Assert
        mockMvc.perform(patch("/api/v1/employees/99/status")
                        .with(csrf())
                        .param("status", "INACTIVE"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/employees/{id} — deleteEmployee
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    void deleteEmployee_whenEmployeeExists_returns204NoContent() throws Exception {
        // Arrange
        willDoNothing().given(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteEmployee_whenEmployeeDoesNotExist_returns404() throws Exception {
        // Arrange
        willThrow(new EmployeeNotFoundException("Employee not found with ID: 99"))
                .given(employeeService).deleteEmployee(99L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/99").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
