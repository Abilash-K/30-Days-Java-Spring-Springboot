# Day 04: Dependency Injection and IoC Container

## 📋 Table of Contents
- [Introduction](#introduction)
- [Understanding IoC and DI](#understanding-ioc-and-di)
- [Types of Dependency Injection](#types-of-dependency-injection)
- [Bean Lifecycle](#bean-lifecycle)
- [Bean Scopes](#bean-scopes)
- [Autowiring](#autowiring)
- [Handling Circular Dependencies](#handling-circular-dependencies)
- [Best Practices](#best-practices)
- [Code Examples](#code-examples)
- [Exercises](#exercises)
- [Key Takeaways](#key-takeaways)
- [Resources](#resources)

## Introduction

Welcome to Day 4! Today we'll dive deep into **Dependency Injection (DI)** and **Inversion of Control (IoC)** - the fundamental principles that make Spring Framework powerful and flexible.

### What You'll Learn
- How IoC container manages application objects
- Different ways to inject dependencies
- Bean lifecycle and scopes
- Best practices for dependency management
- Common pitfalls and how to avoid them

## Understanding IoC and DI

### Inversion of Control (IoC)

**Inversion of Control** is a design principle where the control of object creation and lifecycle is transferred from the application code to a framework or container.

**Traditional Approach:**
```java
public class UserService {
    private UserRepository repository;
    
    public UserService() {
        // Application creates dependency
        this.repository = new UserRepositoryImpl();
    }
}
```

**IoC Approach:**
```java
@Service
public class UserService {
    private final UserRepository repository;
    
    // Container injects dependency
    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

### Dependency Injection (DI)

**Dependency Injection** is a pattern where dependencies are provided (injected) to an object rather than the object creating them itself.

**Benefits:**
- ✅ Loose coupling between components
- ✅ Easier testing with mock objects
- ✅ Better code maintainability
- ✅ Flexible configuration
- ✅ Promotes single responsibility principle

### Spring IoC Container

Spring provides two types of IoC containers:

**1. BeanFactory** - Basic container
```java
Resource resource = new ClassPathResource("beans.xml");
BeanFactory factory = new XmlBeanFactory(resource);
MyBean bean = (MyBean) factory.getBean("myBean");
```

**2. ApplicationContext** - Advanced container (recommended)
```java
ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
// or
ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
MyBean bean = context.getBean(MyBean.class);
```

**ApplicationContext provides:**
- Bean lifecycle management
- Automatic BeanPostProcessor registration
- Event propagation
- Internationalization
- Application-layer specific contexts

## Types of Dependency Injection

### 1. Constructor Injection (Recommended)

**Best practice** for required dependencies.

```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final EmailService emailService;
    
    // Constructor injection - recommended
    public OrderService(OrderRepository orderRepository,
                       PaymentService paymentService,
                       EmailService emailService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
        this.emailService = emailService;
    }
    
    public Order createOrder(OrderRequest request) {
        Order order = new Order(request);
        order = orderRepository.save(order);
        paymentService.processPayment(order);
        emailService.sendOrderConfirmation(order);
        return order;
    }
}
```

**Advantages:**
- ✅ Immutable objects (final fields)
- ✅ Ensures all required dependencies are provided
- ✅ Easier to test (no reflection needed)
- ✅ Prevents NullPointerException
- ✅ Thread-safe

**Spring Boot 4.3+:** `@Autowired` is optional for single constructor

### 2. Setter Injection

Used for **optional** dependencies.

```java
@Service
public class NotificationService {
    private SmsService smsService;
    private PushService pushService;
    
    // Optional: SMS notifications
    @Autowired(required = false)
    public void setSmsService(SmsService smsService) {
        this.smsService = smsService;
    }
    
    // Optional: Push notifications
    @Autowired(required = false)
    public void setPushService(PushService pushService) {
        this.pushService = pushService;
    }
    
    public void notifyUser(String message) {
        if (smsService != null) {
            smsService.send(message);
        }
        if (pushService != null) {
            pushService.send(message);
        }
    }
}
```

**Use Cases:**
- Optional dependencies
- Dependencies that can be reconfigured
- JMX managed beans

### 3. Field Injection

**Not recommended** but widely used for simplicity.

```java
@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryService categoryService;
    
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}
```

**Disadvantages:**
- ❌ Cannot create immutable objects
- ❌ Hidden dependencies (not visible in constructor)
- ❌ Harder to test (requires reflection or Spring context)
- ❌ Breaks encapsulation
- ❌ Makes circular dependencies easy (bad thing)

**When it's acceptable:**
- Tests (with @MockBean)
- Configuration classes
- Quick prototyping

## Bean Lifecycle

### Lifecycle Phases

```
Container Started
      ↓
Bean Instantiation (Constructor called)
      ↓
Dependency Injection (setters/fields)
      ↓
BeanNameAware.setBeanName()
      ↓
BeanFactoryAware.setBeanFactory()
      ↓
ApplicationContextAware.setApplicationContext()
      ↓
BeanPostProcessor.postProcessBeforeInitialization()
      ↓
@PostConstruct method
      ↓
InitializingBean.afterPropertiesSet()
      ↓
Custom init-method
      ↓
BeanPostProcessor.postProcessAfterInitialization()
      ↓
Bean Ready to Use
      ↓
Container Shutdown Triggered
      ↓
@PreDestroy method
      ↓
DisposableBean.destroy()
      ↓
Custom destroy-method
      ↓
Bean Destroyed
```

### Lifecycle Callbacks

**1. Using Annotations (Recommended)**
```java
@Component
public class DatabaseConnection {
    private Connection connection;
    
    @PostConstruct
    public void init() {
        System.out.println("Initializing database connection...");
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb");
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("Closing database connection...");
        if (connection != null) {
            connection.close();
        }
    }
}
```

**2. Using Interfaces**
```java
@Component
public class CacheManager implements InitializingBean, DisposableBean {
    private Map<String, Object> cache;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Initializing cache...");
        cache = new ConcurrentHashMap<>();
        // Load cache from database
    }
    
    @Override
    public void destroy() throws Exception {
        System.out.println("Clearing cache...");
        cache.clear();
    }
}
```

**3. Using XML Configuration**
```java
@Component
public class FileProcessor {
    
    public void initialize() {
        System.out.println("File processor initialized");
    }
    
    public void cleanup() {
        System.out.println("File processor cleanup");
    }
}
```

```xml
<bean id="fileProcessor" class="com.example.FileProcessor"
      init-method="initialize"
      destroy-method="cleanup"/>
```

**4. Using Java Configuration**
```java
@Configuration
public class AppConfig {
    
    @Bean(initMethod = "initialize", destroyMethod = "cleanup")
    public FileProcessor fileProcessor() {
        return new FileProcessor();
    }
}
```

### Aware Interfaces

Spring provides several "Aware" interfaces to access container infrastructure:

```java
@Component
public class ApplicationContextProvider implements ApplicationContextAware,
                                                   BeanNameAware,
                                                   BeanFactoryAware {
    private ApplicationContext applicationContext;
    private String beanName;
    private BeanFactory beanFactory;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        System.out.println("Bean name is: " + name);
    }
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
    
    public <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }
}
```

## Bean Scopes

Spring supports several bean scopes:

### 1. Singleton (Default)

One instance per Spring IoC container.

```java
@Component
@Scope("singleton") // or @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DatabaseConfig {
    private String url;
    private String username;
    
    // Single instance shared across application
}
```

### 2. Prototype

New instance every time the bean is requested.

```java
@Component
@Scope("prototype") // or @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReportGenerator {
    
    public Report generate() {
        // New instance each time
        return new Report();
    }
}
```

### 3. Request (Web Applications)

One instance per HTTP request.

```java
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ShoppingCart {
    private List<Item> items = new ArrayList<>();
    
    // New instance for each HTTP request
}
```

### 4. Session (Web Applications)

One instance per HTTP session.

```java
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserPreferences {
    private String theme;
    private String language;
    
    // Shared across requests in same session
}
```

### 5. Application (Web Applications)

One instance per ServletContext.

```java
@Component
@Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ApplicationCache {
    private Map<String, Object> cache = new ConcurrentHashMap<>();
    
    // Single instance for entire web application
}
```

### Custom Scope

```java
public class TenantScope implements Scope {
    private Map<String, Object> scopedObjects = new ConcurrentHashMap<>();
    
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        String tenant = TenantContext.getCurrentTenant();
        String key = tenant + "_" + name;
        return scopedObjects.computeIfAbsent(key, k -> objectFactory.getObject());
    }
    
    @Override
    public Object remove(String name) {
        String tenant = TenantContext.getCurrentTenant();
        return scopedObjects.remove(tenant + "_" + name);
    }
    
    // Implement other methods...
}

@Configuration
public class ScopeConfig {
    @Bean
    public CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope("tenant", new TenantScope());
        return configurer;
    }
}
```

## Autowiring

### @Autowired Annotation

**By Type:**
```java
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository; // Inject by type
}
```

**By Name with @Qualifier:**
```java
@Service
public class PaymentService {
    @Autowired
    @Qualifier("stripePaymentProcessor")
    private PaymentProcessor paymentProcessor;
}
```

**Required vs Optional:**
```java
@Service
public class EmailService {
    @Autowired(required = false)
    private SmsService smsService; // Optional dependency
}
```

### @Qualifier

Use when multiple beans of same type exist:

```java
// Multiple implementations
@Component("smsNotification")
public class SmsNotification implements NotificationService {
    @Override
    public void send(String message) {
        System.out.println("SMS: " + message);
    }
}

@Component("emailNotification")
public class EmailNotification implements NotificationService {
    @Override
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}

// Usage
@Service
public class UserService {
    private final NotificationService smsNotification;
    private final NotificationService emailNotification;
    
    public UserService(@Qualifier("smsNotification") NotificationService smsNotification,
                      @Qualifier("emailNotification") NotificationService emailNotification) {
        this.smsNotification = smsNotification;
        this.emailNotification = emailNotification;
    }
}
```

### @Primary

Specify a default bean when multiple candidates exist:

```java
@Component
@Primary
public class DefaultPaymentProcessor implements PaymentProcessor {
    // This will be injected by default
}

@Component
public class StripePaymentProcessor implements PaymentProcessor {
    // Alternative implementation
}

@Service
public class CheckoutService {
    @Autowired
    private PaymentProcessor processor; // DefaultPaymentProcessor injected
}
```

### @Resource (JSR-250)

Inject by name:

```java
@Service
public class OrderService {
    @Resource(name = "orderRepository")
    private OrderRepository repository;
}
```

### @Inject (JSR-330)

Standard alternative to @Autowired:

```java
@Service
public class ProductService {
    @Inject
    private ProductRepository repository;
    
    @Inject
    @Named("productCache")
    private Cache cache;
}
```

## Handling Circular Dependencies

### What is Circular Dependency?

```java
@Service
public class ServiceA {
    @Autowired
    private ServiceB serviceB; // A depends on B
}

@Service
public class ServiceB {
    @Autowired
    private ServiceA serviceA; // B depends on A
}
// Results in: BeanCurrentlyInCreationException
```

### Solution 1: Constructor Injection + @Lazy

```java
@Service
public class ServiceA {
    private final ServiceB serviceB;
    
    public ServiceA(@Lazy ServiceB serviceB) {
        this.serviceB = serviceB;
    }
}

@Service
public class ServiceB {
    private final ServiceA serviceA;
    
    public ServiceB(ServiceA serviceA) {
        this.serviceA = serviceA;
    }
}
```

### Solution 2: Setter Injection

```java
@Service
public class ServiceA {
    private ServiceB serviceB;
    
    @Autowired
    public void setServiceB(ServiceB serviceB) {
        this.serviceB = serviceB;
    }
}

@Service
public class ServiceB {
    private ServiceA serviceA;
    
    @Autowired
    public void setServiceA(ServiceA serviceA) {
        this.serviceA = serviceA;
    }
}
```

### Solution 3: Redesign (Best Practice)

Extract common functionality to a third service:

```java
@Service
public class CommonService {
    public void doCommonWork() {
        // Shared logic
    }
}

@Service
public class ServiceA {
    private final CommonService commonService;
    
    public ServiceA(CommonService commonService) {
        this.commonService = commonService;
    }
}

@Service
public class ServiceB {
    private final CommonService commonService;
    
    public ServiceB(CommonService commonService) {
        this.commonService = commonService;
    }
}
```

### Solution 4: @PostConstruct

```java
@Service
public class ServiceA {
    @Autowired
    private ServiceB serviceB;
    
    @PostConstruct
    public void init() {
        serviceB.setServiceA(this);
    }
}

@Service
public class ServiceB {
    private ServiceA serviceA;
    
    public void setServiceA(ServiceA serviceA) {
        this.serviceA = serviceA;
    }
}
```

## Best Practices

### 1. Prefer Constructor Injection

```java
// Good
@Service
public class UserService {
    private final UserRepository repository;
    private final EmailService emailService;
    
    public UserService(UserRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }
}

// Avoid
@Service
public class UserService {
    @Autowired
    private UserRepository repository;
    @Autowired
    private EmailService emailService;
}
```

### 2. Use Interfaces for Abstraction

```java
// Good
public interface PaymentService {
    void processPayment(Order order);
}

@Service
public class StripePaymentService implements PaymentService {
    @Override
    public void processPayment(Order order) {
        // Implementation
    }
}

@Service
public class OrderService {
    private final PaymentService paymentService;
    
    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

### 3. Avoid Circular Dependencies

Design your architecture to prevent circular dependencies from the start.

### 4. Use @Qualifier for Multiple Implementations

```java
@Service
public class NotificationManager {
    private final NotificationService smsService;
    private final NotificationService emailService;
    
    public NotificationManager(
            @Qualifier("smsNotification") NotificationService smsService,
            @Qualifier("emailNotification") NotificationService emailService) {
        this.smsService = smsService;
        this.emailService = emailService;
    }
}
```

### 5. Appropriate Bean Scopes

- Use **singleton** for stateless services (default)
- Use **prototype** for stateful objects
- Use **request/session** for web-specific data

### 6. Lifecycle Management

```java
@Component
public class ResourceManager {
    
    @PostConstruct
    public void init() {
        // Initialize resources
    }
    
    @PreDestroy
    public void cleanup() {
        // Clean up resources
    }
}
```

## Code Examples

### Complete Example: E-Commerce Order System

```java
// Repository Layer
public interface OrderRepository extends JpaRepository<Order, Long> {
}

public interface ProductRepository extends JpaRepository<Product, Long> {
}

// Service Layer
@Service
public class InventoryService {
    private final ProductRepository productRepository;
    
    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    public boolean checkAvailability(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        return product.getStock() >= quantity;
    }
    
    public void reduceStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }
}

@Service
public class PaymentService {
    
    public boolean processPayment(Order order) {
        // Process payment logic
        System.out.println("Processing payment for order: " + order.getId());
        return true;
    }
}

@Service
public class NotificationService {
    
    public void sendOrderConfirmation(Order order) {
        System.out.println("Sending confirmation for order: " + order.getId());
    }
}

// Main Service with DI
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    
    // Constructor injection
    public OrderService(OrderRepository orderRepository,
                       InventoryService inventoryService,
                       PaymentService paymentService,
                       NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
    }
    
    @Transactional
    public Order createOrder(OrderRequest request) {
        // Check inventory
        if (!inventoryService.checkAvailability(request.getProductId(), request.getQuantity())) {
            throw new InsufficientStockException();
        }
        
        // Create order
        Order order = new Order();
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setStatus("PENDING");
        order = orderRepository.save(order);
        
        // Process payment
        boolean paymentSuccess = paymentService.processPayment(order);
        if (!paymentSuccess) {
            order.setStatus("PAYMENT_FAILED");
            throw new PaymentException();
        }
        
        // Update inventory
        inventoryService.reduceStock(request.getProductId(), request.getQuantity());
        
        // Update order status
        order.setStatus("CONFIRMED");
        order = orderRepository.save(order);
        
        // Send notification
        notificationService.sendOrderConfirmation(order);
        
        return order;
    }
}

// Controller
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.ok(order);
    }
}
```

## Exercises

### Exercise 1: Basic Dependency Injection

Create a simple blog application with:
- `PostRepository` interface with `findAll()` and `save()` methods
- `PostRepositoryImpl` implementation
- `PostService` with constructor injection
- Demonstrate all three types of injection

### Exercise 2: Bean Scopes

Create a shopping cart system:
- `ShoppingCart` with request scope
- `UserSession` with session scope
- `ProductCatalog` with singleton scope
- Test that each scope works correctly

### Exercise 3: Lifecycle Callbacks

Create a `CacheService` that:
- Loads data in `@PostConstruct`
- Saves data in `@PreDestroy`
- Implements both `InitializingBean` and `DisposableBean`

### Exercise 4: Qualifier and Primary

Create a notification system with:
- `NotificationService` interface
- Multiple implementations (Email, SMS, Push)
- Use `@Primary` for default
- Use `@Qualifier` to inject specific implementations

### Exercise 5: Circular Dependency

Create two services with circular dependency:
- Intentionally create the problem
- Solve it using three different methods
- Explain which solution is best and why

## Key Takeaways

✅ **IoC** transfers control of object creation to Spring container  
✅ **Constructor injection** is preferred for required dependencies  
✅ **Setter injection** is suitable for optional dependencies  
✅ **Avoid field injection** in production code  
✅ **Bean lifecycle** provides hooks for initialization and cleanup  
✅ **Bean scopes** control instance creation strategy  
✅ Use **@Qualifier** and **@Primary** for disambiguation  
✅ **Prevent circular dependencies** through proper design  
✅ Use **interfaces** for loose coupling and testability  
✅ Leverage **@PostConstruct** and **@PreDestroy** for lifecycle management  

## Resources

### Official Documentation
- [Spring Framework IoC Container](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans)
- [Spring Dependency Injection](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-dependencies)
- [Bean Scopes](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-scopes)

### Books
- "Spring in Action" by Craig Walls
- "Pro Spring 5" by Iuliana Cosmina
- "Expert One-on-One J2EE Design and Development" by Rod Johnson

### Articles
- [Baeldung: Guide to Spring Dependency Injection](https://www.baeldung.com/spring-dependency-injection)
- [DZone: Spring Dependency Injection Types](https://dzone.com/articles/spring-dependency-injection)

## Next Steps

Tomorrow, we'll explore **Spring MVC and REST APIs** where we'll use DI to build a complete RESTful web service!

---
[<< Previous: Day 03](../Day03_SpringBoot_Basics/README.md) | [Back to Main](../README.md) | [Next: Day 05 >>](../Day05_REST_APIs/README.md)
