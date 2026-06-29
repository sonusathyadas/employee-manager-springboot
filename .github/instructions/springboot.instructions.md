---
description: Instruction and best practices for Spring Boot projects.
applyTo: '**/*.java, **/*.properties, **/*.yml'
---

## General Java Rules

- Always use **Java 21 features** where appropriate: records, sealed classes, pattern matching, text blocks.
- Prefer **immutability**: use `final` fields, unmodifiable collections, and records for DTOs.
- Use **`var`** for local variables only when the type is obvious from the right-hand side.
- Never use raw types. Always parameterize generics.
- Avoid `null` returns — use `Optional<T>` in repository/service return types where absence is meaningful.
- Prefer **`switch` expressions** over `switch` statements for exhaustive pattern matching.

## Spring Boot Conventions

### Dependency Injection
- **Always use constructor injection.** Never use `@Autowired` on fields or setters.
- Use Lombok `@RequiredArgsConstructor` to generate the constructor automatically.
- Mark injected fields `private final`.

```java
// Correct
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
}

// Wrong — never do this
@Autowired
private EmployeeRepository employeeRepository;
```

### Configuration
- Use **`application.yml`** (not `.properties`) for all configuration.
- Use **profile-specific files**: `application-dev.yml`, `application-prod.yml`.
- Never hardcode secrets. Use environment variables: `${DB_PASSWORD}`.
- Set `spring.jpa.open-in-view=false` always.
- Validate required config values using `@ConfigurationProperties` + `@Validated`.

### Beans & Components
- Use the most specific stereotype annotation: `@Repository`, `@Service`, `@RestController` — not generic `@Component`.
- Define infrastructure beans (datasource, mappers, clients) in `@Configuration` classes.
- Keep `@SpringBootApplication` main class free of beans except `main()`.

---

## REST API Design

- Base all API paths on versioned routes: `/api/v1/...`
- Use **nouns** for resources, not verbs: `/employees` not `/getEmployees`.
- Return `ResponseEntity<T>` from all controller methods.
- Correct HTTP status codes:
  - `200 OK` — successful GET, PUT, PATCH
  - `201 Created` — successful POST (include `Location` header)
  - `204 No Content` — successful DELETE
  - `400 Bad Request` — validation failure
  - `404 Not Found` — resource not found
  - `409 Conflict` — duplicate resource
  - `500 Internal Server Error` — unexpected failure
- Use `@PageableDefault` for paginated endpoints. Return `Page<T>` not `List<T>`.
- Never return entity objects from controllers — always use response DTOs.

---

## Layered Architecture

```
Controller → Service (interface) → ServiceImpl → Repository → Entity
                                                ↘ Mapper ↗
```

- **Controllers:** HTTP boundary only. Parse input, delegate to service, build response. No business logic.
- **Service interface:** Public contract. All methods here.
- **Service implementation:** Business logic, `@Transactional`, validation.
- **Repository:** Data access only. Custom queries with `@Query` or derived method names.
- **Entity:** JPA mapping + audit fields. No business logic.
- **DTO:** Input validation (request) and output shaping (response). No JPA annotations.
- **Mapper:** MapStruct only. No manual mapping loops.

---

## Data & JPA

- Always annotate entities with `@EntityListeners(AuditingEntityListener.class)` and enable `@EnableJpaAuditing`.
- Include `createdAt` and `updatedAt` on all entities using `@CreatedDate` / `@LastModifiedDate`.
- Use `@Column(nullable = false)` to mirror Bean Validation constraints at the DB level.
- Prefer **soft deletes** (status field) over physical DELETE for auditable data.
- Use `@Transactional(readOnly = true)` on all read-only service methods.
- Avoid `FetchType.EAGER`. Use `JOIN FETCH` in JPQL or `@EntityGraph` when needed.
- Never call `findAll()` without pagination on large tables.

```java
// Correct
Page<Employee> findAll(Pageable pageable);

// Wrong — unbounded
List<Employee> findAll();
```

---

## Validation

- Use **Bean Validation (Jakarta)** annotations on all request DTOs: `@NotBlank`, `@Email`, `@Size`, `@Positive`, etc.
- Annotate controller parameters with `@Valid` or `@Validated`.
- For path/query param validation, use `@Validated` at class level and constraints directly on params.
- Never validate inside service implementations manually — let the framework throw `ConstraintViolationException`.

---

## Exception Handling

- Create a **dedicated exception class** per business error case (e.g., `EmployeeNotFoundException`, `DuplicateEmailException`).
- All exceptions must extend `RuntimeException`.
- Handle ALL exceptions in a single `@RestControllerAdvice` class: `GlobalExceptionHandler`.
- Always return a consistent `ErrorResponse` record with: `status`, `error`, `message`, `timestamp`, `path`.
- Log unexpected exceptions at `ERROR` level with stack trace. Log expected business exceptions at `WARN`.

```java
@ExceptionHandler(EmployeeNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(
        EmployeeNotFoundException ex, HttpServletRequest request) {
    log.warn("Employee not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(404, "Not Found", ex.getMessage(),
              LocalDateTime.now(), request.getRequestURI(), null));
}
```

---

## Logging

- Use **SLF4J** via Lombok `@Slf4j`. Never use `System.out.println` or `printStackTrace()`.
- Log at correct levels:
  - `DEBUG`: internal variable values, repository queries (dev only)
  - `INFO`: business operations started/completed (create, update, delete)
  - `WARN`: recoverable issues, expected business exceptions
  - `ERROR`: unexpected exceptions, integration failures
- Never log passwords, tokens, PII (email in production, salary) at INFO or above.
- Use structured log messages with parameters, not string concatenation:

```java
// Correct
log.info("Creating employee with email: {}", request.email());

// Wrong
log.info("Creating employee with email: " + request.email());
```

---

## Testing

- Unit test service implementations with `@ExtendWith(MockitoExtension.class)`.
- Test controllers with `@WebMvcTest` — mock the service layer with `@MockBean`.
- Use `@DataJpaTest` for repository tests with an in-memory DB.
- Integration test critical flows with `@SpringBootTest` + `@AutoConfigureMockMvc`.
- Follow **AAA pattern**: Arrange, Act, Assert — one assertion group per test.
- Name tests with the pattern: `methodName_stateUnderTest_expectedBehavior`.
- Aim for **80%+ branch coverage** on service implementations.

```java
@Test
void createEmployee_whenEmailAlreadyExists_throwsDuplicateEmailException() {
    // Arrange
    given(employeeRepository.existsByEmail(any())).willReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> employeeService.createEmployee(request))
        .isInstanceOf(DuplicateEmailException.class);
}
```

---

## Security Readiness (Pre-JWT)

- Keep `SecurityConfig` simple with `permitAll()` until JWT is added.
- Never store user context in static fields or ThreadLocal manually — use `SecurityContextHolder`.
- Structure the security config so a `JwtAuthenticationFilter` can be inserted as a single `addFilterBefore(...)` call.
- Do NOT add `@PreAuthorize` annotations yet — add them in the JWT step.
- Keep `HttpSecurity` chain stateless-ready: `sessionManagement(session -> session.sessionCreationPolicy(STATELESS))` even now.

---

## Code Quality

- Use **Lombok** to reduce boilerplate: `@Data`, `@Builder`, `@Slf4j`, `@RequiredArgsConstructor`.
- Use **records** for DTOs and value objects (Java 16+).
- Keep methods under **30 lines**. Extract private helpers if longer.
- Keep classes under **300 lines**. Split responsibilities if larger.
- No magic strings or numbers — use constants or enums.
- All public API methods must have **Javadoc** on the interface/contract.
- Run **Checkstyle** and **SpotBugs** in the Maven build.

---

## What Copilot Should NOT Generate

- `@Autowired` field injection.
- `System.out.println` or `e.printStackTrace()`.
- Returning `null` from service methods (use `Optional` or throw).
- Physical DELETE of auditable records.
- Hardcoded credentials or secrets.
- Business logic inside controllers or entities.
- Entity objects in API responses.
- Unbounded `findAll()` calls.
- `FetchType.EAGER` on collections.
- `open-in-view=true` (Hibernate anti-pattern).
- `@Transactional` on controllers.
