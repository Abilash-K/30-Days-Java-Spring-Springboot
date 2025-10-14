# Day 05: Spring MVC and REST APIs

## 📋 Table of Contents
- [Introduction](#introduction)
- [REST Principles](#rest-principles)
- [Spring MVC Architecture](#spring-mvc-architecture)
- [Building REST APIs](#building-rest-apis)
- [Request Handling](#request-handling)
- [Response Handling](#response-handling)
- [HTTP Methods](#http-methods)
- [Status Codes](#status-codes)
- [Content Negotiation](#content-negotiation)
- [API Versioning](#api-versioning)
- [HATEOAS](#hateoas)
- [Best Practices](#best-practices)
- [Complete Example](#complete-example)
- [Exercises](#exercises)
- [Key Takeaways](#key-takeaways)
- [Resources](#resources)

## Introduction

Welcome to Day 5! Today we'll master **Spring MVC** and learn how to build robust **RESTful APIs** following industry best practices.

### What You'll Learn
- REST architectural principles
- Spring MVC request processing flow
- Building CRUD REST APIs
- Content negotiation and versioning
- Industry-standard API design patterns

## REST Principles

### What is REST?

**REST** (Representational State Transfer) is an architectural style for distributed hypermedia systems.

**Key Principles:**

1. **Client-Server Architecture**: Separation of concerns
2. **Stateless**: Each request contains all necessary information
3. **Cacheable**: Responses can be cached
4. **Uniform Interface**: Consistent interaction pattern
5. **Layered System**: Components don't know beyond immediate layer
6. **Code on Demand** (optional): Server can extend client functionality

### REST Constraints

```
Resource-Based URLs
    ↓
HTTP Methods (GET, POST, PUT, DELETE)
    ↓
Representations (JSON, XML)
    ↓
Stateless Communication
    ↓
HATEOAS (Hypermedia)
```

### Resource Naming Conventions

```
✅ Good:
GET  /api/users              (Get all users)
GET  /api/users/123          (Get specific user)
POST /api/users              (Create user)
PUT  /api/users/123          (Update user)
DELETE /api/users/123        (Delete user)
GET  /api/users/123/orders   (Get user's orders)

❌ Bad:
GET  /api/getAllUsers
POST /api/createUser
GET  /api/user/get?id=123
POST /api/deleteUser
```

## Spring MVC Architecture

### Request Processing Flow

```
Client Request
    ↓
DispatcherServlet (Front Controller)
    ↓
HandlerMapping (Find controller)
    ↓
Controller (Process request)
    ↓
Service Layer (Business logic)
    ↓
Repository Layer (Data access)
    ↓
Service Layer (Return data)
    ↓
Controller (Return response)
    ↓
ViewResolver / MessageConverter
    ↓
Client Response
```

### Core Components

**1. DispatcherServlet**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // DispatcherServlet is auto-configured in Spring Boot
}
```

**2. Controller**
```java
@RestController
@RequestMapping("/api")
public class UserController {
    // Handle HTTP requests
}
```

**3. Service**
```java
@Service
public class UserService {
    // Business logic
}
```

**4. Repository**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Data access
}
```

## Building REST APIs

### @RestController vs @Controller

**@Controller** - Returns views (HTML pages)
```java
@Controller
public class WebController {
    
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("message", "Welcome");
        return "home"; // Returns view name
    }
}
```

**@RestController** - Returns data (JSON/XML)
```java
@RestController
@RequestMapping("/api")
public class ApiController {
    
    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.findAll(); // Returns JSON
    }
}

// @RestController = @Controller + @ResponseBody
```

### Basic REST Controller

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.findAll();
    }
    
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.findById(id);
    }
    
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productService.save(product);
    }
    
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productService.update(id, product);
    }
    
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.delete(id);
    }
}
```

## Request Handling

### @RequestMapping Variants

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // GET request
    @GetMapping
    public List<User> getAllUsers() { }
    
    // GET with path variable
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) { }
    
    // POST request
    @PostMapping
    public User createUser(@RequestBody User user) { }
    
    // PUT request
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) { }
    
    // PATCH request (partial update)
    @PatchMapping("/{id}")
    public User partialUpdate(@PathVariable Long id, @RequestBody Map<String, Object> updates) { }
    
    // DELETE request
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) { }
}
```

### Path Variables

```java
@RestController
@RequestMapping("/api")
public class OrderController {
    
    // Single path variable
    @GetMapping("/orders/{orderId}")
    public Order getOrder(@PathVariable Long orderId) {
        return orderService.findById(orderId);
    }
    
    // Multiple path variables
    @GetMapping("/users/{userId}/orders/{orderId}")
    public Order getUserOrder(@PathVariable Long userId, @PathVariable Long orderId) {
        return orderService.findByUserAndId(userId, orderId);
    }
    
    // Custom variable name
    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable("id") Long productId) {
        return productService.findById(productId);
    }
    
    // Optional path variable (using Optional)
    @GetMapping({"/items", "/items/{category}"})
    public List<Item> getItems(@PathVariable(required = false) Optional<String> category) {
        return category.isPresent() 
            ? itemService.findByCategory(category.get())
            : itemService.findAll();
    }
}
```

### Request Parameters

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    // Single parameter
    @GetMapping
    public List<Product> search(@RequestParam String name) {
        return productService.searchByName(name);
    }
    // GET /api/products?name=laptop
    
    // Multiple parameters
    @GetMapping("/filter")
    public List<Product> filter(
            @RequestParam String category,
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        return productService.filter(category, minPrice, maxPrice);
    }
    // GET /api/products/filter?category=electronics&minPrice=100&maxPrice=1000
    
    // Optional parameter
    @GetMapping("/search")
    public List<Product> search(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        return productService.search(keyword, page, size);
    }
    
    // Map all parameters
    @GetMapping("/dynamic-search")
    public List<Product> dynamicSearch(@RequestParam Map<String, String> params) {
        return productService.dynamicSearch(params);
    }
}
```

### Request Body

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // Simple request body
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.create(user);
    }
    
    // With validation
    @PostMapping("/validated")
    public User createValidatedUser(@Valid @RequestBody UserDTO userDTO) {
        return userService.create(userDTO);
    }
    
    // Partial JSON
    @PatchMapping("/{id}")
    public User partialUpdate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        return userService.partialUpdate(id, updates);
    }
}
```

### Request Headers

```java
@RestController
@RequestMapping("/api")
public class HeaderController {
    
    // Single header
    @GetMapping("/users")
    public List<User> getUsers(@RequestHeader("X-API-Key") String apiKey) {
        // Validate API key
        return userService.findAll();
    }
    
    // Multiple headers
    @GetMapping("/data")
    public ResponseEntity<String> getData(
            @RequestHeader("Authorization") String auth,
            @RequestHeader("Accept-Language") String language) {
        return ResponseEntity.ok("Data in " + language);
    }
    
    // Optional header
    @GetMapping("/info")
    public String getInfo(
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        return "User agent: " + userAgent;
    }
    
    // All headers
    @GetMapping("/headers")
    public String getAllHeaders(@RequestHeader Map<String, String> headers) {
        return headers.toString();
    }
}
```

### Request Cookies

```java
@RestController
public class CookieController {
    
    @GetMapping("/session")
    public String getSession(@CookieValue("JSESSIONID") String sessionId) {
        return "Session ID: " + sessionId;
    }
    
    @GetMapping("/preferences")
    public String getPreferences(
            @CookieValue(value = "theme", defaultValue = "light") String theme) {
        return "Theme: " + theme;
    }
}
```

## Response Handling

### ResponseEntity

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // Basic response
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Created response (201)
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.create(user);
        URI location = URI.create("/api/users/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }
    
    // No content (204)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    // Custom headers
    @GetMapping("/{id}/details")
    public ResponseEntity<User> getUserDetails(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok()
            .header("X-Custom-Header", "value")
            .header("X-User-Role", user.getRole())
            .body(user);
    }
    
    // Custom status
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        if (userService.exists(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
```

### @ResponseStatus

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201
    public Product createProduct(@RequestBody Product product) {
        return productService.create(product);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void deleteProduct(@PathVariable Long id) {
        productService.delete(id);
    }
}
```

## HTTP Methods

### Complete CRUD Example

```java
@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;
    
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
    
    // GET - Retrieve all books
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks(
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        List<Book> books = (author != null)
            ? bookService.findByAuthor(author, page, size)
            : bookService.findAll(page, size);
        
        return ResponseEntity.ok(books);
    }
    
    // GET - Retrieve single book
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable Long id) {
        return bookService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // POST - Create new book
    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody BookDTO bookDTO) {
        Book created = bookService.create(bookDTO);
        URI location = URI.create("/api/books/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }
    
    // PUT - Full update
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookDTO bookDTO) {
        
        return bookService.update(id, bookDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // PATCH - Partial update
    @PatchMapping("/{id}")
    public ResponseEntity<Book> partialUpdate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        
        return bookService.partialUpdate(id, updates)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // DELETE - Remove book
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        if (bookService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // HEAD - Check existence
    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkBook(@PathVariable Long id) {
        return bookService.exists(id)
            ? ResponseEntity.ok().build()
            : ResponseEntity.notFound().build();
    }
    
    // OPTIONS - Available methods
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> options() {
        return ResponseEntity.ok()
            .allow(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, 
                   HttpMethod.PATCH, HttpMethod.DELETE, HttpMethod.OPTIONS)
            .build();
    }
}
```

## Status Codes

### Common HTTP Status Codes

```java
@RestController
@RequestMapping("/api")
public class StatusCodeExamples {
    
    // 200 OK - Success
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }
    
    // 201 Created - Resource created
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    // 204 No Content - Success with no body
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    // 400 Bad Request - Invalid input
    @PostMapping("/validate")
    public ResponseEntity<String> validate(@RequestBody Data data) {
        if (!data.isValid()) {
            return ResponseEntity.badRequest().body("Invalid data");
        }
        return ResponseEntity.ok("Valid");
    }
    
    // 401 Unauthorized - Authentication required
    @GetMapping("/protected")
    public ResponseEntity<String> protectedResource(@RequestHeader("Authorization") String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok("Protected data");
    }
    
    // 403 Forbidden - No permission
    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<Void> deleteUserAsAdmin(@PathVariable Long id) {
        if (!securityService.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    // 404 Not Found - Resource not found
    @GetMapping("/users/{id}")
    public ResponseEntity<User> findUser(@PathVariable Long id) {
        return userService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // 409 Conflict - Resource already exists
    @PostMapping("/users")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("User already exists");
        }
        userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User created");
    }
    
    // 500 Internal Server Error - Server error
    @GetMapping("/process")
    public ResponseEntity<String> process() {
        try {
            service.complexOperation();
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Processing failed");
        }
    }
}
```

## Content Negotiation

### Accept Header

```java
@RestController
@RequestMapping("/api/users")
public class ContentNegotiationController {
    
    // Produces JSON (default)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> getUsersAsJson() {
        return userService.findAll();
    }
    
    // Produces XML
    @GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
    public List<User> getUsersAsXml() {
        return userService.findAll();
    }
    
    // Multiple formats
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<User> getUsers() {
        return userService.findAll();
    }
    
    // Consumes JSON
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public User createUser(@RequestBody User user) {
        return userService.create(user);
    }
    
    // Custom media type
    @GetMapping(produces = "application/vnd.company.user-v1+json")
    public UserV1 getUserV1(@PathVariable Long id) {
        return userService.findByIdV1(id);
    }
}
```

### Configuration

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(true)
            .parameterName("format")
            .ignoreAcceptHeader(false)
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType("xml", MediaType.APPLICATION_XML);
    }
}
```

## API Versioning

### 1. URI Versioning

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller {
    
    @GetMapping("/{id}")
    public UserV1 getUser(@PathVariable Long id) {
        return userService.findByIdV1(id);
    }
}

@RestController
@RequestMapping("/api/v2/users")
public class UserV2Controller {
    
    @GetMapping("/{id}")
    public UserV2 getUser(@PathVariable Long id) {
        return userService.findByIdV2(id);
    }
}
```

### 2. Request Parameter Versioning

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping(params = "version=1")
    public UserV1 getUserV1(@PathVariable Long id) {
        return userService.findByIdV1(id);
    }
    
    @GetMapping(params = "version=2")
    public UserV2 getUserV2(@PathVariable Long id) {
        return userService.findByIdV2(id);
    }
}
// GET /api/users?version=1
```

### 3. Header Versioning

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping(headers = "X-API-VERSION=1")
    public UserV1 getUserV1(@PathVariable Long id) {
        return userService.findByIdV1(id);
    }
    
    @GetMapping(headers = "X-API-VERSION=2")
    public UserV2 getUserV2(@PathVariable Long id) {
        return userService.findByIdV2(id);
    }
}
// Header: X-API-VERSION: 1
```

### 4. Media Type Versioning (Content Negotiation)

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping(produces = "application/vnd.company.user-v1+json")
    public UserV1 getUserV1(@PathVariable Long id) {
        return userService.findByIdV1(id);
    }
    
    @GetMapping(produces = "application/vnd.company.user-v2+json")
    public UserV2 getUserV2(@PathVariable Long id) {
        return userService.findByIdV2(id);
    }
}
// Header: Accept: application/vnd.company.user-v1+json
```

## HATEOAS

### Hypermedia as the Engine of Application State

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>
```

```java
import org.springframework.hateoas.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/users")
public class UserHateoasController {
    
    @GetMapping("/{id}")
    public EntityModel<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        
        EntityModel<User> resource = EntityModel.of(user);
        
        // Add self link
        resource.add(linkTo(methodOn(UserHateoasController.class).getUser(id)).withSelfRel());
        
        // Add related links
        resource.add(linkTo(methodOn(UserHateoasController.class).getAllUsers()).withRel("all-users"));
        resource.add(linkTo(methodOn(OrderController.class).getUserOrders(id)).withRel("orders"));
        
        return resource;
    }
    
    @GetMapping
    public CollectionModel<EntityModel<User>> getAllUsers() {
        List<EntityModel<User>> users = userService.findAll().stream()
            .map(user -> EntityModel.of(user,
                linkTo(methodOn(UserHateoasController.class).getUser(user.getId())).withSelfRel()))
            .collect(Collectors.toList());
        
        return CollectionModel.of(users,
            linkTo(methodOn(UserHateoasController.class).getAllUsers()).withSelfRel());
    }
}

// Response:
// {
//   "id": 1,
//   "name": "John Doe",
//   "_links": {
//     "self": { "href": "http://localhost:8080/api/users/1" },
//     "all-users": { "href": "http://localhost:8080/api/users" },
//     "orders": { "href": "http://localhost:8080/api/users/1/orders" }
//   }
// }
```

## Best Practices

### 1. RESTful URL Design

```java
// ✅ Good - Resource-based
GET    /api/users           // Get all users
GET    /api/users/123       // Get user 123
POST   /api/users           // Create user
PUT    /api/users/123       // Update user 123
DELETE /api/users/123       // Delete user 123
GET    /api/users/123/orders // Get orders for user 123

// ❌ Bad - Action-based
GET    /api/getUsers
POST   /api/createUser
GET    /api/getUserById?id=123
POST   /api/deleteUser
```

### 2. Use DTOs (Data Transfer Objects)

```java
// Entity
@Entity
public class User {
    @Id
    private Long id;
    private String email;
    private String password; // Should not be exposed
    private String role;
}

// DTO for response
public class UserDTO {
    private Long id;
    private String email;
    private String role;
    // No password field
}

// DTO for creation
public class CreateUserDTO {
    @NotBlank
    private String email;
    
    @Size(min = 8)
    private String password;
    
    private String role;
}

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public UserDTO getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return modelMapper.map(user, UserDTO.class);
    }
    
    @PostMapping
    public UserDTO createUser(@Valid @RequestBody CreateUserDTO dto) {
        User user = userService.create(dto);
        return modelMapper.map(user, UserDTO.class);
    }
}
```

### 3. Pagination

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping
    public Page<ProductDTO> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        
        Pageable pageable = PageRequest.of(page, size, direction, sortBy);
        return productService.findAll(pageable);
    }
}

// Response includes pagination metadata:
// {
//   "content": [...],
//   "totalElements": 100,
//   "totalPages": 10,
//   "size": 10,
//   "number": 0
// }
```

### 4. Filtering and Searching

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping("/search")
    public List<ProductDTO> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        
        return productService.search(name, category, minPrice, maxPrice);
    }
}
```

### 5. Error Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            errors,
            LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(error);
    }
}
```

## Complete Example

### E-Commerce Product API

```java
// Entity
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    private Integer stock;
    
    private String category;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Getters and setters
}

// DTO
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    
    // Getters and setters
}

public class CreateProductDTO {
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;
    
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;
    
    private String category;
    
    // Getters and setters
}

// Repository
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
}

// Service
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    
    public ProductService(ProductRepository productRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }
    
    public Page<ProductDTO> findAll(Pageable pageable) {
        return productRepository.findAll(pageable)
            .map(product -> modelMapper.map(product, ProductDTO.class));
    }
    
    public Optional<ProductDTO> findById(Long id) {
        return productRepository.findById(id)
            .map(product -> modelMapper.map(product, ProductDTO.class));
    }
    
    public ProductDTO create(CreateProductDTO dto) {
        Product product = modelMapper.map(dto, Product.class);
        product = productRepository.save(product);
        return modelMapper.map(product, ProductDTO.class);
    }
    
    public Optional<ProductDTO> update(Long id, CreateProductDTO dto) {
        return productRepository.findById(id)
            .map(product -> {
                product.setName(dto.getName());
                product.setDescription(dto.getDescription());
                product.setPrice(dto.getPrice());
                product.setStock(dto.getStock());
                product.setCategory(dto.getCategory());
                product = productRepository.save(product);
                return modelMapper.map(product, ProductDTO.class);
            });
    }
    
    public boolean delete(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public List<ProductDTO> search(String name, String category, 
                                   BigDecimal minPrice, BigDecimal maxPrice) {
        // Implement dynamic search logic
        List<Product> products = productRepository.findAll();
        
        if (name != null) {
            products = productRepository.findByNameContainingIgnoreCase(name);
        }
        
        return products.stream()
            .map(product -> modelMapper.map(product, ProductDTO.class))
            .collect(Collectors.toList());
    }
}

// Controller
@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<ProductDTO> products = productService.findAll(pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        return productService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductDTO dto) {
        ProductDTO created = productService.create(dto);
        URI location = URI.create("/api/products/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductDTO dto) {
        
        return productService.update(id, dto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        
        List<ProductDTO> products = productService.search(name, category, minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }
}
```

## Exercises

### Exercise 1: Basic CRUD API
Create a REST API for managing books with:
- GET /api/books - List all books
- GET /api/books/{id} - Get book by ID
- POST /api/books - Create new book
- PUT /api/books/{id} - Update book
- DELETE /api/books/{id} - Delete book

### Exercise 2: Advanced Features
Enhance the books API with:
- Pagination and sorting
- Search by title or author
- Filter by category and price range
- Input validation

### Exercise 3: API Versioning
Implement two versions of an API:
- v1: Returns basic user info
- v2: Returns extended user info with additional fields
Use URI versioning

### Exercise 4: HATEOAS
Add HATEOAS links to your book API:
- Self link
- Link to all books
- Link to author details
- Link to related books

### Exercise 5: Complete E-Commerce API
Build a mini e-commerce system with:
- Product management
- Category management
- Shopping cart (session-scoped)
- Order management
- Proper error handling and validation

## Key Takeaways

✅ REST is an architectural style, not a protocol  
✅ Use **@RestController** for REST APIs  
✅ Follow **RESTful URL conventions** (resource-based, not action-based)  
✅ Use appropriate **HTTP methods** (GET, POST, PUT, PATCH, DELETE)  
✅ Return correct **HTTP status codes**  
✅ Use **DTOs** to control data exposure  
✅ Implement **pagination** for large datasets  
✅ Use **content negotiation** for multiple formats  
✅ Version your APIs for backward compatibility  
✅ Apply **HATEOAS** for discoverable APIs  
✅ Handle **errors consistently** with @RestControllerAdvice  
✅ Validate input with **@Valid** and Bean Validation  

## Resources

### Official Documentation
- [Spring MVC Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html)
- [Spring REST Documentation](https://spring.io/guides/tutorials/rest/)
- [Spring HATEOAS](https://spring.io/projects/spring-hateoas)

### REST Best Practices
- [RESTful API Design Best Practices](https://restfulapi.net/)
- [Microsoft REST API Guidelines](https://github.com/microsoft/api-guidelines)
- [Google API Design Guide](https://cloud.google.com/apis/design)

### Books
- "REST in Practice" by Jim Webber
- "RESTful Web Services" by Leonard Richardson
- "Spring in Action" by Craig Walls

### Tools
- [Postman](https://www.postman.com/) - API testing
- [Swagger/OpenAPI](https://swagger.io/) - API documentation
- [HTTPie](https://httpie.io/) - Command-line HTTP client

## Next Steps

Tomorrow, we'll dive into **JPA and Database Integration** where we'll persist our REST API data to databases!

---
[<< Previous: Day 04](../Day04_DI_IoC/README.md) | [Back to Main](../README.md) | [Next: Day 06 >>](../Day06_JPA_Database/README.md)
