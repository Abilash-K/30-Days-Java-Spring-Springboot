# Day 03: Spring Boot Basics and Annotations

## 📋 Table of Contents
- [Introduction](#introduction)
- [Core Spring Annotations](#core-spring-annotations)
- [Web Annotations](#web-annotations)
- [Component Scanning](#component-scanning)
- [Bean Lifecycle](#bean-lifecycle)
- [Practical Examples](#practical-examples)
- [Exercises](#exercises)
- [Resources](#resources)

## Introduction

Welcome to Day 3! Today we'll explore the fundamental annotations that power Spring Boot applications. Understanding these annotations is crucial for building robust Spring Boot applications.

## Core Spring Annotations

### @Component
Marks a Java class as a Spring-managed component (bean).

```java
@Component
public class EmailService {
    public void sendEmail(String to, String message) {
        System.out.println("Sending email to: " + to);
    }
}
```

### @Service
Specialized `@Component` for service layer classes.

```java
@Service
public class UserService {
    public User getUserById(Long id) {
        // Business logic here
        return new User(id, "John Doe");
    }
}
```

### @Repository
Specialized `@Component` for data access layer. Provides additional exception translation.

```java
@Repository
public class UserRepository {
    public User findById(Long id) {
        // Database access logic
        return new User(id, "John Doe");
    }
}
```

### @Controller vs @RestController

**@Controller**: Returns views (HTML pages)
```java
@Controller
public class WebController {
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("message", "Hello");
        return "home"; // Returns view name
    }
}
```

**@RestController**: Returns data (JSON/XML), combines `@Controller` + `@ResponseBody`
```java
@RestController
public class ApiController {
    @GetMapping("/api/users")
    public List<User> getUsers() {
        return userService.findAll(); // Returns JSON
    }
}
```

## Web Annotations

### Request Mapping Annotations

#### @RequestMapping
Generic mapping for HTTP requests.

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Product getProduct(@PathVariable Long id) {
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

### @Autowired
Injects dependencies automatically.

```java
@Service
public class OrderService {
    
    // Field injection (not recommended)
    @Autowired
    private UserService userService;
    
    // Constructor injection (recommended)
    private final ProductService productService;
    
    @Autowired
    public OrderService(ProductService productService) {
        this.productService = productService;
    }
    
    // Setter injection
    private PaymentService paymentService;
    
    @Autowired
    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

### @Qualifier
Specifies which bean to inject when multiple candidates exist.

```java
public interface MessageService {
    void sendMessage(String message);
}

@Service
@Qualifier("email")
public class EmailService implements MessageService {
    public void sendMessage(String message) {
        System.out.println("Email: " + message);
    }
}

@Service
@Qualifier("sms")
public class SmsService implements MessageService {
    public void sendMessage(String message) {
        System.out.println("SMS: " + message);
    }
}

@Service
public class NotificationService {
    private final MessageService messageService;
    
    @Autowired
    public NotificationService(@Qualifier("email") MessageService messageService) {
        this.messageService = messageService;
    }
}
```

### @Primary
Marks a bean as primary when multiple candidates exist.

```java
@Service
@Primary
public class EmailService implements MessageService {
    // This will be injected by default
}

@Service
public class SmsService implements MessageService {
    // This needs explicit @Qualifier to be injected
}
```

## Configuration Annotations

### @Configuration
Indicates a class declares Spring beans.

```java
@Configuration
public class AppConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
```

### @Value
Injects property values.

```java
@Component
public class AppSettings {
    
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.version:1.0}")  // Default value 1.0
    private String version;
    
    @Value("${app.max-users:100}")
    private int maxUsers;
}
```

### @ConfigurationProperties
Binds properties to POJO.

```java
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String name;
    private String version;
    private int maxUsers;
    
    // Getters and setters
}

// application.properties:
// app.name=MyApp
// app.version=2.0
// app.max-users=500
```

## Component Scanning

### @ComponentScan
Configures component scanning directives.

```java
@Configuration
@ComponentScan(basePackages = {
    "com.example.service",
    "com.example.repository"
})
public class AppConfig {
    // Configuration
}
```

### @EnableAutoConfiguration
Enables Spring Boot's auto-configuration mechanism.

```java
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    SecurityAutoConfiguration.class
})
public class Application {
    // Application code
}
```

## Bean Scope Annotations

### @Scope
Defines bean scope.

```java
@Component
@Scope("singleton")  // Default - one instance per container
public class SingletonBean { }

@Component
@Scope("prototype")  // New instance for each request
public class PrototypeBean { }

@Component
@Scope("request")    // One instance per HTTP request (Web)
public class RequestBean { }

@Component
@Scope("session")    // One instance per HTTP session (Web)
public class SessionBean { }
```

## Lifecycle Annotations

### @PostConstruct and @PreDestroy

```java
@Component
public class DatabaseConnection {
    
    @PostConstruct
    public void init() {
        System.out.println("Bean initialized, opening connection");
        // Initialization logic
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("Bean destroyed, closing connection");
        // Cleanup logic
    }
}
```

## Conditional Annotations

### @Conditional
Creates beans conditionally.

```java
@Configuration
public class ConditionalConfig {
    
    @Bean
    @ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
    public FeatureService featureService() {
        return new FeatureService();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public DefaultService defaultService() {
        return new DefaultService();
    }
    
    @Bean
    @ConditionalOnClass(name = "com.example.OptionalLib")
    public OptionalFeature optionalFeature() {
        return new OptionalFeature();
    }
}
```

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

✅ Annotations reduce boilerplate configuration  
✅ @RestController combines @Controller and @ResponseBody  
✅ Constructor injection is recommended over field injection  
✅ @Service, @Repository, and @Component organize layers  
✅ @PathVariable and @RequestParam extract URL data  
✅ @Value and @ConfigurationProperties manage properties  

## Resources

### Official Documentation
- [Spring Core Annotations](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-stereotype-annotations)
- [Spring Web Annotations](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-ann-requestmapping)
- [Spring Boot Annotations](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.auto-configuration)

### Guides
- [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
- [Dependency Injection](https://spring.io/guides/gs/dependency-injection/)

## Next Steps

Tomorrow, we'll explore **Dependency Injection and IoC Container** in depth, understanding how Spring manages beans and dependencies!

---

[<< Previous: Day 02](../Day02_SpringBoot_Setup/README.md) | [Back to Main](../README.md) | [Next: Day 04 >>](../Day04_DI_IoC/README.md)
