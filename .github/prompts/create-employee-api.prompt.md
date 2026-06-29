---
name: create-employee-api
description: A prompt to create a SpringBoot REST API for managing employee data, including endpoints for creating, reading, updating, and deleting employee records.
---

## Overview

Generate a complete Spring Boot REST API application that manages Employee profiles with full CRUD operations. The application must follow clean architecture principles, production-ready patterns, and be structured to allow JWT authentication to be added later without major refactoring.

---

## Project Setup

- **Framework:** Spring Boot 3.x
- **Language:** Java 21
- **Build tool:** Maven (`pom.xml`)
- **Packaging:** JAR
- **Base package:** `com.company.employeemanager`

### Required Maven Dependencies (`pom.xml`)

```xml
<dependencies>
  <!-- Web -->
  <dependency>spring-boot-starter-web</dependency>

  <!-- Data -->
  <dependency>spring-boot-starter-data-jpa</dependency>
  <dependency>postgresql (or h2 for dev)</dependency>

  <!-- Validation -->
  <dependency>spring-boot-starter-validation</dependency>

  <!-- Lombok -->
  <dependency>lombok</dependency>

  <!-- MapStruct -->
  <dependency>mapstruct</dependency>
  <dependency>mapstruct-processor</dependency>

  <!-- Actuator -->
  <dependency>spring-boot-starter-actuator</dependency>

  <!-- Test -->
  <dependency>spring-boot-starter-test</dependency>
</dependencies>
```

---

## Project Structure

Generate the following package structure:

```
src/
└── main/
    └── java/com/company/employeemanager/
        ├── EmployeeManagerApplication.java
        ├── config/
        │   └── OpenApiConfig.java
        ├── controller/
        │   └── EmployeeController.java
        ├── dto/
        │   ├── request/
        │   │   ├── CreateEmployeeRequest.java
        │   │   └── UpdateEmployeeRequest.java
        │   └── response/
        │       └── EmployeeResponse.java
        ├── entity/
        │   └── Employee.java
        ├── exception/
        │   ├── GlobalExceptionHandler.java
        │   ├── EmployeeNotFoundException.java
        │   └── ErrorResponse.java
        ├── mapper/
        │   └── EmployeeMapper.java
        ├── repository/
        │   └── EmployeeRepository.java
        └── service/
            ├── EmployeeService.java
            └── impl/
                └── EmployeeServiceImpl.java
└── resources/
    ├── application.yml
    ├── application-dev.yml
    └── application-prod.yml
```

---

## Entity: `Employee`

Generate a JPA entity with the following fields. Use Lombok annotations (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`). Use `@EntityListeners(AuditingEntityListener.class)` for audit fields.

| Field         | Type           | Constraints                              |
|---------------|----------------|------------------------------------------|
| `id`          | `Long`         | `@Id`, `@GeneratedValue` (IDENTITY)      |
| `firstName`   | `String`       | Not null, max 100 chars                  |
| `lastName`    | `String`       | Not null, max 100 chars                  |
| `email`       | `String`       | Not null, unique, valid email format     |
| `phone`       | `String`       | Optional, max 20 chars                   |
| `department`  | `String`       | Not null, max 100 chars                  |
| `jobTitle`    | `String`       | Not null, max 100 chars                  |
| `salary`      | `BigDecimal`   | Not null, positive value                 |
| `hireDate`    | `LocalDate`    | Not null                                 |
| `status`      | `EmployeeStatus` (enum) | Not null, default `ACTIVE`      |
| `createdAt`   | `LocalDateTime`| `@CreatedDate`, not updatable            |
| `updatedAt`   | `LocalDateTime`| `@LastModifiedDate`                      |

Generate an `EmployeeStatus` enum with values: `ACTIVE`, `INACTIVE`, `ON_LEAVE`, `TERMINATED`.

Enable JPA Auditing in `EmployeeManagerApplication.java` using `@EnableJpaAuditing`.

---

## DTOs

### `CreateEmployeeRequest`

Include all required fields with Bean Validation annotations:
- `@NotBlank` on `firstName`, `lastName`, `email`, `department`, `jobTitle`
- `@Email` on `email`
- `@NotNull` + `@Positive` on `salary`
- `@NotNull` + `@PastOrPresent` on `hireDate`
- `@Size` constraints matching entity limits

### `UpdateEmployeeRequest`

Same fields as `CreateEmployeeRequest` but all fields are **optional** (use wrapper types or `Optional`). Only provided fields should update the entity (partial update / PATCH semantics).

### `EmployeeResponse`

A flat read-only DTO exposing all entity fields including `id`, `createdAt`, and `updatedAt`. Annotate with `@JsonFormat` for date/time fields (ISO-8601).

---

## Mapper: `EmployeeMapper`

Use **MapStruct** (`@Mapper(componentModel = "spring")`).

Generate mapping methods:
- `toResponse(Employee employee) → EmployeeResponse`
- `toEntity(CreateEmployeeRequest request) → Employee`
- `updateEntity(UpdateEmployeeRequest request, @MappingTarget Employee employee)` — ignore null fields using `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)`
- `toResponseList(List<Employee> employees) → List<EmployeeResponse>`

---

## Repository: `EmployeeRepository`

Extend `JpaRepository<Employee, Long>`. Add the following query methods:

```java
Optional<Employee> findByEmail(String email);
boolean existsByEmail(String email);
boolean existsByEmailAndIdNot(String email, Long id);
Page<Employee> findByDepartment(String department, Pageable pageable);
Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);

@Query("SELECT e FROM Employee e WHERE " +
       "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
       "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
       "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))")
Page<Employee> searchEmployees(@Param("search") String search, Pageable pageable);
```

---

## Service Interface: `EmployeeService`

```java
public interface EmployeeService {
    EmployeeResponse createEmployee(CreateEmployeeRequest request);
    EmployeeResponse getEmployeeById(Long id);
    EmployeeResponse getEmployeeByEmail(String email);
    Page<EmployeeResponse> getAllEmployees(Pageable pageable);
    Page<EmployeeResponse> getEmployeesByDepartment(String department, Pageable pageable);
    Page<EmployeeResponse> getEmployeesByStatus(EmployeeStatus status, Pageable pageable);
    Page<EmployeeResponse> searchEmployees(String search, Pageable pageable);
    EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request);
    void deleteEmployee(Long id);
    EmployeeResponse updateEmployeeStatus(Long id, EmployeeStatus status);
}
```

### `EmployeeServiceImpl`

Implement the interface with `@Service` and `@Transactional`. Business rules:
- Before creating, verify no employee exists with the same `email` → throw `DuplicateEmailException` if found.
- Before updating email, verify uniqueness excluding the current employee.
- `getEmployeeById` must throw `EmployeeNotFoundException` if not found.
- `deleteEmployee` performs a soft delete by setting `status = TERMINATED` (do NOT physically delete rows).
- Log key operations at INFO level using SLF4J `@Slf4j`.

---

## Controller: `EmployeeController`

Base path: `/api/v1/employees`

Annotate with `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor`. Add Swagger/OpenAPI annotations (`@Tag`, `@Operation`, `@ApiResponse`).

| Method | Endpoint               | Description                         | Request Body              | Response            |
|--------|------------------------|-------------------------------------|---------------------------|---------------------|
| POST   | `/`                    | Create new employee                 | `CreateEmployeeRequest`   | `201 EmployeeResponse` |
| GET    | `/{id}`                | Get employee by ID                  | —                         | `200 EmployeeResponse` |
| GET    | `/email/{email}`       | Get employee by email               | —                         | `200 EmployeeResponse` |
| GET    | `/`                    | Get all employees (paginated)       | Query: `page`, `size`, `sort` | `200 Page<EmployeeResponse>` |
| GET    | `/department/{dept}`   | Filter by department (paginated)    | —                         | `200 Page<EmployeeResponse>` |
| GET    | `/status/{status}`     | Filter by status (paginated)        | —                         | `200 Page<EmployeeResponse>` |
| GET    | `/search`              | Search by name/email (paginated)    | Query: `q`                | `200 Page<EmployeeResponse>` |
| PUT    | `/{id}`                | Full update                         | `UpdateEmployeeRequest`   | `200 EmployeeResponse` |
| PATCH  | `/{id}`                | Partial update                      | `UpdateEmployeeRequest`   | `200 EmployeeResponse` |
| PATCH  | `/{id}/status`         | Update status only                  | Query: `status`           | `200 EmployeeResponse` |
| DELETE | `/{id}`                | Soft delete (sets TERMINATED)       | —                         | `204 No Content`    |

Use `ResponseEntity<?>` return types throughout.

---

## Exception Handling

### Custom Exceptions

```java
// Thrown when employee is not found by id or email
public class EmployeeNotFoundException extends RuntimeException { ... }

// Thrown when email already exists
public class DuplicateEmailException extends RuntimeException { ... }
```

### `GlobalExceptionHandler`

Annotate with `@RestControllerAdvice`. Handle:

| Exception                            | HTTP Status | Message                          |
|--------------------------------------|-------------|----------------------------------|
| `EmployeeNotFoundException`          | 404         | "Employee not found: {detail}"   |
| `DuplicateEmailException`            | 409         | "Email already in use: {email}"  |
| `MethodArgumentNotValidException`    | 400         | List of field validation errors  |
| `ConstraintViolationException`       | 400         | Constraint violation message     |
| `HttpMessageNotReadableException`    | 400         | "Malformed JSON request"         |
| `Exception` (fallback)              | 500         | "An unexpected error occurred"   |

### `ErrorResponse` DTO

```java
public record ErrorResponse(
    int status,
    String error,
    String message,
    LocalDateTime timestamp,
    String path,
    List<FieldError> fieldErrors   // nullable, for validation errors
) {}
```

---

## Configuration Files

### `application.yml` (base)

```yaml
spring:
  application:
    name: employee-manager
  profiles:
    active: dev
  jpa:
    open-in-view: false

management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics
```

### `application-dev.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:employeedb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

server:
  port: 8080

logging:
  level:
    com.company.employeemanager: DEBUG
```

### `application-prod.yml`

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

server:
  port: ${PORT:8080}

logging:
  level:
    root: WARN
    com.company.employeemanager: INFO
```

---

## OpenAPI / Swagger Config

Generate `OpenApiConfig.java` in the `config` package:

```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI employeeManagerOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Employee Manager API")
                .description("REST API for managing employee profiles. JWT auth will be added.")
                .version("1.0.0"));
    }
}
```

> **Note:** Do NOT add `SecurityScheme` or security requirements to the OpenAPI config — JWT will be wired in a later step.

---

## Security Placeholder (IMPORTANT)

Add a `SecurityConfig.java` class in the `config` package that permits all requests for now:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
```

> This class is intentionally minimal. JWT filter chain and role-based rules will replace this later using agent mode.

---

## Unit Tests
- Do not generate any test cases in this step. Unit tests will be added in a later step after the API is fully implemented.

## Code Style Rules

- All classes must use **constructor injection** (no `@Autowired` on fields).
- Use **`@Slf4j`** from Lombok for logging — never `System.out.println`.
- No business logic in controllers.
- No entity objects returned directly from controllers — always use DTOs.
- All service methods must be `@Transactional` (read-only methods use `@Transactional(readOnly = true)`).
- Paginated endpoints must accept `Pageable` via `@PageableDefault`.
