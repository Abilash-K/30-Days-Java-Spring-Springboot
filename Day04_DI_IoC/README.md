# Day 04: Dependency Injection and IoC Container

## 📋 Table of Contents
- [Introduction](#introduction)
- [The Evolution: From Manual to Managed Dependencies](#the-evolution-from-manual-to-managed-dependencies)
- [Spring IoC Container Deep Dive](#spring-ioc-container-deep-dive)
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
- **The evolution from XML configuration to Spring Boot auto-configuration**

---

## The Evolution: From Manual to Managed Dependencies

### The Problem: Tight Coupling in Traditional Java

**Before Spring (Manual Dependency Management):**

```java
// The Problem: Tight coupling, hard to test, hard to change
public class OrderService {
    
    // Direct instantiation - tight coupling!
    private UserRepository userRepository = new UserRepositoryImpl();
    private EmailService emailService = new SmtpEmailService();
    private PaymentGateway paymentGateway = new StripePaymentGateway();
    
    public void processOrder(Order order) {
        User user = userRepository.findById(order.getUserId());
        paymentGateway.charge(user, order.getTotal());
        emailService.sendConfirmation(user.getEmail(), order);
    }
}

// Problems:
// 1. Cannot easily switch implementations (want PayPal instead of Stripe?)
// 2. Cannot test without hitting real database, sending real emails, charging real cards
// 3. Every class that uses UserRepository must know about UserRepositoryImpl
// 4. Changes to dependencies ripple through the codebase
```

### Solution 1: Manual Dependency Injection (No Spring)

```java
// Better: Constructor-based manual DI
public class OrderService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PaymentGateway paymentGateway;
    
    // Dependencies passed in - loose coupling!
    public OrderService(UserRepository userRepository, 
                        EmailService emailService,
                        PaymentGateway paymentGateway) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.paymentGateway = paymentGateway;
    }
}

// But now, SOMEONE has to create and wire everything together...
public class Application {
    public static void main(String[] args) {
        // Manual wiring - tedious and error-prone!
        DataSource dataSource = new HikariDataSource(...);
        UserRepository userRepository = new UserRepositoryImpl(dataSource);
        EmailService emailService = new SmtpEmailService("smtp.example.com", 587);
        PaymentGateway paymentGateway = new StripePaymentGateway(API_KEY);
        
        OrderService orderService = new OrderService(userRepository, emailService, paymentGateway);
        
        // Imagine doing this for hundreds of classes...
    }
}
```

### Solution 2: Spring XML Configuration (Spring 1.x - 2.x Era)

```xml
<!-- applicationContext.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- DataSource bean -->
    <bean id="dataSource" class="com.zaxxer.hikari.HikariDataSource">
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/mydb"/>
        <property name="username" value="root"/>
        <property name="password" value="secret"/>
    </bean>

    <!-- Repository bean with DataSource dependency -->
    <bean id="userRepository" class="com.example.repository.UserRepositoryImpl">
        <constructor-arg ref="dataSource"/>
    </bean>

    <!-- Email service bean -->
    <bean id="emailService" class="com.example.service.SmtpEmailService">
        <property name="host" value="smtp.example.com"/>
        <property name="port" value="587"/>
    </bean>

    <!-- Payment gateway bean -->
    <bean id="paymentGateway" class="com.example.service.StripePaymentGateway">
        <constructor-arg value="${stripe.api.key}"/>
    </bean>

    <!-- Order service with all dependencies -->
    <bean id="orderService" class="com.example.service.OrderService">
        <constructor-arg ref="userRepository"/>
        <constructor-arg ref="emailService"/>
        <constructor-arg ref="paymentGateway"/>
    </bean>

</beans>
```

```java
// Java code to load XML configuration
public class Application {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        OrderService orderService = context.getBean(OrderService.class);
        // Now Spring manages everything!
    }
}
```

### Solution 3: Spring Java Configuration (Spring 3.0+ Era)

```java
@Configuration
public class AppConfig {
    
    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
        dataSource.setUsername("root");
        dataSource.setPassword("secret");
        return dataSource;
    }
    
    @Bean
    public UserRepository userRepository() {
        return new UserRepositoryImpl(dataSource());
    }
    
    @Bean
    public EmailService emailService() {
        SmtpEmailService service = new SmtpEmailService();
        service.setHost("smtp.example.com");
        service.setPort(587);
        return service;
    }
    
    @Bean
    public PaymentGateway paymentGateway(@Value("${stripe.api.key}") String apiKey) {
        return new StripePaymentGateway(apiKey);
    }
    
    @Bean
    public OrderService orderService(UserRepository userRepository, 
                                      EmailService emailService, 
                                      PaymentGateway paymentGateway) {
        // Dependencies are injected by Spring, not called directly
        return new OrderService(userRepository, emailService, paymentGateway);
    }
}

// Load Java configuration
public class Application {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        OrderService orderService = context.getBean(OrderService.class);
    }
}
```

### Solution 4: Spring Boot Auto-Configuration (Modern Era)

```java
// That's it! Spring Boot auto-configures everything.

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// Just use annotations - Spring Boot handles the rest
@Repository
public interface UserRepository extends JpaRepository<User, Long> { }

@Service
public class OrderService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PaymentGateway paymentGateway;
    
    // No @Autowired needed in Spring Boot!
    public OrderService(UserRepository userRepository, 
                        EmailService emailService,
                        PaymentGateway paymentGateway) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.paymentGateway = paymentGateway;
    }
}
```

```yaml
# application.yml - External configuration
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: secret

email:
  host: smtp.example.com
  port: 587

stripe:
  api:
    key: ${STRIPE_API_KEY}
```

### Comparison: Configuration Evolution

| Aspect | Manual | XML Config | Java Config | Spring Boot |
|--------|--------|------------|-------------|-------------|
| **Verbosity** | High | Very High | Medium | Minimal |
| **Type Safety** | Yes | No | Yes | Yes |
| **IDE Support** | Limited | Limited | Good | Excellent |
| **Refactoring** | Hard | Very Hard | Good | Excellent |
| **Learning Curve** | Low | Medium | Medium | Low |
| **Boilerplate** | Maximum | High | Medium | Minimal |
| **Auto-Configuration** | None | None | None | Yes |
| **Configuration Location** | Code | XML files | Java classes | Properties/YAML |

---

## Spring IoC Container Deep Dive

### What is the IoC Container?

The **IoC (Inversion of Control) Container** is the core of Spring Framework. It's responsible for:

1. **Creating beans** (instantiating objects)
2. **Configuring beans** (setting properties)
3. **Managing bean lifecycle** (initialization, destruction)
4. **Wiring beans together** (dependency injection)

### Types of IoC Containers

**1. BeanFactory** - Basic container (lazy initialization)
```java
// Basic, lightweight container
// Beans are created when requested (lazy)
Resource resource = new ClassPathResource("beans.xml");
BeanFactory factory = new XmlBeanFactory(resource);  // Deprecated in newer Spring

// Modern equivalent
BeanFactory factory = new DefaultListableBeanFactory();
XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
reader.loadBeanDefinitions(new ClassPathResource("beans.xml"));

MyBean bean = (MyBean) factory.getBean("myBean");  // Created NOW, not at startup
```

**2. ApplicationContext** - Advanced container (eager initialization)
```java
// Full-featured container
// Beans are created at startup (eager)
// This is what you use 99% of the time

// Traditional Spring - XML
ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

// Traditional Spring - Java Config
ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

// Spring Boot - Automatic
// @SpringBootApplication creates ApplicationContext automatically
```

### BeanFactory vs ApplicationContext

| Feature | BeanFactory | ApplicationContext |
|---------|-------------|-------------------|
| **Initialization** | Lazy (on-demand) | Eager (at startup) |
| **AOP Support** | Basic | Full |
| **Event Publishing** | No | Yes |
| **Message Resources (i18n)** | No | Yes |
| **Environment Abstraction** | Basic | Full |
| **BeanPostProcessor** | Manual registration | Automatic |
| **Use Case** | Resource-constrained environments | Standard applications |

### How Spring Boot Enhances the Container

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        // SpringApplication.run() creates a special ApplicationContext
        // that includes auto-configuration capabilities
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        
        // This context has:
        // - Auto-configured beans (DataSource, EntityManager, etc.)
        // - Property sources (application.properties, environment variables)
        // - Conditional bean registration
        // - Embedded server (if web application)
    }
}
```

---

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

### Understanding DI Types: Evolution from Spring to Spring Boot

**The Big Picture:**
| DI Type | Spring 2.x | Spring 3.x+ | Spring Boot |
|---------|-----------|-------------|-------------|
| Constructor | XML only | XML + @Autowired | Auto (no annotation needed) |
| Setter | XML only | XML + @Autowired | @Autowired (optional deps) |
| Field | N/A | @Autowired | @Autowired (avoid in prod) |

### 1. Constructor Injection (Recommended ✅)

**Best practice** for required dependencies.

**Why Constructor Injection is King:**

```java
@Service
public class OrderService {
    
    // Final fields = immutable = thread-safe
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final EmailService emailService;
    
    // In Spring Boot (4.3+): @Autowired is OPTIONAL for single constructor
    // Spring automatically injects dependencies
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

**Evolution of Constructor Injection:**

```java
// Traditional Spring (XML era)
// applicationContext.xml
<bean id="orderService" class="com.example.OrderService">
    <constructor-arg ref="orderRepository"/>
    <constructor-arg ref="paymentService"/>
    <constructor-arg ref="emailService"/>
</bean>

// Spring 3.x (Annotation era) - @Autowired required
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    
    @Autowired  // Required!
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}

// Spring Boot / Spring 4.3+ - @Autowired optional
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    
    // No @Autowired needed! Spring infers it.
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
```

**Advantages of Constructor Injection:**
| Advantage | Explanation |
|-----------|-------------|
| ✅ **Immutability** | Fields can be `final`, creating thread-safe objects |
| ✅ **Required Dependencies** | Missing dependency = compile error or startup failure |
| ✅ **Testability** | Just pass mocks to constructor - no Spring context needed |
| ✅ **No NullPointerException** | All dependencies guaranteed at construction |
| ✅ **Clear Contract** | Constructor signature shows all dependencies |
| ✅ **No Reflection** | Works with standard Java - no magic |

### 2. Setter Injection (For Optional Dependencies)

Used for **optional** dependencies that may or may not be available.

```java
@Service
public class NotificationService {
    
    // Required dependency - use constructor
    private final EmailService emailService;
    
    // Optional dependencies - use setter
    private SmsService smsService;
    private PushNotificationService pushService;
    
    // Required dependency via constructor
    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    // Optional: SMS notifications (may not be configured)
    @Autowired(required = false)
    public void setSmsService(SmsService smsService) {
        this.smsService = smsService;
    }
    
    // Optional: Push notifications
    @Autowired(required = false)
    public void setPushService(PushNotificationService pushService) {
        this.pushService = pushService;
    }
    
    public void notifyUser(User user, String message) {
        // Email is always sent (required)
        emailService.send(user.getEmail(), message);
        
        // SMS only if service is available and user has phone
        if (smsService != null && user.getPhone() != null) {
            smsService.send(user.getPhone(), message);
        }
        
        // Push only if service is available
        if (pushService != null) {
            pushService.send(user.getId(), message);
        }
    }
}
```

**Use Cases for Setter Injection:**
- Dependencies that can be changed at runtime
- Optional features that may not be configured
- Legacy code that requires setter-based configuration
- JMX managed beans

### 3. Field Injection (Avoid in Production ⚠️)

**Not recommended** for production code but widely used for simplicity.

```java
@Service
public class ProductService {
    
    // ⚠️ Field injection - convenient but problematic
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryService categoryService;
    
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}
```

**Why Field Injection is Problematic:**

```java
// The hidden problems with field injection:

public class FieldInjectionProblems {
    
    @Autowired
    private SomeService service;  // What if this is null?
    
    // Problem 1: Cannot be final (not immutable, not thread-safe)
    
    // Problem 2: Testing requires Spring or reflection
    // This test doesn't work:
    // ProductService service = new ProductService();  // service.productRepository is null!
    
    // Problem 3: Hidden dependencies
    // Looking at ProductService, you can't see its dependencies without reading the class body
    
    // Problem 4: Easy to add too many dependencies (code smell hidden)
    // Constructor with 10 parameters is obvious - 10 @Autowired fields are not
}
```

**When Field Injection is Acceptable:**
- **Test classes** (with @MockBean or @Autowired)
- **Quick prototypes** (not production code)
- **Configuration classes** (sometimes)

**Comparison: Field vs Constructor Injection**

```java
// ❌ Field Injection - Hard to test, hidden dependencies
@Service
public class BadService {
    @Autowired private UserRepository userRepo;
    @Autowired private EmailService emailService;
    @Autowired private PaymentService paymentService;
    @Autowired private InventoryService inventoryService;
    @Autowired private ShippingService shippingService;
    // ... it's easy to keep adding without noticing the class is too big
}

// ✅ Constructor Injection - Explicit, testable
@Service
public class GoodService {
    private final UserRepository userRepo;
    private final EmailService emailService;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final ShippingService shippingService;
    
    // Constructor makes it OBVIOUS this class has too many dependencies
    // This signals: maybe split into smaller services
    public GoodService(UserRepository userRepo, EmailService emailService,
                      PaymentService paymentService, InventoryService inventoryService,
                      ShippingService shippingService) {
        this.userRepo = userRepo;
        this.emailService = emailService;
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
        this.shippingService = shippingService;
    }
}
```

---

## Bean Lifecycle

### Understanding the Complete Bean Lifecycle

The bean lifecycle is one of the most important concepts in Spring. Understanding it helps you write better initialization code and avoid common pitfalls.

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

**Evolution: Spring to Spring Boot**

| Method | Spring (XML) | Spring (Java) | Spring Boot |
|--------|-------------|---------------|-------------|
| **@PostConstruct** | Required config | Available | Recommended ✅ |
| **InitializingBean** | Available | Available | Avoid (couples to Spring) |
| **init-method** | XML only | @Bean attribute | @Bean attribute |
| **@PreDestroy** | Required config | Available | Recommended ✅ |
| **DisposableBean** | Available | Available | Avoid (couples to Spring) |
| **destroy-method** | XML only | @Bean attribute | @Bean attribute |

**1. Using Annotations (Recommended in Spring Boot)**
```java
@Component
public class DatabaseConnection {
    private Connection connection;
    
    @PostConstruct  // Called after all dependencies are injected
    public void init() {
        System.out.println("Initializing database connection...");
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb");
    }
    
    @PreDestroy  // Called before bean is destroyed
    public void cleanup() {
        System.out.println("Closing database connection...");
        if (connection != null) {
            connection.close();
        }
    }
}
```

**Why @PostConstruct/@PreDestroy are Best:**
- Standard JSR-250 annotations (not Spring-specific)
- Work in any CDI container (Quarkus, Micronaut, etc.)
- Clear and readable
- Automatic in Spring Boot (no configuration needed)

**2. Using Interfaces (Couples Code to Spring - Avoid)**
```java
@Component
public class CacheManager implements InitializingBean, DisposableBean {
    private Map<String, Object> cache;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Initializing cache...");
        cache = new ConcurrentHashMap<>();
    }
    
    @Override
    public void destroy() throws Exception {
        System.out.println("Clearing cache...");
        cache.clear();
    }
}
// ⚠️ This couples your code to Spring - harder to test, less portable
```

**3. Using @Bean Attributes (For Third-Party Classes)**
```java
@Configuration
public class AppConfig {
    
    @Bean(initMethod = "initialize", destroyMethod = "cleanup")
    public ThirdPartyConnection thirdPartyConnection() {
        // Use this for classes you don't control
        return new ThirdPartyConnection();
    }
}
```

### Aware Interfaces - Accessing Spring Infrastructure

Spring provides several "Aware" interfaces to access container infrastructure:

```java
@Component
public class ApplicationContextProvider implements ApplicationContextAware,
                                                   BeanNameAware,
                                                   BeanFactoryAware,
                                                   EnvironmentAware {
    private ApplicationContext applicationContext;
    private String beanName;
    private BeanFactory beanFactory;
    private Environment environment;
    
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
    
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
    
    // Utility methods
    public <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }
    
    public String getProperty(String key) {
        return environment.getProperty(key);
    }
}
```

**Common Aware Interfaces:**

| Interface | What it provides | Use case |
|-----------|-----------------|----------|
| `ApplicationContextAware` | Application context | Get beans programmatically |
| `BeanNameAware` | Bean's name | Logging, debugging |
| `BeanFactoryAware` | Bean factory | Advanced scenarios |
| `EnvironmentAware` | Environment properties | Access configuration |
| `ResourceLoaderAware` | Resource loader | Load files/resources |
| `ApplicationEventPublisherAware` | Event publisher | Publish application events |

**Spring Boot Alternative - Just Inject What You Need:**
```java
@Component
public class ModernApproach {
    
    // In Spring Boot, just inject the context if you need it
    private final ApplicationContext context;
    private final Environment environment;
    
    public ModernApproach(ApplicationContext context, Environment environment) {
        this.context = context;
        this.environment = environment;
    }
}
```

---

## Bean Scopes

### Understanding Bean Scopes

**What are Bean Scopes?**
Scopes define when and how Spring creates bean instances. This affects memory usage, thread-safety, and application behavior.

### Spring vs Spring Boot Scopes

| Scope | Spring | Spring Boot | Typical Use |
|-------|--------|-------------|-------------|
| `singleton` | Default | Default | Stateless services |
| `prototype` | Available | Available | Stateful objects |
| `request` | Web only | Web only | Request-specific data |
| `session` | Web only | Web only | User session data |
| `application` | Web only | Web only | App-wide shared state |
| `websocket` | Web only | Web only | WebSocket sessions |

### 1. Singleton Scope (Default)

One instance per Spring IoC container.

```java
@Component  // Singleton by default
// @Scope("singleton")  // Explicit, but unnecessary
public class DatabaseConfig {
    
    // Shared across ENTIRE application
    // Must be thread-safe!
    private final String url;
    
    public DatabaseConfig(@Value("${db.url}") String url) {
        this.url = url;
        System.out.println("DatabaseConfig created: " + this);
    }
}
```

**When to use Singleton:**
- ✅ Stateless services (most services)
- ✅ Configuration holders
- ✅ Cache managers
- ✅ Connection pools

**⚠️ Warning: Singleton doesn't mean thread-safe!**
```java
@Service  // Singleton
public class BadCounterService {
    private int count = 0;  // ❌ Not thread-safe!
    
    public void increment() {
        count++;  // Race condition!
    }
}

@Service  // Singleton
public class GoodCounterService {
    private final AtomicInteger count = new AtomicInteger(0);  // ✅ Thread-safe
    
    public void increment() {
        count.incrementAndGet();
    }
}
```

### 2. Prototype Scope

New instance every time the bean is requested.

```java
@Component
@Scope("prototype")
public class ReportGenerator {
    
    private final String reportId;
    private List<String> data = new ArrayList<>();
    
    public ReportGenerator() {
        this.reportId = UUID.randomUUID().toString();
        System.out.println("New ReportGenerator created: " + reportId);
    }
    
    public void addData(String item) {
        data.add(item);  // Safe - each request gets its own instance
    }
    
    public Report generate() {
        return new Report(reportId, data);
    }
}
```

**When to use Prototype:**
- Stateful objects
- Objects that shouldn't be shared
- Builder patterns
- Objects with request-specific data

**⚠️ Important: Prototype beans injected into Singletons**
```java
@Service  // Singleton
public class ReportService {
    
    private final ReportGenerator generator;  // ❌ Problem!
    
    // generator is created ONCE and reused forever
    // Prototype behavior is lost!
    public ReportService(ReportGenerator generator) {
        this.generator = generator;
    }
}

// ✅ Solution 1: Inject ApplicationContext
@Service
public class ReportService {
    
    private final ApplicationContext context;
    
    public ReportService(ApplicationContext context) {
        this.context = context;
    }
    
    public Report createReport() {
        // Get fresh instance each time
        ReportGenerator generator = context.getBean(ReportGenerator.class);
        return generator.generate();
    }
}

// ✅ Solution 2: Use ObjectFactory or Provider
@Service
public class ReportService {
    
    private final ObjectFactory<ReportGenerator> generatorFactory;
    
    public ReportService(ObjectFactory<ReportGenerator> generatorFactory) {
        this.generatorFactory = generatorFactory;
    }
    
    public Report createReport() {
        ReportGenerator generator = generatorFactory.getObject();
        return generator.generate();
    }
}
```

### 3. Request Scope (Web Applications)

One instance per HTTP request.

```java
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext {
    
    private final String requestId;
    private final LocalDateTime startTime;
    private Map<String, Object> attributes = new HashMap<>();
    
    public RequestContext() {
        this.requestId = UUID.randomUUID().toString();
        this.startTime = LocalDateTime.now();
    }
    
    // Each HTTP request gets its own instance
    // Destroyed when request completes
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
