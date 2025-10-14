# Day 12: Introduction to Microservices Architecture

## 📋 Table of Contents
- [Introduction](#introduction)
- [What are Microservices?](#what-are-microservices)
- [Monolithic vs Microservices](#monolithic-vs-microservices)
- [Microservices Principles](#microservices-principles)
- [Design Patterns](#design-patterns)
- [Communication Patterns](#communication-patterns)
- [Building Your First Microservice](#building-your-first-microservice)
- [Challenges and Solutions](#challenges-and-solutions)
- [Exercises](#exercises)
- [Resources](#resources)

## Introduction

Welcome to Day 12! Today marks a significant milestone as we transition from building monolithic Spring Boot applications to understanding and implementing microservices architecture. This is where your journey into distributed systems begins!

## What are Microservices?

### Definition
Microservices is an architectural style that structures an application as a collection of small, autonomous services modeled around a business domain.

### Key Characteristics

**1. Single Responsibility**
- Each service focuses on one business capability
- Service boundaries align with business domains
- High cohesion within service, low coupling between services

**2. Autonomous**
- Can be developed independently
- Can be deployed independently
- Can be scaled independently
- Can fail independently

**3. Decentralized**
- Decentralized data management
- Decentralized governance
- Technology diversity

**4. Built Around Business Capabilities**
- Organized by business domain
- Cross-functional teams
- End-to-end ownership

## Monolithic vs Microservices

### Monolithic Architecture

```
┌─────────────────────────────────────┐
│                                     │
│     Monolithic Application          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │    User Interface           │   │
│  ├─────────────────────────────┤   │
│  │    Business Logic           │   │
│  │  • User Management          │   │
│  │  • Order Processing         │   │
│  │  • Inventory Management     │   │
│  │  • Payment Processing       │   │
│  ├─────────────────────────────┤   │
│  │    Data Access Layer        │   │
│  └─────────────────────────────┘   │
│                │                    │
└────────────────┼────────────────────┘
                 │
         ┌───────▼────────┐
         │    Database    │
         └────────────────┘
```

**Advantages:**
- Simple to develop initially
- Simple to test
- Simple to deploy
- Simple to scale horizontally (multiple instances)

**Disadvantages:**
- Large codebase becomes difficult to maintain
- Long-term commitment to technology stack
- Scaling requires scaling entire application
- Deployment of small changes requires redeploying entire application
- Reliability issues (one bug can crash entire system)

### Microservices Architecture

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   Client     │    │  API Gateway │    │   Service    │
│ Applications │───▶│              │───▶│   Discovery  │
└──────────────┘    └──────┬───────┘    └──────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
    ┌───▼───┐         ┌────▼────┐       ┌────▼────┐
    │ User  │         │ Order   │       │Product  │
    │Service│         │ Service │       │Service  │
    └───┬───┘         └────┬────┘       └────┬────┘
        │                  │                  │
    ┌───▼────┐        ┌────▼─────┐      ┌────▼────┐
    │User DB │        │Order DB  │      │Product  │
    └────────┘        └──────────┘      │   DB    │
                                        └─────────┘
```

**Advantages:**
- Independent deployment
- Technology diversity
- Better fault isolation
- Independent scaling
- Organized around business capabilities
- Easier to understand and maintain (smaller codebases)

**Disadvantages:**
- Distributed system complexity
- Operational overhead
- Deployment complexity
- Testing complexity
- Data consistency challenges
- Network latency

## Microservices Principles

### 1. Single Responsibility Principle
```
Bad: Order Service handling orders, payments, and shipping
Good: 
  - Order Service (manages orders)
  - Payment Service (handles payments)
  - Shipping Service (manages deliveries)
```

### 2. Autonomous Services
Each service should:
- Own its data
- Be independently deployable
- Have its own release cycle
- Maintain backward compatibility

### 3. Domain-Driven Design (DDD)

**Bounded Context:**
```java
// User Service - User bounded context
@Entity
public class User {
    private Long id;
    private String name;
    private String email;
    // User-specific attributes
}

// Order Service - Order bounded context
@Entity
public class Customer {  // Different model for same entity
    private Long userId;
    private String name;
    // Only order-relevant attributes
}
```

### 4. Smart Endpoints, Dumb Pipes
- Services communicate using simple protocols (HTTP/REST, messaging)
- Business logic stays in services, not in middleware
- Prefer choreography over orchestration

## Design Patterns

### 1. Database per Service

**Pattern:**
Each microservice has its own database.

```
User Service     Order Service    Product Service
     │                │                  │
     ▼                ▼                  ▼
  User DB          Order DB          Product DB
```

**Benefits:**
- Service independence
- Technology diversity
- Better scalability

**Challenges:**
- Data duplication
- Maintaining data consistency
- Complex queries across services

### 2. API Gateway Pattern

**Pattern:**
Single entry point for all client requests.

```java
@RestController
@RequestMapping("/api")
public class ApiGateway {
    
    @Autowired
    private UserServiceClient userService;
    
    @Autowired
    private OrderServiceClient orderService;
    
    @GetMapping("/user-orders/{userId}")
    public UserOrdersDTO getUserWithOrders(@PathVariable Long userId) {
        User user = userService.getUser(userId);
        List<Order> orders = orderService.getUserOrders(userId);
        return new UserOrdersDTO(user, orders);
    }
}
```

**Responsibilities:**
- Request routing
- Authentication and authorization
- Rate limiting
- Request/response transformation
- Load balancing

### 3. Service Registry Pattern

**Pattern:**
Services register themselves and discover other services.

```java
// Eureka Server
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}

// Service Registration
@SpringBootApplication
@EnableEurekaClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

### 4. Circuit Breaker Pattern

**Pattern:**
Prevent cascading failures by failing fast.

```java
@Service
public class OrderService {
    
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackInventory")
    public boolean checkInventory(Long productId) {
        return inventoryClient.isAvailable(productId);
    }
    
    public boolean fallbackInventory(Long productId, Exception e) {
        log.error("Inventory service unavailable", e);
        return false; // Fail gracefully
    }
}
```

## Communication Patterns

### 1. Synchronous Communication (REST)

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @PostMapping
    public Order createOrder(@RequestBody OrderRequest request) {
        // Call User Service
        User user = restTemplate.getForObject(
            "http://user-service/api/users/" + request.getUserId(),
            User.class
        );
        
        // Call Product Service
        Product product = restTemplate.getForObject(
            "http://product-service/api/products/" + request.getProductId(),
            Product.class
        );
        
        // Create order
        return orderRepository.save(new Order(user, product));
    }
}
```

**Pros:**
- Simple and straightforward
- Immediate response
- Easy to understand

**Cons:**
- Tight coupling
- Service availability dependency
- Network latency accumulation

### 2. Asynchronous Communication (Messaging)

```java
@Service
public class OrderService {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public Order createOrder(OrderRequest request) {
        Order order = orderRepository.save(new Order(request));
        
        // Send event
        OrderCreatedEvent event = new OrderCreatedEvent(order.getId());
        rabbitTemplate.convertAndSend("order-exchange", "order.created", event);
        
        return order;
    }
}

@Service
public class NotificationService {
    
    @RabbitListener(queues = "notification-queue")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Send notification asynchronously
        emailService.sendOrderConfirmation(event.getOrderId());
    }
}
```

**Pros:**
- Loose coupling
- Better fault tolerance
- Natural load leveling

**Cons:**
- Complexity
- Eventual consistency
- Debugging challenges

## Building Your First Microservice

### User Service Example

**1. Project Structure**
```
user-service/
├── src/main/java/com/example/userservice/
│   ├── UserServiceApplication.java
│   ├── controller/
│   │   └── UserController.java
│   ├── service/
│   │   └── UserService.java
│   ├── repository/
│   │   └── UserRepository.java
│   └── model/
│       └── User.java
├── src/main/resources/
│   └── application.yml
└── pom.xml
```

**2. Dependencies (pom.xml)**
```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- H2 Database (for development) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Eureka Client -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
</dependencies>
```

**3. Configuration (application.yml)**
```yaml
spring:
  application:
    name: user-service
  datasource:
    url: jdbc:h2:mem:userdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

server:
  port: 8081

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    preferIpAddress: true
```

**4. Implementation**
```java
// Model
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String phone;
    
    // Constructors, getters, setters
}

// Repository
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

// Service
@Service
public class UserService {
    
    @Autowired
    private UserRepository repository;
    
    public User createUser(User user) {
        return repository.save(user);
    }
    
    public User getUser(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public List<User> getAllUsers() {
        return repository.findAll();
    }
}

// Controller
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService service;
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(service.createUser(user));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(service.getUser(id));
    }
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }
}
```

## Challenges and Solutions

### 1. Distributed Data Management

**Challenge:** Maintaining data consistency across services.

**Solutions:**
- Saga Pattern for distributed transactions
- Event Sourcing
- CQRS (Command Query Responsibility Segregation)

### 2. Service Discovery

**Challenge:** Services need to find each other dynamically.

**Solution:** Service Registry (Eureka, Consul, Zookeeper)

### 3. Configuration Management

**Challenge:** Managing configuration across multiple services.

**Solution:** Centralized Configuration Server (Spring Cloud Config)

### 4. Monitoring and Debugging

**Challenge:** Tracking requests across multiple services.

**Solutions:**
- Distributed Tracing (Sleuth, Zipkin)
- Centralized Logging (ELK Stack)
- Metrics and Monitoring (Prometheus, Grafana)

### 5. Security

**Challenge:** Authentication and authorization across services.

**Solutions:**
- API Gateway for centralized security
- JWT tokens for stateless authentication
- OAuth2 for authorization

## Exercises

### Exercise 1: Design Microservices
Design a microservices architecture for an e-commerce application:
- Identify services (User, Product, Order, Payment, Inventory)
- Define service boundaries
- Identify communication patterns
- Design database strategy

### Exercise 2: Create User Service
Build a complete User Service with:
- CRUD operations
- H2 in-memory database
- REST API endpoints
- Error handling

### Exercise 3: Create Product Service
Build a Product Service that:
- Manages product catalog
- Handles inventory
- Provides search functionality
- Returns product details

### Exercise 4: Service Communication
Create two services that communicate:
- Order Service creates orders
- Calls User Service to validate user
- Calls Product Service to check availability

### Exercise 5: Service Discovery
Implement service discovery:
- Set up Eureka Server
- Register User and Product services
- Test service discovery

## Key Takeaways

✅ Microservices organize applications around business capabilities  
✅ Each service owns its data and can be deployed independently  
✅ Communication can be synchronous (REST) or asynchronous (messaging)  
✅ API Gateway provides single entry point for clients  
✅ Service discovery enables dynamic service location  
✅ Circuit breakers prevent cascading failures  

## Resources

### Books
- "Building Microservices" by Sam Newman
- "Microservices Patterns" by Chris Richardson
- "Spring Microservices in Action" by John Carnell

### Websites
- [Microservices.io](https://microservices.io/) - Patterns and practices
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Martin Fowler's Microservices Guide](https://martinfowler.com/microservices/)

### Tools
- Spring Cloud Netflix
- Docker and Kubernetes
- API Gateway tools (Spring Cloud Gateway, Kong)

## Next Steps

Tomorrow, we'll implement **Service Discovery with Eureka** to enable dynamic service registration and discovery in our microservices architecture!

---

[<< Previous: Day 11](../Day11_JWT_Auth/README.md) | [Back to Main](../README.md) | [Next: Day 13 >>](../Day13_Eureka_Discovery/README.md)
