# Day 03: Spring Boot Basics and Annotations

## 📋 Table of Contents
- [Introduction](#introduction)
- [Understanding Spring vs Spring Boot](#understanding-spring-vs-spring-boot)
- [@Bean vs @Component - The Complete Guide](#bean-vs-component---the-complete-guide)
- [Core Spring Annotations](#core-spring-annotations)
- [Stereotype Annotations Deep Dive](#stereotype-annotations-deep-dive)
- [Web Annotations](#web-annotations)
- [Component Scanning](#component-scanning)
- [Bean Lifecycle](#bean-lifecycle)
- [Practical Examples](#practical-examples)
- [Exercises](#exercises)
- [Resources](#resources)

## Introduction

Welcome to Day 3! Today we'll explore the fundamental annotations that power Spring Boot applications. Understanding these annotations is crucial for building robust Spring Boot applications.

**What you'll learn in-depth:**
- The key differences between Spring and Spring Boot
- When to use `@Bean` vs `@Component` (and why)
- How annotations evolved from Spring to Spring Boot
- The complete hierarchy of stereotype annotations
- Best practices for annotation usage in enterprise applications

---

## Understanding Spring vs Spring Boot

### What is Spring Framework?

**Spring Framework** is a comprehensive, modular Java framework that provides infrastructure support for developing Java applications. It was created by Rod Johnson in 2003 as a response to the complexity of J2EE (Java 2 Enterprise Edition).

**Core Philosophy:** Inversion of Control (IoC) and Dependency Injection (DI)

**The Problem Spring Solved:**
Before Spring, Java enterprise applications required:
- Complex XML configurations
- Heavy EJB (Enterprise JavaBeans) containers
- Tight coupling between components
- Difficulty in testing

### What is Spring Boot?

**Spring Boot** is an opinionated framework built ON TOP of Spring Framework. Created by Pivotal (now VMware) in 2014, it simplifies Spring application development by providing:

1. **Auto-configuration**: Automatically configures your application based on dependencies
2. **Embedded servers**: No need for external application servers
3. **Starter dependencies**: Pre-packaged dependency descriptors
4. **Production-ready features**: Health checks, metrics, externalized configuration

### Spring vs Spring Boot: Key Differences

| Aspect | Spring Framework | Spring Boot |
|--------|-----------------|-------------|
| **Configuration** | Manual XML or Java-based configuration | Auto-configuration with sensible defaults |
| **Dependency Management** | Manual dependency resolution | Starter POMs with compatible versions |
| **Embedded Server** | Requires external server (Tomcat, Jetty) | Embedded server included |
| **Setup Time** | Complex, time-consuming | Quick and minimal |
| **XML Configuration** | Often required | Minimal to none |
| **Boilerplate Code** | Significant amount | Minimal |
| **Learning Curve** | Steep | Gentle (but Spring knowledge helps) |
| **Flexibility** | Maximum flexibility | Convention over configuration |
| **Production Features** | Custom setup needed | Built-in (Actuator) |

### How Configuration Evolved

**Traditional Spring (XML-based):**
```xml
<!-- applicationContext.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- Define a bean manually -->
    <bean id="emailService" class="com.example.EmailService">
        <property name="smtpHost" value="smtp.example.com"/>
        <property name="smtpPort" value="587"/>
    </bean>

    <!-- Define another bean with dependency -->
    <bean id="userService" class="com.example.UserService">
        <constructor-arg ref="emailService"/>
    </bean>

</beans>
```

**Spring with Java Configuration (Spring 3.0+):**
```java
@Configuration
public class AppConfig {

    @Bean
    public EmailService emailService() {
        EmailService service = new EmailService();
        service.setSmtpHost("smtp.example.com");
        service.setSmtpPort(587);
        return service;
    }

    @Bean
    public UserService userService() {
        return new UserService(emailService());
    }
}
```

**Spring Boot (Auto-configuration + Annotations):**
```java
// No explicit configuration needed for most cases!
// Spring Boot auto-configures based on classpath

@Service  // Automatically detected and registered as a bean
public class EmailService {
    
    @Value("${smtp.host}")  // Values from application.properties
    private String smtpHost;
    
    @Value("${smtp.port}")
    private int smtpPort;
    
    public void sendEmail(String to, String message) {
        // Implementation
    }
}

@Service
public class UserService {
    
    private final EmailService emailService;
    
    // Constructor injection - no @Autowired needed in Spring Boot
    public UserService(EmailService emailService) {
        this.emailService = emailService;
    }
}
```

---

## @Bean vs @Component - The Complete Guide

This is one of the most important concepts to understand in Spring/Spring Boot. Let's break it down completely.

### What is @Component?

**@Component** is a class-level annotation that tells Spring: "This class should be managed by the Spring container as a bean." It enables automatic detection through classpath scanning.

```java
@Component
public class EmailService {
    public void sendEmail(String to, String message) {
        System.out.println("Sending email to: " + to);
    }
}
```

**How it works:**
1. Spring scans the classpath for classes annotated with `@Component`
2. For each found class, Spring creates a bean definition
3. Spring instantiates the class and manages its lifecycle
4. The bean is available for injection into other components

**When to use @Component:**
- When YOU write the class
- When the class has a single, obvious configuration
- When using Spring Boot's auto-configuration
- For general-purpose components that don't fit @Service, @Repository, or @Controller

### What is @Bean?

**@Bean** is a method-level annotation used inside `@Configuration` classes. It tells Spring: "The object returned by this method should be managed as a bean."

```java
@Configuration
public class AppConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate template = new RestTemplate();
        template.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        return template;
    }
}
```

**How it works:**
1. Spring processes `@Configuration` classes
2. For each `@Bean` method, Spring calls the method
3. The returned object is registered as a bean
4. The method name becomes the bean name (by default)

**When to use @Bean:**
- When you DON'T control the source code (third-party libraries)
- When you need complex initialization logic
- When you need multiple beans of the same type with different configurations
- When you need conditional bean creation
- When migrating from XML-based configuration

### @Bean vs @Component: Complete Comparison

| Aspect | @Bean | @Component |
|--------|-------|------------|
| **Level** | Method-level | Class-level |
| **Location** | Inside @Configuration class | On any class |
| **Source Code Control** | Not needed (third-party classes) | Must control the class |
| **Detection** | Explicit declaration | Classpath scanning |
| **Flexibility** | High (complex initialization) | Low (constructor only) |
| **Multiple Instances** | Easy (multiple @Bean methods) | Difficult |
| **Conditional Creation** | Easy with @Conditional | Limited |
| **Naming** | Method name or @Bean(name=...) | Class name or @Component(value=...) |

### Practical Examples: When to Use Each

**Use @Component when you own the class:**
```java
// ✅ Use @Component - you wrote this class
@Component
public class OrderProcessor {
    
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    
    public OrderProcessor(PaymentService paymentService, InventoryService inventoryService) {
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
    }
    
    public void processOrder(Order order) {
        inventoryService.reserve(order.getItems());
        paymentService.charge(order.getTotal());
    }
}
```

**Use @Bean for third-party classes:**
```java
@Configuration
public class ThirdPartyConfig {
    
    // ✅ Use @Bean - RestTemplate is from Spring (you can't add @Component to it)
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    // ✅ Use @Bean - ObjectMapper is from Jackson library
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
    
    // ✅ Use @Bean - ModelMapper is a third-party library
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return mapper;
    }
}
```

**Use @Bean for complex initialization:**
```java
@Configuration
public class DatabaseConfig {
    
    @Value("${db.url}")
    private String dbUrl;
    
    @Value("${db.username}")
    private String username;
    
    @Value("${db.password}")
    private String password;
    
    // ✅ Use @Bean - complex configuration required
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        // Custom validation query
        config.setConnectionTestQuery("SELECT 1");
        
        return new HikariDataSource(config);
    }
}
```

**Use @Bean for multiple instances:**
```java
@Configuration
public class RestClientConfig {
    
    // ✅ Multiple beans of same type with different configurations
    @Bean(name = "internalApiClient")
    public RestTemplate internalApiClient() {
        RestTemplate template = new RestTemplate();
        template.setRequestFactory(new SimpleClientHttpRequestFactory());
        ((SimpleClientHttpRequestFactory) template.getRequestFactory()).setConnectTimeout(5000);
        ((SimpleClientHttpRequestFactory) template.getRequestFactory()).setReadTimeout(5000);
        return template;
    }
    
    @Bean(name = "externalApiClient")
    public RestTemplate externalApiClient() {
        RestTemplate template = new RestTemplate();
        template.setRequestFactory(new SimpleClientHttpRequestFactory());
        ((SimpleClientHttpRequestFactory) template.getRequestFactory()).setConnectTimeout(30000);
        ((SimpleClientHttpRequestFactory) template.getRequestFactory()).setReadTimeout(60000);
        // Add interceptors for external API
        template.setInterceptors(Collections.singletonList(new LoggingInterceptor()));
        return template;
    }
}

// Usage
@Service
public class ApiService {
    
    private final RestTemplate internalApiClient;
    private final RestTemplate externalApiClient;
    
    public ApiService(@Qualifier("internalApiClient") RestTemplate internalApiClient,
                      @Qualifier("externalApiClient") RestTemplate externalApiClient) {
        this.internalApiClient = internalApiClient;
        this.externalApiClient = externalApiClient;
    }
}
```

### The Evolution: XML → @Bean → @Component

**1990s-2000s: XML Configuration Era**
```xml
<!-- Every bean had to be declared in XML -->
<bean id="emailService" class="com.example.EmailService"/>
<bean id="userService" class="com.example.UserService">
    <property name="emailService" ref="emailService"/>
</bean>
```

**2009-2014: @Configuration and @Bean Era (Spring 3.0+)**
```java
// Java-based configuration replaced XML
@Configuration
public class AppConfig {
    @Bean
    public EmailService emailService() { return new EmailService(); }
    
    @Bean
    public UserService userService() { return new UserService(emailService()); }
}
```

**2014-Present: @Component and Auto-Configuration Era (Spring Boot)**
```java
// Minimal configuration - Spring Boot handles the rest
@Service
public class EmailService { }

@Service  
public class UserService {
    private final EmailService emailService;
    public UserService(EmailService emailService) {
        this.emailService = emailService;
    }
}
// That's it! No @Configuration class needed for simple cases
```

---

## Core Spring Annotations

### @Component

**What it is:** The most generic stereotype annotation that marks a class as a Spring-managed component (bean).

**In Traditional Spring:**
```xml
<!-- You had to declare it in XML -->
<context:component-scan base-package="com.example"/>

<!-- Or explicitly define the bean -->
<bean id="emailService" class="com.example.EmailService"/>
```

**In Spring Boot:**
```java
@Component
public class EmailService {
    public void sendEmail(String to, String message) {
        System.out.println("Sending email to: " + to);
    }
}
// Automatically detected! No XML needed.
```

**Why it exists:**
- Enables classpath scanning
- Reduces XML configuration
- Marks classes for Spring container management
- Foundation for other stereotype annotations

---

## Stereotype Annotations Deep Dive

Spring provides specialized versions of `@Component` for different application layers. These are called **stereotype annotations**.

### Hierarchy of Stereotype Annotations

```
                    @Component (Base annotation)
                         │
        ┌────────────────┼────────────────┐
        │                │                │
    @Service       @Repository      @Controller
    (Business       (Data           (Web Layer)
     Logic)         Access)              │
                                         │
                                  @RestController
                                  (@Controller + @ResponseBody)
```

### @Service - Business Logic Layer

**What it is:** A specialization of `@Component` for service layer classes that contain business logic.

**What it does:**
- Same functionality as `@Component` (creates a bean)
- Indicates the class is a service/business logic holder
- Better semantics and code readability
- May have additional framework support in future Spring versions

**Why use @Service instead of @Component?**

1. **Semantic Clarity:** Clearly indicates the class role
2. **Layer Separation:** Enforces architectural boundaries
3. **Future-proofing:** Spring may add specific behavior later
4. **AOP Targeting:** Easier to apply aspects to specific layers

```java
// ❌ Less clear
@Component
public class UserService { }

// ✅ Clear intent
@Service
public class UserService { }
```

**Complete Example:**
```java
@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    // Constructor injection (best practice in Spring Boot)
    public UserService(UserRepository userRepository, 
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }
    
    @Transactional
    public User createUser(CreateUserRequest request) {
        // Validation
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }
        
        // Business logic
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        
        User savedUser = userRepository.save(user);
        
        // Side effects
        emailService.sendVerificationEmail(savedUser);
        
        return savedUser;
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
}
```

### @Repository - Data Access Layer

**What it is:** A specialization of `@Component` for Data Access Object (DAO) classes.

**What it does (IMPORTANT - this is unique to @Repository):**

1. **Bean Registration:** Same as `@Component`
2. **Exception Translation:** Automatically translates database-specific exceptions to Spring's `DataAccessException` hierarchy
3. **Persistence Context:** Indicates the class interacts with databases

**Exception Translation Explained:**

Without @Repository:
```java
// Raw JDBC - throws SQLException
// JPA - throws PersistenceException
// Different databases throw different exceptions
```

With @Repository:
```java
// All exceptions are translated to DataAccessException hierarchy
try {
    userRepository.save(user);
} catch (DataIntegrityViolationException e) {
    // Works regardless of whether you use MySQL, PostgreSQL, MongoDB
}
```

**Spring Boot vs Traditional Spring:**

**Traditional Spring (required explicit configuration):**
```xml
<!-- Required to enable exception translation -->
<bean class="org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor"/>
```

**Spring Boot (auto-configured):**
```java
@Repository
public class UserRepository {
    // Exception translation works automatically!
}
```

**Complete Example:**
```java
// Custom repository (not extending Spring Data interfaces)
@Repository
public class CustomUserRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public List<User> findActiveUsersByRole(String role) {
        return entityManager.createQuery(
            "SELECT u FROM User u WHERE u.status = :status AND u.role = :role", 
            User.class)
            .setParameter("status", UserStatus.ACTIVE)
            .setParameter("role", role)
            .getResultList();
    }
    
    public void batchInsert(List<User> users) {
        int batchSize = 50;
        for (int i = 0; i < users.size(); i++) {
            entityManager.persist(users.get(i));
            if (i > 0 && i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
    }
}

// Spring Data JPA repository (most common in Spring Boot)
@Repository  // Optional - Spring Data repositories are automatically treated as repositories
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    List<User> findByStatusAndCreatedAtAfter(UserStatus status, LocalDateTime date);
    
    @Query("SELECT u FROM User u WHERE u.department.id = :deptId AND u.status = :status")
    List<User> findByDepartmentAndStatus(@Param("deptId") Long deptId, 
                                         @Param("status") UserStatus status);
}
```

### @Controller vs @RestController

**What is @Controller?**

A specialization of `@Component` for web controllers that handle HTTP requests and return **views** (HTML pages).

**What is @RestController?**

A convenience annotation that combines `@Controller` + `@ResponseBody`. It's designed for REST APIs that return **data** (JSON/XML).

**Mathematical Relationship:**
```
@RestController = @Controller + @ResponseBody
```

**Detailed Comparison:**

| Aspect | @Controller | @RestController |
|--------|-------------|-----------------|
| **Returns** | View names (HTML templates) | Data (JSON/XML) |
| **Response Body** | Requires @ResponseBody per method | Automatic for all methods |
| **Use Case** | Server-side rendered pages | REST APIs |
| **View Resolver** | Uses ViewResolver | Not used |
| **Content-Type** | text/html | application/json (default) |
| **Spring MVC** | Traditional MVC | RESTful services |

**@Controller Example (MVC/Thymeleaf):**
```java
@Controller
public class WebController {
    
    private final UserService userService;
    
    public WebController(UserService userService) {
        this.userService = userService;
    }
    
    // Returns a view name - Thymeleaf looks for templates/home.html
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Welcome to our website!");
        return "home";  // View name
    }
    
    // Returns a view with data
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "users/list";  // templates/users/list.html
    }
    
    // Form handling
    @PostMapping("/users")
    public String createUser(@Valid @ModelAttribute UserForm form, 
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "users/create";  // Return to form with errors
        }
        userService.createUser(form);
        redirectAttributes.addFlashAttribute("message", "User created successfully!");
        return "redirect:/users";  // Redirect after POST
    }
    
    // If you need to return JSON from a @Controller, use @ResponseBody
    @GetMapping("/api/user-count")
    @ResponseBody  // Required to return JSON instead of a view
    public Map<String, Long> getUserCount() {
        return Map.of("count", userService.count());
    }
}
```

**@RestController Example (REST API):**
```java
@RestController
@RequestMapping("/api/v1/users")
public class UserApiController {
    
    private final UserService userService;
    
    public UserApiController(UserService userService) {
        this.userService = userService;
    }
    
    // All methods automatically return JSON
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return userService.findById(id)
            .map(user -> ResponseEntity.ok(toDTO(user)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        URI location = URI.create("/api/v1/users/" + user.getId());
        return ResponseEntity.created(location).body(toDTO(user));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, 
                                              @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request)
            .map(user -> ResponseEntity.ok(toDTO(user)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    private UserDTO toDTO(User user) {
        return new UserDTO(user.getId(), user.getEmail(), user.getName());
    }
}
```

**Evolution from Spring to Spring Boot:**

**Traditional Spring MVC (required extensive configuration):**
```xml
<!-- dispatcher-servlet.xml -->
<mvc:annotation-driven/>

<bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
    <property name="prefix" value="/WEB-INF/views/"/>
    <property name="suffix" value=".jsp"/>
</bean>

<!-- For JSON support -->
<bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter"/>
```

**Spring Boot (auto-configured):**
```java
// That's it! Just add the annotation.
// JSON serialization, content negotiation, etc. are auto-configured
@RestController
@RequestMapping("/api/users")
public class UserController {
    // ...
}
```

---

## Web Annotations

### Evolution of Web Annotations: Spring to Spring Boot

**Traditional Spring MVC Configuration (XML era):**
```xml
<!-- web.xml - Every Spring web app needed this -->
<servlet>
    <servlet-name>dispatcher</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/dispatcher-servlet.xml</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
</servlet>

<servlet-mapping>
    <servlet-name>dispatcher</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>
```

**Spring Boot (Zero XML):**
```java
// Just run! Everything is auto-configured.
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Request Mapping Annotations

#### @RequestMapping - The Foundation

**What it is:** The original annotation for mapping HTTP requests to handler methods. It works in both Spring MVC and Spring Boot.

**Spring Evolution:**
- **Spring 2.5:** Introduced `@RequestMapping`
- **Spring 4.3:** Added shortcut annotations (`@GetMapping`, `@PostMapping`, etc.)
- **Spring Boot:** Same annotations, but with auto-configuration

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    // Old style (still works, but verbose)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Product getProduct(@PathVariable Long id) {
        return productService.findById(id);
    }
    
    // Modern style (Spring 4.3+, recommended)
    @GetMapping("/{id}")
    public Product getProductModern(@PathVariable Long id) {
        return productService.findById(id);
    }
}
```

#### HTTP Method Specific Annotations

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping          // GET /api/users
    public List<User> getAllUsers() {
        return userService.findAll();
    }
    
    @GetMapping("/{id}") // GET /api/users/123
    public User getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }
    
    @PostMapping         // POST /api/users
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }
    
    @PutMapping("/{id}") // PUT /api/users/123
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.update(id, user);
    }
    
    @DeleteMapping("/{id}") // DELETE /api/users/123
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
    
    @PatchMapping("/{id}") // PATCH /api/users/123
    public User partialUpdate(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return userService.partialUpdate(id, updates);
    }
}
```

### Parameter Annotations

#### @PathVariable
Extracts values from URI path.

```java
@GetMapping("/users/{id}/orders/{orderId}")
public Order getOrder(@PathVariable Long id, @PathVariable Long orderId) {
    return orderService.findByUserIdAndOrderId(id, orderId);
}

// With custom name
@GetMapping("/users/{userId}")
public User getUser(@PathVariable("userId") Long id) {
    return userService.findById(id);
}
```

#### @RequestParam
Extracts query parameters from URL.

```java
// GET /api/search?name=John&age=25
@GetMapping("/search")
public List<User> search(
    @RequestParam String name,
    @RequestParam int age) {
    return userService.search(name, age);
}

// Optional parameters
@GetMapping("/search")
public List<User> search(
    @RequestParam(required = false) String name,
    @RequestParam(defaultValue = "0") int age) {
    return userService.search(name, age);
}
```

#### @RequestBody
Binds HTTP request body to Java object.

```java
@PostMapping("/users")
public User createUser(@RequestBody User user) {
    return userService.save(user);
}

// Request body:
// {
//   "name": "John Doe",
//   "email": "john@example.com",
//   "age": 30
// }
```

#### @RequestHeader
Extracts HTTP headers.

```java
@GetMapping("/info")
public String getInfo(
    @RequestHeader("User-Agent") String userAgent,
    @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {
    return "User Agent: " + userAgent + ", Language: " + language;
}
```

## Dependency Injection Annotations

### Understanding Dependency Injection: Spring vs Spring Boot

**What is Dependency Injection?**

Dependency Injection (DI) is a design pattern where dependencies are "injected" into a class rather than being created by the class itself. This promotes loose coupling and makes testing easier.

**Evolution of DI in Spring:**

| Era | Approach | Example |
|-----|----------|---------|
| **Spring 1.x-2.x** | XML-based configuration | `<bean>` with `<property>` |
| **Spring 2.5+** | Annotation-based | `@Autowired` required everywhere |
| **Spring 4.3+** | Implicit constructor injection | Single constructor = auto-wired |
| **Spring Boot** | Same as Spring 4.3+ with auto-configuration | Minimal annotations |

### @Autowired - The Complete Guide

**What it does:** Tells Spring to inject a dependency automatically.

**Spring Boot Improvement:** In Spring Boot (and Spring 4.3+), `@Autowired` is **optional** for single-constructor classes!

**Evolution Example:**

```java
// Traditional Spring (pre-4.3) - @Autowired required
@Service
public class OrderService {
    
    private final UserService userService;
    
    @Autowired  // Required!
    public OrderService(UserService userService) {
        this.userService = userService;
    }
}

// Spring Boot / Spring 4.3+ - @Autowired optional
@Service
public class OrderService {
    
    private final UserService userService;
    
    // No @Autowired needed! Spring infers it.
    public OrderService(UserService userService) {
        this.userService = userService;
    }
}
```

### Three Types of Dependency Injection

**1. Constructor Injection (RECOMMENDED in Spring Boot)**
```java
@Service
public class OrderService {
    
    private final UserService userService;
    private final ProductService productService;
    private final PaymentService paymentService;
    
    // Constructor injection - Best Practice
    // Benefits:
    // ✅ Immutable (final fields)
    // ✅ Required dependencies are obvious
    // ✅ Easy to test (just pass mocks to constructor)
    // ✅ Fails fast if dependency is missing
    // ✅ Thread-safe
    public OrderService(UserService userService, 
                        ProductService productService,
                        PaymentService paymentService) {
        this.userService = userService;
        this.productService = productService;
        this.paymentService = paymentService;
    }
}
```

**2. Field Injection (NOT Recommended)**
```java
@Service
public class OrderService {
    
    // Field injection - Avoid in production code
    // Problems:
    // ❌ Cannot make fields final (mutable)
    // ❌ Hidden dependencies (not visible from outside)
    // ❌ Hard to test (requires reflection or Spring context)
    // ❌ Breaks encapsulation
    // ❌ Easy to add too many dependencies (code smell hidden)
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    // Only acceptable uses:
    // - Test classes
    // - Spring configuration classes
    // - Quick prototyping
}
```

**3. Setter Injection (For Optional Dependencies)**
```java
@Service
public class NotificationService {
    
    private EmailService emailService;
    private SmsService smsService;  // Optional
    
    // Constructor for required dependencies
    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    // Setter for optional dependencies
    // Use case: Feature that may or may not be available
    @Autowired(required = false)
    public void setSmsService(SmsService smsService) {
        this.smsService = smsService;
    }
    
    public void notify(User user, String message) {
        emailService.send(user.getEmail(), message);
        
        if (smsService != null && user.getPhone() != null) {
            smsService.send(user.getPhone(), message);
        }
    }
}
```

### @Qualifier - Disambiguating Multiple Beans

**When needed:** When multiple beans of the same type exist.

**Problem Scenario:**
```java
public interface PaymentProcessor {
    void process(Payment payment);
}

@Service
public class StripePaymentProcessor implements PaymentProcessor {
    @Override
    public void process(Payment payment) {
        // Stripe implementation
    }
}

@Service
public class PayPalPaymentProcessor implements PaymentProcessor {
    @Override
    public void process(Payment payment) {
        // PayPal implementation
    }
}

@Service
public class OrderService {
    private final PaymentProcessor paymentProcessor;
    
    // ❌ Error! Spring doesn't know which one to inject
    public OrderService(PaymentProcessor paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }
}
```

**Solution with @Qualifier:**
```java
@Service
@Qualifier("stripe")  // or use @Component("stripe")
public class StripePaymentProcessor implements PaymentProcessor { }

@Service
@Qualifier("paypal")
public class PayPalPaymentProcessor implements PaymentProcessor { }

@Service
public class OrderService {
    private final PaymentProcessor paymentProcessor;
    
    // ✅ Specify which implementation to use
    public OrderService(@Qualifier("stripe") PaymentProcessor paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }
}
```

### @Primary
Marks a bean as primary when multiple candidates exist.

**When to use:**
- When you want a default bean that's used in most cases
- Combined with `@Qualifier` for specific overrides

```java
@Service
@Primary  // This is the default implementation
public class StripePaymentProcessor implements PaymentProcessor {
    @Override
    public void process(Payment payment) {
        // Stripe is our primary payment processor
    }
}

@Service
public class PayPalPaymentProcessor implements PaymentProcessor {
    @Override
    public void process(Payment payment) {
        // PayPal is an alternative
    }
}

@Service
public class OrderService {
    private final PaymentProcessor paymentProcessor;
    
    // ✅ Stripe is injected (it's @Primary)
    public OrderService(PaymentProcessor paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }
}

@Service
public class RefundService {
    private final PaymentProcessor paymentProcessor;
    
    // ✅ Use @Qualifier to override @Primary
    public RefundService(@Qualifier("payPalPaymentProcessor") PaymentProcessor paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }
}
```

---

## Configuration Annotations

### @Configuration - The Deep Dive

**What it is:** Marks a class as a source of bean definitions. It's the Java-based alternative to XML configuration.

**Spring vs Spring Boot:**

| Aspect | Traditional Spring | Spring Boot |
|--------|-------------------|-------------|
| **Purpose** | Required for all bean definitions | Only needed for custom/third-party beans |
| **Amount** | Many @Configuration classes | Minimal (auto-configuration handles most) |
| **Typical Use** | All beans defined here | Third-party libraries, custom config |

**How @Configuration Works (CGLIB Proxying):**

```java
@Configuration
public class AppConfig {
    
    @Bean
    public UserRepository userRepository() {
        return new UserRepository(dataSource());
    }
    
    @Bean
    public UserService userService() {
        return new UserService(userRepository());  // Calls userRepository()
    }
    
    @Bean
    public OrderService orderService() {
        return new OrderService(userRepository());  // Calls userRepository() again
    }
    
    @Bean
    public DataSource dataSource() {
        return new HikariDataSource();
    }
}
```

**Important:** Even though `userRepository()` is called twice, Spring ensures only ONE instance is created. This is because `@Configuration` classes are proxied with CGLIB.

**@Configuration vs @Component:**

```java
// @Configuration - CGLIB proxied, method calls return same bean
@Configuration
public class AppConfig {
    @Bean
    public ServiceA serviceA() { return new ServiceA(commonBean()); }
    
    @Bean
    public ServiceB serviceB() { return new ServiceB(commonBean()); }
    
    @Bean
    public CommonBean commonBean() { return new CommonBean(); }
    // ✅ serviceA and serviceB share the SAME CommonBean instance
}

// @Component with @Bean - Not proxied (lite mode), each call creates new instance
@Component
public class AppConfig {
    @Bean
    public ServiceA serviceA() { return new ServiceA(commonBean()); }
    
    @Bean
    public ServiceB serviceB() { return new ServiceB(commonBean()); }
    
    @Bean
    public CommonBean commonBean() { return new CommonBean(); }
    // ❌ serviceA and serviceB get DIFFERENT CommonBean instances!
}
```

### @Value - Injecting Configuration Properties

**What it does:** Injects values from property files, environment variables, or SpEL expressions.

**Spring Boot Enhancement:** Works seamlessly with `application.properties` / `application.yml`

```properties
# application.properties
app.name=My Application
app.version=2.0.0
app.max-users=1000
app.feature.email-enabled=true
```

```java
@Component
public class AppSettings {
    
    // Basic property injection
    @Value("${app.name}")
    private String appName;
    
    // With default value (if property not found)
    @Value("${app.version:1.0.0}")
    private String version;
    
    // Numeric property
    @Value("${app.max-users:100}")
    private int maxUsers;
    
    // Boolean property
    @Value("${app.feature.email-enabled:false}")
    private boolean emailEnabled;
    
    // Environment variable (with default)
    @Value("${DATABASE_URL:jdbc:h2:mem:testdb}")
    private String databaseUrl;
    
    // SpEL expression
    @Value("#{${app.max-users} * 2}")
    private int doubleMaxUsers;
    
    // Inject list from comma-separated values
    @Value("${app.allowed-origins:http://localhost:3000}")
    private List<String> allowedOrigins;
}
```

### @ConfigurationProperties - Type-Safe Configuration (Spring Boot Feature)

**What it is:** A Spring Boot-specific annotation that binds external configuration to a Java object in a type-safe manner.

**Why it's better than @Value:**

| Aspect | @Value | @ConfigurationProperties |
|--------|--------|--------------------------|
| **Type Safety** | String-based, error-prone | Type-safe, compile-time checking |
| **Validation** | No built-in validation | Supports JSR-380 validation |
| **Nested Objects** | Manual, complex | Automatic binding |
| **Relaxed Binding** | Exact match required | Flexible (kebab-case, camelCase, etc.) |
| **Documentation** | Hard to document | Self-documenting POJOs |
| **IDE Support** | Limited | Full support with spring-boot-configuration-processor |

**Example - From @Value to @ConfigurationProperties:**

```yaml
# application.yml
app:
  name: My Application
  version: 2.0.0
  server:
    host: localhost
    port: 8080
  security:
    enabled: true
    token-expiration-seconds: 3600
    allowed-origins:
      - http://localhost:3000
      - http://localhost:4200
```

**Old way with @Value (verbose and error-prone):**
```java
@Component
public class AppConfig {
    @Value("${app.name}") private String name;
    @Value("${app.version}") private String version;
    @Value("${app.server.host}") private String serverHost;
    @Value("${app.server.port}") private int serverPort;
    @Value("${app.security.enabled}") private boolean securityEnabled;
    @Value("${app.security.token-expiration-seconds}") private long tokenExpiration;
    // Difficult to handle lists and nested objects!
}
```

**Modern way with @ConfigurationProperties (clean and type-safe):**
```java
@Configuration
@ConfigurationProperties(prefix = "app")
@Validated  // Enable validation
public class AppProperties {
    
    @NotBlank
    private String name;
    
    @NotBlank
    private String version;
    
    @Valid
    private Server server = new Server();
    
    @Valid
    private Security security = new Security();
    
    // Getters and Setters
    
    public static class Server {
        private String host = "localhost";
        
        @Min(1) @Max(65535)
        private int port = 8080;
        
        // Getters and Setters
    }
    
    public static class Security {
        private boolean enabled = false;
        
        @Positive
        private long tokenExpirationSeconds = 3600;
        
        private List<String> allowedOrigins = new ArrayList<>();
        
        // Getters and Setters
    }
}

// Usage
@Service
public class AuthService {
    private final AppProperties appProperties;
    
    public AuthService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }
    
    public void checkConfig() {
        System.out.println("App: " + appProperties.getName());
        System.out.println("Security: " + appProperties.getSecurity().isEnabled());
        System.out.println("Token expires in: " + appProperties.getSecurity().getTokenExpirationSeconds());
    }
}
```

**Enabling @ConfigurationProperties in Spring Boot:**
```java
// Option 1: On the properties class itself
@Configuration  // Makes it a bean
@ConfigurationProperties(prefix = "app")
public class AppProperties { }

// Option 2: Enable on main application (scan for @ConfigurationProperties)
@SpringBootApplication
@ConfigurationPropertiesScan  // Scans for @ConfigurationProperties classes
public class Application { }

// Option 3: Explicit registration
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class Application { }
```

---

## Component Scanning

### Understanding Component Scanning: Spring vs Spring Boot

**What is Component Scanning?**
Component scanning is the process by which Spring automatically discovers and registers beans from the classpath.

**Traditional Spring (Manual Configuration):**
```xml
<!-- applicationContext.xml -->
<context:component-scan base-package="com.example"/>
```

Or in Java:
```java
@Configuration
@ComponentScan(basePackages = "com.example")
public class AppConfig { }
```

**Spring Boot (Automatic):**
```java
@SpringBootApplication  // Includes @ComponentScan automatically!
public class Application { }
// Scans from the package where this class is located and all sub-packages
```

### @ComponentScan - Configuring What Gets Scanned

```java
// Scan specific packages
@Configuration
@ComponentScan(basePackages = {
    "com.example.service",
    "com.example.repository"
})
public class AppConfig { }

// Scan from a marker class (type-safe)
@Configuration
@ComponentScan(basePackageClasses = {
    ServiceMarker.class,
    RepositoryMarker.class
})
public class AppConfig { }

// Exclude certain components
@Configuration
@ComponentScan(
    basePackages = "com.example",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = TestService.class
    )
)
public class AppConfig { }

// Include only specific annotations
@Configuration
@ComponentScan(
    basePackages = "com.example",
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        classes = Service.class
    ),
    useDefaultFilters = false
)
public class AppConfig { }
```

### @SpringBootApplication - The Meta Annotation

**What it combines:**
```java
@SpringBootApplication 
// Is equivalent to:
@SpringBootConfiguration  // Same as @Configuration
@EnableAutoConfiguration  // Spring Boot's magic
@ComponentScan           // Scans from this package down
```

**Customizing @SpringBootApplication:**
```java
@SpringBootApplication(
    scanBasePackages = {"com.example.app", "com.example.shared"},
    exclude = {DataSourceAutoConfiguration.class}
)
public class Application { }
```

### @EnableAutoConfiguration - Spring Boot's Auto-Configuration

**What it does:** Enables Spring Boot's auto-configuration mechanism, which automatically configures beans based on classpath dependencies.

**How it works:**
1. Spring Boot scans for `META-INF/spring.factories` in all JARs
2. Finds auto-configuration classes listed there
3. Applies configurations if conditions are met (`@ConditionalOn...`)

**Excluding Auto-Configurations:**
```java
// Via annotation
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    SecurityAutoConfiguration.class,
    WebMvcAutoConfiguration.class
})
public class Application { }

// Via properties
# application.properties
spring.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
  org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
```

---

## Bean Scope Annotations

### @Scope - Controlling Bean Lifecycle

**What it does:** Defines how many instances of a bean Spring creates and how long they live.

**Available Scopes:**

| Scope | Description | Use Case |
|-------|-------------|----------|
| **singleton** | One instance per Spring container (default) | Stateless services |
| **prototype** | New instance every time requested | Stateful beans |
| **request** | One per HTTP request (web only) | Request-specific data |
| **session** | One per HTTP session (web only) | User session data |
| **application** | One per ServletContext (web only) | App-wide shared state |
| **websocket** | One per WebSocket session | WebSocket state |

**Scope Examples:**
```java
// Singleton (Default) - Use for stateless services
@Service
@Scope("singleton")  // Optional - singleton is default
public class UserService {
    // All requests share this single instance
    // ✅ Good for: Services, Repositories, Utilities
}

// Prototype - New instance each time
@Component
@Scope("prototype")
public class ReportGenerator {
    private List<String> reportData = new ArrayList<>();
    
    // Each injection gets a new instance
    // ✅ Good for: Stateful objects, builders
}

// Request Scope - One per HTTP request
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext {
    private String requestId;
    private LocalDateTime startTime;
    // ✅ Good for: Request tracking, request-scoped caching
}

// Session Scope - One per user session
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ShoppingCart {
    private List<CartItem> items = new ArrayList<>();
    // ✅ Good for: User-specific data that spans requests
}
```

**Important: Scope and Proxy Mode**

When injecting a shorter-lived bean into a longer-lived bean, use `proxyMode`:

```java
@Service  // Singleton
public class CheckoutService {
    
    private final ShoppingCart cart;  // Session-scoped
    
    // ❌ Problem: Singleton holds reference to session-scoped bean
    // The cart would be from the first user's session forever!
    
    // ✅ Solution: Use proxyMode on ShoppingCart
    // Spring injects a proxy that delegates to the correct session's cart
    public CheckoutService(ShoppingCart cart) {
        this.cart = cart;
    }
}
```

---

## Lifecycle Annotations

### @PostConstruct and @PreDestroy

**What they do:** Allow you to define initialization and cleanup code for beans.

**Evolution:**
- **Spring 2.5+:** Introduced support for JSR-250 annotations
- **Spring Boot:** Same support, automatically enabled

**Spring Traditional vs Spring Boot:**

| Aspect | Traditional Spring | Spring Boot |
|--------|-------------------|-------------|
| **Configuration** | Required `<context:annotation-config/>` | Auto-configured |
| **Dependency** | Manual JSR-250 dependency | Included with spring-boot-starter |

```java
@Component
public class DatabaseConnection {
    
    private Connection connection;
    
    @PostConstruct  // Called after dependency injection is complete
    public void init() {
        System.out.println("Initializing database connection...");
        // Open connections, load caches, start schedulers
        this.connection = createConnection();
    }
    
    @PreDestroy  // Called before the bean is removed from container
    public void cleanup() {
        System.out.println("Closing database connection...");
        // Close connections, flush caches, stop schedulers
        if (connection != null) {
            connection.close();
        }
    }
    
    private Connection createConnection() {
        // Connection creation logic
        return null;
    }
}
```

**Alternative: Using @Bean Attributes**
```java
@Configuration
public class AppConfig {
    
    @Bean(initMethod = "init", destroyMethod = "cleanup")
    public DatabaseConnection databaseConnection() {
        return new DatabaseConnection();
    }
}
```

---

## Conditional Annotations (Spring Boot Feature)

### Understanding @Conditional Annotations

**What they are:** Spring Boot-specific annotations that allow beans to be created only when certain conditions are met.

**Why they exist:** This is the foundation of Spring Boot's auto-configuration. Without conditional beans, auto-configuration would create conflicts.

### Common Conditional Annotations

```java
@Configuration
public class ConditionalConfig {
    
    // Create bean only if property exists and has specific value
    @Bean
    @ConditionalOnProperty(name = "feature.email.enabled", havingValue = "true")
    public EmailService emailService() {
        return new EmailService();
    }
    
    // Create bean only if no other bean of this type exists
    @Bean
    @ConditionalOnMissingBean(EmailService.class)
    public EmailService defaultEmailService() {
        return new MockEmailService();  // Fallback
    }
    
    // Create bean only if a specific class is on the classpath
    @Bean
    @ConditionalOnClass(name = "com.stripe.Stripe")
    public PaymentProcessor stripeProcessor() {
        return new StripePaymentProcessor();
    }
    
    // Create bean only if a specific class is NOT on the classpath
    @Bean
    @ConditionalOnMissingClass("com.stripe.Stripe")
    public PaymentProcessor mockPaymentProcessor() {
        return new MockPaymentProcessor();
    }
    
    // Create bean only in web applications
    @Bean
    @ConditionalOnWebApplication
    public WebOnlyService webOnlyService() {
        return new WebOnlyService();
    }
    
    // Create bean only if another bean exists
    @Bean
    @ConditionalOnBean(DataSource.class)
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    
    // Create bean based on Spring profile
    @Bean
    @Profile("production")
    public AuditService productionAuditService() {
        return new ProductionAuditService();
    }
    
    @Bean
    @Profile("!production")  // NOT production
    public AuditService devAuditService() {
        return new MockAuditService();
    }
}
```

**How Auto-Configuration Uses Conditionals:**
```java
// This is how Spring Boot's DataSource auto-configuration works (simplified)
@Configuration
@ConditionalOnClass(DataSource.class)  // Only if JDBC is on classpath
@ConditionalOnMissingBean(DataSource.class)  // Only if user hasn't defined one
public class DataSourceAutoConfiguration {
    
    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource", name = "url")
    public DataSource dataSource() {
        // Create HikariCP DataSource from properties
        return new HikariDataSource();
    }
}
```

---

## Practical Examples

### Complete REST API Example

```java
// Model
public class Product {
    private Long id;
    private String name;
    private Double price;
    private Integer quantity;
    
    // Constructors, getters, setters
}

// Repository
@Repository
public class ProductRepository {
    private Map<Long, Product> products = new ConcurrentHashMap<>();
    private AtomicLong idCounter = new AtomicLong();
    
    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(idCounter.incrementAndGet());
        }
        products.put(product.getId(), product);
        return product;
    }
    
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(products.get(id));
    }
    
    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }
    
    public void deleteById(Long id) {
        products.remove(id);
    }
}

// Service
@Service
public class ProductService {
    private final ProductRepository repository;
    
    @Autowired
    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }
    
    public Product createProduct(Product product) {
        return repository.save(product);
    }
    
    public Product getProduct(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
    }
    
    public List<Product> getAllProducts() {
        return repository.findAll();
    }
    
    public Product updateProduct(Long id, Product product) {
        Product existing = getProduct(id);
        existing.setName(product.getName());
        existing.setPrice(product.getPrice());
        existing.setQuantity(product.getQuantity());
        return repository.save(existing);
    }
    
    public void deleteProduct(Long id) {
        repository.deleteById(id);
    }
}

// Controller
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;
    
    @Autowired
    public ProductController(ProductService service) {
        this.service = service;
    }
    
    @GetMapping
    public List<Product> getAllProducts() {
        return service.getAllProducts();
    }
    
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return service.getProduct(id);
    }
    
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return service.createProduct(product);
    }
    
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return service.updateProduct(id, product);
    }
    
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        service.deleteProduct(id);
    }
}
```

## Exercises

### Exercise 1: Basic CRUD API
Create a complete CRUD API for a Book entity with:
- Model: Book (id, title, author, isbn, price)
- Repository layer with in-memory storage
- Service layer with business logic
- REST controller with all CRUD operations

### Exercise 2: Dependency Injection
Create multiple implementations of a PaymentService interface:
- CreditCardPaymentService
- PayPalPaymentService
- Use @Qualifier to inject specific implementations
- Create a PaymentProcessor that uses @Primary

### Exercise 3: Configuration
Create configuration classes:
- AppConfig with custom beans
- Use @Value to inject properties
- Use @ConfigurationProperties for complex properties
- Create different configurations for dev and prod profiles

### Exercise 4: Component Scanning
- Create packages: controller, service, repository, config
- Configure component scanning to include only specific packages
- Test bean creation with @PostConstruct

### Exercise 5: Request Parameters
Create an endpoint that accepts:
- Path variables for resource ID
- Query parameters for filtering
- Request headers for authentication
- Request body for data

## Key Takeaways

### Spring vs Spring Boot: Quick Reference

| What You Want | Traditional Spring | Spring Boot |
|---------------|-------------------|-------------|
| Define a bean | XML `<bean>` or `@Bean` | `@Component`/`@Service`/`@Repository` |
| Configure DataSource | XML or @Bean with properties | `application.properties` + auto-config |
| Create REST endpoint | @Controller + @ResponseBody + XML | @RestController (that's it!) |
| Handle transactions | XML config + @Transactional | Just @Transactional |
| Connect to database | Manual EntityManager setup | spring.datasource.* properties |

### Annotation Summary

| Annotation | Purpose | When to Use |
|------------|---------|-------------|
| `@Component` | Generic bean | Classes you own, general purpose |
| `@Service` | Business logic bean | Service layer classes |
| `@Repository` | Data access bean | DAO classes (with exception translation) |
| `@Controller` | Web MVC controller | HTML-returning endpoints |
| `@RestController` | REST API controller | JSON/XML-returning endpoints |
| `@Bean` | Method-level bean definition | Third-party classes, complex config |
| `@Configuration` | Bean configuration class | Where @Bean methods live |
| `@Autowired` | Dependency injection | Optional in Spring Boot (single constructor) |
| `@Qualifier` | Specify which bean to inject | Multiple beans of same type |
| `@Primary` | Default bean when ambiguous | One "main" implementation |
| `@Value` | Inject single property | Simple configuration values |
| `@ConfigurationProperties` | Inject grouped properties | Complex, type-safe configuration |
| `@Scope` | Bean lifecycle scope | Non-singleton beans |

### Best Practices Checklist

✅ **Use `@Component` variants** (`@Service`, `@Repository`, `@Controller`) for semantic clarity  
✅ **Prefer constructor injection** over field injection for testability  
✅ **Use `@Bean` for third-party classes** that you can't annotate  
✅ **Use `@ConfigurationProperties`** instead of multiple `@Value` annotations  
✅ **Let Spring Boot auto-configure** when possible  
✅ **Use `@RestController`** for REST APIs, `@Controller` for MVC  
✅ **Keep `@Configuration` classes** focused and well-organized  
✅ **Use `@Qualifier` or `@Primary`** to resolve ambiguity  
✅ **Don't use field injection** in production code  
✅ **Understand the layered architecture**: Controller → Service → Repository  

### The Golden Rule

> **Spring Boot = Spring Framework + Auto-configuration + Opinionated Defaults**
> 
> Spring Boot doesn't replace Spring annotations—it builds on top of them and eliminates boilerplate configuration. Understanding both helps you customize behavior when auto-configuration isn't enough.

## Resources

### Official Documentation
- [Spring Core Annotations](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-stereotype-annotations)
- [Spring Web Annotations](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-ann-requestmapping)
- [Spring Boot Annotations](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.auto-configuration)
- [Spring Boot Auto-Configuration Report](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration)

### Guides
- [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
- [Dependency Injection](https://spring.io/guides/gs/dependency-injection/)
- [Spring Boot Features](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html)

### Further Reading
- [Why Constructor Injection?](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-constructor-injection)
- [Understanding Spring Boot Auto-Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration)

## Next Steps

Tomorrow, we'll explore **Dependency Injection and IoC Container** in depth, understanding how Spring manages beans and dependencies!

---

[<< Previous: Day 02](../Day02_SpringBoot_Setup/README.md) | [Back to Main](../README.md) | [Next: Day 04 >>](../Day04_DI_IoC/README.md)
