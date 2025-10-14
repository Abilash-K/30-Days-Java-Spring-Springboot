# Day 07: Exception Handling and Validation

## 📋 Table of Contents
- [Introduction](#introduction)
- [Exception Handling in Spring Boot](#exception-handling-in-spring-boot)
- [Global Exception Handling](#global-exception-handling)
- [Custom Exceptions](#custom-exceptions)
- [Bean Validation (JSR-380)](#bean-validation-jsr-380)
- [Custom Validators](#custom-validators)
- [Validation Groups](#validation-groups)
- [Error Response Standardization](#error-response-standardization)
- [Logging and Monitoring](#logging-and-monitoring)
- [Best Practices](#best-practices)
- [Complete Example](#complete-example)
- [Exercises](#exercises)
- [Key Takeaways](#key-takeaways)
- [Resources](#resources)

## Introduction

Welcome to Day 7! Today we'll master **exception handling** and **input validation** - critical skills for building robust, production-ready Spring Boot applications.

### What You'll Learn
- Global exception handling with @ControllerAdvice
- Creating custom exceptions
- Bean Validation (JSR-380) annotations
- Custom validation logic
- Standardized error responses
- Exception logging and monitoring

### Why This Matters
- ✅ Prevents application crashes
- ✅ Provides meaningful error messages to clients
- ✅ Ensures data integrity
- ✅ Improves security by validating input
- ✅ Makes debugging easier

## Exception Handling in Spring Boot

### Traditional Exception Handling

**Without Spring:**
```java
@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        try {
            return userService.findById(id);
        } catch (UserNotFoundException e) {
            // Duplicate error handling in every method
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DatabaseException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
```

**Problems:**
- ❌ Duplicate error handling code
- ❌ Inconsistent error responses
- ❌ Hard to maintain
- ❌ Mixes business logic with error handling

### @ExceptionHandler

Handle exceptions at controller level:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id); // May throw exception
    }
    
    // Handle specific exception in this controller
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An error occurred",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### @ResponseStatus

Annotate exceptions with HTTP status:

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

@RestController
public class ProductController {
    
    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        // Automatically returns 404
    }
}
```

## Global Exception Handling

### @ControllerAdvice

Handle exceptions globally across all controllers:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    // Handle specific custom exception
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        logger.error("User not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ValidationErrorResponse response = ValidationErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Validation failed")
            .errors(errors)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    // Handle constraint violations
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex) {
        
        String message = ex.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    // Handle data integrity violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {
        
        logger.error("Data integrity violation: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.CONFLICT.value())
            .message("Data integrity violation. Resource may already exist.")
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    // Handle HTTP method not supported
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.METHOD_NOT_ALLOWED.value())
            .message("HTTP method not supported: " + ex.getMethod())
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }
    
    // Handle missing request parameters
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(
            MissingServletRequestParameterException ex) {
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Missing required parameter: " + ex.getParameterName())
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    // Handle type mismatch
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        
        String message = String.format("Invalid value '%s' for parameter '%s'", 
            ex.getValue(), ex.getName());
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    // Handle all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        logger.error("Unexpected error occurred", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("An unexpected error occurred")
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### @RestControllerAdvice with Specific Controllers

```java
// Apply to specific controllers
@RestControllerAdvice(basePackages = "com.example.api.controllers")
public class ApiExceptionHandler {
    // Exception handlers
}

// Apply to controllers with specific annotations
@RestControllerAdvice(annotations = RestController.class)
public class RestExceptionHandler {
    // Exception handlers
}

// Apply to specific controller classes
@RestControllerAdvice(assignableTypes = {UserController.class, ProductController.class})
public class SpecificExceptionHandler {
    // Exception handlers
}
```

## Custom Exceptions

### Exception Hierarchy

```java
// Base exception
public class BusinessException extends RuntimeException {
    private final String errorCode;
    
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

// Specific exceptions
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s not found with id: %d", resource, id), "RESOURCE_NOT_FOUND");
    }
    
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRequestException extends BusinessException {
    public InvalidRequestException(String message) {
        super(message, "INVALID_REQUEST");
    }
}

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String resource, String field, String value) {
        super(String.format("%s already exists with %s: %s", resource, field, value),
              "DUPLICATE_RESOURCE");
    }
}

@ResponseStatus(HttpStatus.FORBIDDEN)
public class InsufficientPermissionException extends BusinessException {
    public InsufficientPermissionException(String message) {
        super(message, "INSUFFICIENT_PERMISSION");
    }
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED");
    }
}
```

### Usage in Service Layer

```java
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
    
    public User create(CreateUserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("User", "email", dto.getEmail());
        }
        
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        return userRepository.save(user);
    }
    
    public User update(Long id, UpdateUserDTO dto) {
        User user = findById(id); // Throws ResourceNotFoundException
        
        if (!user.getEmail().equals(dto.getEmail()) && 
            userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("User", "email", dto.getEmail());
        }
        
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        return userRepository.save(user);
    }
    
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }
}
```

## Bean Validation (JSR-380)

### Setup

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### Common Validation Annotations

```java
public class UserDTO {
    
    @NotNull(message = "ID cannot be null")
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 120, message = "Age must be less than 120")
    private Integer age;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
    private String phone;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be positive")
    @DecimalMax(value = "999999.99", message = "Salary too high")
    private BigDecimal salary;
    
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
             message = "Password must contain uppercase, lowercase, number, and special character")
    private String password;
    
    @AssertTrue(message = "Terms must be accepted")
    private Boolean acceptedTerms;
    
    @Future(message = "Appointment must be in the future")
    private LocalDateTime appointmentDate;
    
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    
    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer stock;
    
    @Negative(message = "Refund amount must be negative")
    private BigDecimal refundAmount;
    
    @URL(message = "Website must be a valid URL")
    private String website;
    
    // Getters and setters
}
```

### Nested Object Validation

```java
public class OrderDTO {
    
    @NotNull(message = "Customer information is required")
    @Valid // Important: validates nested object
    private CustomerDTO customer;
    
    @NotEmpty(message = "Order must contain at least one item")
    @Valid // Validates each item in the list
    private List<OrderItemDTO> items;
    
    @NotNull
    @DecimalMin(value = "0.01", message = "Total must be positive")
    private BigDecimal total;
}

public class CustomerDTO {
    @NotBlank(message = "Customer name is required")
    private String name;
    
    @Email(message = "Email must be valid")
    private String email;
    
    @Valid
    private AddressDTO address;
}

public class AddressDTO {
    @NotBlank(message = "Street is required")
    private String street;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @Pattern(regexp = "\\d{5}", message = "ZIP code must be 5 digits")
    private String zipCode;
}
```

### Using @Valid in Controllers

```java
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    // Validate request body
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserDTO dto) {
        User user = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    // Validate path variable
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(
            @PathVariable @Min(value = 1, message = "ID must be positive") Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }
    
    // Validate request parameter
    @GetMapping
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam 
            @Size(min = 3, message = "Search query must be at least 3 characters") 
            String query) {
        
        List<User> users = userService.search(query);
        return ResponseEntity.ok(users);
    }
    
    // Multiple validations
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateUserDTO dto) {
        
        User user = userService.update(id, dto);
        return ResponseEntity.ok(user);
    }
}
```

## Custom Validators

### Custom Annotation

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
@Documented
public @interface ValidPhoneNumber {
    String message() default "Invalid phone number format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### Validator Implementation

```java
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    
    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        // Initialization logic if needed
    }
    
    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.isEmpty()) {
            return true; // Use @NotNull for null checks
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }
}
```

### Usage

```java
public class ContactDTO {
    @ValidPhoneNumber(message = "Please provide a valid phone number")
    private String phone;
    
    // Other fields
}
```

### Class-Level Validation

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchesValidator.class)
public @interface PasswordMatches {
    String message() default "Passwords don't match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class PasswordMatchesValidator 
        implements ConstraintValidator<PasswordMatches, Object> {
    
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj instanceof PasswordDTO) {
            PasswordDTO dto = (PasswordDTO) obj;
            return dto.getPassword() != null && 
                   dto.getPassword().equals(dto.getConfirmPassword());
        }
        return false;
    }
}

@PasswordMatches
public class PasswordDTO {
    @NotBlank
    @Size(min = 8)
    private String password;
    
    @NotBlank
    private String confirmPassword;
    
    // Getters and setters
}
```

### Custom Validator with Dependencies

```java
@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return true;
        }
        return !userRepository.existsByEmail(email);
    }
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
    String message() default "Email already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class RegisterDTO {
    @NotBlank
    @Email
    @UniqueEmail
    private String email;
    
    // Other fields
}
```

## Validation Groups

### Defining Groups

```java
public interface CreateValidation {}
public interface UpdateValidation {}
```

### Using Groups in DTO

```java
public class ProductDTO {
    
    @Null(groups = CreateValidation.class, message = "ID must be null when creating")
    @NotNull(groups = UpdateValidation.class, message = "ID is required when updating")
    private Long id;
    
    @NotBlank(groups = {CreateValidation.class, UpdateValidation.class})
    private String name;
    
    @NotNull(groups = CreateValidation.class)
    @DecimalMin(value = "0.01", groups = {CreateValidation.class, UpdateValidation.class})
    private BigDecimal price;
    
    // Other fields
}
```

### Using Groups in Controller

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @PostMapping
    public ResponseEntity<Product> create(
            @Validated(CreateValidation.class) @RequestBody ProductDTO dto) {
        Product product = productService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Product> update(
            @PathVariable Long id,
            @Validated(UpdateValidation.class) @RequestBody ProductDTO dto) {
        Product product = productService.update(id, dto);
        return ResponseEntity.ok(product);
    }
}
```

## Error Response Standardization

### Error Response Model

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private String errorCode;
    private LocalDateTime timestamp;
    private String path;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String debugMessage;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SubError> subErrors;
}

@Data
@AllArgsConstructor
public class SubError {
    private String object;
    private String field;
    private Object rejectedValue;
    private String message;
}

@Data
@Builder
public class ValidationErrorResponse {
    private int status;
    private String message;
    private Map<String, String> errors;
    private LocalDateTime timestamp;
    private String path;
}
```

### Enhanced Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        List<SubError> subErrors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            subErrors.add(new SubError(
                error.getObjectName(),
                error.getField(),
                error.getRejectedValue(),
                error.getDefaultMessage()
            ));
        });
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Validation failed")
            .errorCode("VALIDATION_ERROR")
            .timestamp(LocalDateTime.now())
            .path(getPath(request))
            .subErrors(subErrors)
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            WebRequest request) {
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message(ex.getMessage())
            .errorCode(ex.getErrorCode())
            .timestamp(LocalDateTime.now())
            .path(getPath(request))
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    private String getPath(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }
}
```

## Logging and Monitoring

### Logging Exceptions

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        
        // Log with context
        logger.error("Unexpected error occurred", ex);
        logger.error("Request URI: {}", ((ServletWebRequest) request).getRequest().getRequestURI());
        logger.error("Request Method: {}", ((ServletWebRequest) request).getRequest().getMethod());
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("An unexpected error occurred")
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        
        // Log as warning for expected exceptions
        logger.warn("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
```

### Exception Metrics

```java
@RestControllerAdvice
public class MetricsExceptionHandler {
    
    private final MeterRegistry meterRegistry;
    
    public MetricsExceptionHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        // Increment counter
        meterRegistry.counter("exceptions.total", 
            "type", ex.getClass().getSimpleName()).increment();
        
        // Handle exception...
        return null;
    }
}
```

## Best Practices

### 1. Use Custom Exceptions

```java
// Good
throw new ResourceNotFoundException("User", userId);

// Bad
throw new RuntimeException("User not found");
```

### 2. Centralize Exception Handling

```java
// Good - Single @RestControllerAdvice
@RestControllerAdvice
public class GlobalExceptionHandler {
    // All exception handlers
}

// Bad - Multiple handlers scattered
```

### 3. Provide Meaningful Messages

```java
// Good
throw new InvalidRequestException(
    "Invalid date range: start date must be before end date");

// Bad
throw new InvalidRequestException("Invalid input");
```

### 4. Don't Expose Sensitive Information

```java
// Good
@ExceptionHandler(SQLException.class)
public ResponseEntity<ErrorResponse> handleSQLException(SQLException ex) {
    logger.error("Database error", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("Database error occurred"));
}

// Bad
@ExceptionHandler(SQLException.class)
public ResponseEntity<ErrorResponse> handleSQLException(SQLException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(ex.getMessage())); // Exposes DB details
}
```

### 5. Validate Early

```java
// Good - Validate at controller
@PostMapping
public User create(@Valid @RequestBody UserDTO dto) {
    return userService.create(dto);
}

// Bad - Validate in service
@PostMapping
public User create(@RequestBody UserDTO dto) {
    return userService.create(dto); // Validation happens too late
}
```

## Complete Example

### User Management API with Complete Error Handling

```java
// Custom Exceptions
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }
}

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("User already exists with email: " + email);
    }
}

// DTO with Validation
@Data
public class CreateUserDTO {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 120, message = "Age must be less than 120")
    private Integer age;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", 
             message = "Phone number must be valid")
    private String phone;
}

// Service
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User create(CreateUserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException(dto.getEmail());
        }
        
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setAge(dto.getAge());
        user.setPhone(dto.getPhone());
        
        return userRepository.save(user);
    }
    
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
    
    public User update(Long id, CreateUserDTO dto) {
        User user = findById(id);
        
        if (!user.getEmail().equals(dto.getEmail()) && 
            userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException(dto.getEmail());
        }
        
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setAge(dto.getAge());
        user.setPhone(dto.getPhone());
        
        return userRepository.save(user);
    }
}

// Controller
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable @Positive Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserDTO dto) {
        User user = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable @Positive Long id,
            @Valid @RequestBody CreateUserDTO dto) {
        User user = userService.update(id, dto);
        return ResponseEntity.ok(user);
    }
}

// Global Exception Handler
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex, WebRequest request) {
        
        logger.warn("User not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(
            DuplicateEmailException ex, WebRequest request) {
        
        logger.warn("Duplicate email: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.CONFLICT.value())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ValidationErrorResponse response = ValidationErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Validation failed")
            .errors(errors)
            .timestamp(LocalDateTime.now())
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        String message = ex.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message(message)
            .timestamp(LocalDateTime.now())
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        
        logger.error("Unexpected error occurred", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("An unexpected error occurred")
            .timestamp(LocalDateTime.now())
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

## Exercises

### Exercise 1: Custom Exceptions
Create a library management system with:
- `BookNotFoundException`
- `BookAlreadyBorrowedException`
- `InvalidReturnDateException`
- Global exception handler

### Exercise 2: Validation
Create a registration form DTO with validation for:
- Username (unique, 3-20 characters, alphanumeric)
- Email (valid format, unique)
- Password (min 8 chars, contains uppercase, lowercase, number, special char)
- Age (18-100)
- Phone (valid format)

### Exercise 3: Custom Validator
Create a custom validator `@FutureWorkingDay` that validates:
- Date is in the future
- Date is not a weekend
- Date is not a holiday

### Exercise 4: Validation Groups
Create a Product DTO with validation groups for:
- Create operation (ID must be null)
- Update operation (ID required)
- Partial update (only non-null fields validated)

### Exercise 5: Complete Error Handling
Build a complete REST API with:
- Custom exceptions
- Global exception handler
- Standardized error responses
- Validation
- Logging
- Metrics

## Key Takeaways

✅ Use **@RestControllerAdvice** for global exception handling  
✅ Create **custom exception hierarchy** for business logic  
✅ Use **@Valid** and **Bean Validation** for input validation  
✅ Implement **standardized error responses**  
✅ **Log exceptions** with appropriate levels  
✅ Don't expose **sensitive information** in error messages  
✅ Use **@ResponseStatus** for simple exceptions  
✅ Create **custom validators** for complex validation logic  
✅ Use **validation groups** for different scenarios  
✅ Return appropriate **HTTP status codes**  
✅ Validate early at the **controller layer**  
✅ Monitor exceptions with **metrics**  

## Resources

### Official Documentation
- [Spring Exception Handling](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-ann-exceptionhandler)
- [Bean Validation Specification](https://beanvalidation.org/2.0/spec/)
- [Hibernate Validator](https://hibernate.org/validator/)

### Articles
- [Baeldung: Error Handling for REST with Spring](https://www.baeldung.com/exception-handling-for-rest-with-spring)
- [DZone: Exception Handling in Spring Boot](https://dzone.com/articles/exception-handling-in-spring-boot)

### Books
- "Spring in Action" by Craig Walls
- "Spring Boot in Practice" by Somnath Musib

## Next Steps

Tomorrow, we'll explore **Spring Boot Configuration Management** where we'll learn how to externalize and manage application configurations!

---
[<< Previous: Day 06](../Day06_JPA_Database/README.md) | [Back to Main](../README.md) | [Next: Day 08 >>](../Day08_Configuration/README.md)
