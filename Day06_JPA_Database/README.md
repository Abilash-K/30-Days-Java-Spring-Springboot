# Day 06: JPA and Database Integration

## 📋 Table of Contents
- [Introduction](#introduction)
- [What is JPA?](#what-is-jpa)
- [Setting Up Database](#setting-up-database)
- [Entity Mapping](#entity-mapping)
- [Repositories](#repositories)
- [JPQL and Native Queries](#jpql-and-native-queries)
- [Relationships](#relationships)
- [Transaction Management](#transaction-management)
- [Best Practices](#best-practices)
- [Exercises](#exercises)
- [Resources](#resources)

## Introduction

Welcome to Day 6! Today we'll integrate databases into our Spring Boot applications using Spring Data JPA. You'll learn how to perform database operations without writing complex SQL queries.

## What is JPA?

### Java Persistence API (JPA)
- Standard specification for Object-Relational Mapping (ORM)
- Maps Java objects to database tables
- Provides database-agnostic API
- Hibernate is the most popular JPA implementation

### Benefits
- **Reduced boilerplate**: No need to write SQL for CRUD operations
- **Database independence**: Switch databases with minimal changes
- **Object-oriented**: Work with objects instead of SQL
- **Automatic schema generation**: Create tables from entities
- **Caching**: Built-in first and second-level caching
- **Relationship management**: Handle complex relationships easily

## Setting Up Database

### Dependencies (pom.xml)

```xml
<dependencies>
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
    
    <!-- PostgreSQL (for production) -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- MySQL -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### Configuration (application.yml)

**H2 Database (Development):**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect
```

**PostgreSQL (Production):**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          lob:
            non_contextual_creation: true
```

**MySQL:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb?useSSL=false
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

### DDL Auto Options
- `create`: Drop and create tables on startup
- `create-drop`: Drop tables on shutdown
- `update`: Update schema (keep data)
- `validate`: Validate schema, make no changes
- `none`: No action

## Entity Mapping

### Basic Entity

```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false)
    private String email;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors, getters, setters
}
```

### Column Types

```java
@Entity
public class DataTypesExample {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // String
    @Column(length = 100)
    private String name;
    
    // Text (large strings)
    @Lob
    private String description;
    
    // Integer types
    private Integer count;
    private Long bigCount;
    private Short smallCount;
    
    // Decimal
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    // Boolean
    private Boolean active;
    
    // Date and Time
    private LocalDate birthDate;
    private LocalTime startTime;
    private LocalDateTime createdAt;
    
    // Enum
    @Enumerated(EnumType.STRING)
    private Status status;
    
    // Binary data
    @Lob
    private byte[] image;
    
    // JSON (PostgreSQL)
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String metadata;
}

enum Status {
    ACTIVE, INACTIVE, PENDING
}
```

### Composite Primary Key

**Using @IdClass:**
```java
@Entity
@IdClass(OrderItemId.class)
public class OrderItem {
    
    @Id
    private Long orderId;
    
    @Id
    private Long productId;
    
    private Integer quantity;
    private BigDecimal price;
    
    // Getters, setters
}

public class OrderItemId implements Serializable {
    private Long orderId;
    private Long productId;
    
    // equals, hashCode, constructors
}
```

**Using @EmbeddedId:**
```java
@Embeddable
public class OrderItemId implements Serializable {
    private Long orderId;
    private Long productId;
    
    // equals, hashCode, constructors, getters, setters
}

@Entity
public class OrderItem {
    
    @EmbeddedId
    private OrderItemId id;
    
    private Integer quantity;
    private BigDecimal price;
    
    // Getters, setters
}
```

## Repositories

### JpaRepository Interface

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository provides:
    // - save(entity)
    // - saveAll(entities)
    // - findById(id)
    // - findAll()
    // - count()
    // - deleteById(id)
    // - delete(entity)
    // - existsById(id)
}
```

### Custom Query Methods

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find by single field
    Optional<User> findByEmail(String email);
    List<User> findByFirstName(String firstName);
    
    // Multiple conditions (AND)
    List<User> findByFirstNameAndLastName(String firstName, String lastName);
    
    // OR condition
    List<User> findByFirstNameOrLastName(String firstName, String lastName);
    
    // Like/Contains
    List<User> findByEmailContaining(String emailPart);
    List<User> findByFirstNameStartingWith(String prefix);
    List<User> findByLastNameEndingWith(String suffix);
    
    // Comparison
    List<User> findByAgeGreaterThan(Integer age);
    List<User> findByAgeLessThanEqual(Integer age);
    List<User> findByAgeBetween(Integer startAge, Integer endAge);
    
    // Date comparison
    List<User> findByCreatedAtAfter(LocalDateTime date);
    List<User> findByCreatedAtBefore(LocalDateTime date);
    
    // Null checks
    List<User> findByMiddleNameIsNull();
    List<User> findByMiddleNameIsNotNull();
    
    // Boolean
    List<User> findByActiveTrue();
    List<User> findByActiveFalse();
    
    // In clause
    List<User> findByIdIn(List<Long> ids);
    
    // Sorting
    List<User> findByLastNameOrderByFirstNameAsc(String lastName);
    
    // Pagination
    Page<User> findByActive(Boolean active, Pageable pageable);
    
    // Count
    Long countByActive(Boolean active);
    
    // Exists
    Boolean existsByEmail(String email);
    
    // Delete
    void deleteByEmail(String email);
    Long deleteByActive(Boolean active);
    
    // Top/First
    User findFirstByOrderByCreatedAtDesc();
    List<User> findTop10ByOrderByCreatedAtDesc();
    
    // Distinct
    List<User> findDistinctByLastName(String lastName);
}
```

## JPQL and Native Queries

### JPQL Queries

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Simple JPQL
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> findByEmailQuery(String email);
    
    // Named parameters
    @Query("SELECT u FROM User u WHERE u.firstName = :firstName AND u.lastName = :lastName")
    List<User> findByFullName(@Param("firstName") String firstName, 
                              @Param("lastName") String lastName);
    
    // JPQL with JOIN
    @Query("SELECT u FROM User u JOIN u.orders o WHERE o.status = :status")
    List<User> findUsersWithOrderStatus(@Param("status") OrderStatus status);
    
    // JPQL with aggregation
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    Long countActiveUsers();
    
    @Query("SELECT u.lastName, COUNT(u) FROM User u GROUP BY u.lastName")
    List<Object[]> countByLastName();
    
    // Update query
    @Modifying
    @Query("UPDATE User u SET u.active = :active WHERE u.id = :id")
    int updateUserStatus(@Param("id") Long id, @Param("active") Boolean active);
    
    // Delete query
    @Modifying
    @Query("DELETE FROM User u WHERE u.createdAt < :date")
    int deleteOldUsers(@Param("date") LocalDateTime date);
}
```

### Native SQL Queries

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Native SQL
    @Query(value = "SELECT * FROM users WHERE email = ?1", nativeQuery = true)
    User findByEmailNative(String email);
    
    // Native SQL with JOIN
    @Query(value = "SELECT u.* FROM users u " +
                   "INNER JOIN orders o ON u.id = o.user_id " +
                   "WHERE o.total > :amount", 
           nativeQuery = true)
    List<User> findUsersWithOrdersAbove(@Param("amount") BigDecimal amount);
    
    // Native SQL with pagination
    @Query(value = "SELECT * FROM users WHERE active = true",
           countQuery = "SELECT count(*) FROM users WHERE active = true",
           nativeQuery = true)
    Page<User> findActiveUsersNative(Pageable pageable);
    
    // Stored procedure
    @Query(value = "CALL get_user_stats(?1)", nativeQuery = true)
    List<Object[]> getUserStats(Long userId);
}
```

### Pagination and Sorting

```java
@Service
public class UserService {
    
    @Autowired
    private UserRepository repository;
    
    public Page<User> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAll(pageable);
    }
    
    public Page<User> getUsersSorted(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return repository.findAll(pageable);
    }
    
    public Page<User> getUsersWithMultiSort(int page, int size) {
        Sort sort = Sort.by(
            Sort.Order.asc("lastName"),
            Sort.Order.desc("firstName")
        );
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findAll(pageable);
    }
}
```

## Relationships

### One-to-One

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, 
              orphanRemoval = true)
    private Profile profile;
    
    // Getters, setters
}

@Entity
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String bio;
    private String website;
    
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    
    // Getters, setters
}
```

### One-to-Many / Many-to-One

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, 
               orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();
    
    public void addOrder(Order order) {
        orders.add(order);
        order.setUser(this);
    }
    
    public void removeOrder(Order order) {
        orders.remove(order);
        order.setUser(null);
    }
    
    // Getters, setters
}

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private BigDecimal total;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    // Getters, setters
}
```

### Many-to-Many

```java
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();
    
    public void addCourse(Course course) {
        courses.add(course);
        course.getStudents().add(this);
    }
    
    public void removeCourse(Course course) {
        courses.remove(course);
        course.getStudents().remove(this);
    }
    
    // Getters, setters
}

@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();
    
    // Getters, setters
}
```

## Transaction Management

### Understanding Transactions

A **database transaction** is a sequence of operations that are executed as a single logical unit of work. Transactions must follow the **ACID** properties:

- **Atomicity**: All operations succeed or all fail (no partial completion)
- **Consistency**: Database remains in a valid state before and after transaction
- **Isolation**: Concurrent transactions don't interfere with each other
- **Durability**: Committed changes persist even after system failures

### @Transactional Annotation

The `@Transactional` annotation is Spring's declarative transaction management approach. It automatically handles transaction demarcation (begin, commit, rollback) without writing boilerplate code.

#### Basic Usage

```java
@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Transactional
    public Order createOrder(OrderRequest request) {
        // All operations in single transaction
        Order order = new Order(request);
        orderRepository.save(order);
        
        inventoryService.decreaseStock(request.getProductId(), 
                                       request.getQuantity());
        
        return order;
        // If any operation fails, entire transaction rolls back
    }
}
```

**How it works:**
1. Spring creates a proxy around the annotated method
2. Before method execution: Transaction begins
3. Method executes: All database operations participate in the same transaction
4. On success: Transaction commits
5. On exception: Transaction rolls back

#### Where to Apply @Transactional

```java
// ✅ Good: At service layer methods
@Service
public class UserService {
    @Transactional
    public void updateUserProfile(Long userId, ProfileData data) {
        // Business logic with multiple DB operations
    }
}

// ✅ Good: At class level (applies to all public methods)
@Service
@Transactional
public class OrderService {
    public void createOrder() { }
    public void updateOrder() { }
    
    @Transactional(readOnly = true)  // Override for specific method
    public Order getOrder(Long id) { }
}

// ❌ Bad: At repository layer (unnecessary, Spring Data JPA already handles it)
@Repository
@Transactional  // Not needed
public interface UserRepository extends JpaRepository<User, Long> { }

// ⚠️ Caution: Private methods don't work with @Transactional
@Service
public class PaymentService {
    @Transactional
    private void processPayment() {  // Won't work! Must be public
        // Transaction won't be created
    }
}
```

### @Transactional Attributes Explained

#### 1. Propagation

Defines how transactions relate to each other when methods call other transactional methods.

```java
public enum Propagation {
    REQUIRED,      // Default
    REQUIRES_NEW,
    SUPPORTS,
    NOT_SUPPORTED,
    MANDATORY,
    NEVER,
    NESTED
}
```

**REQUIRED (Default)**: Use existing transaction or create new one

```java
@Service
public class OrderService {
    
    @Autowired
    private PaymentService paymentService;
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void createOrder(OrderRequest request) {
        // Transaction T1 starts here
        saveOrder(request);
        
        // Joins same transaction T1
        paymentService.processPayment(request);
        
        // Both operations commit together
    }
}

@Service
public class PaymentService {
    @Transactional(propagation = Propagation.REQUIRED)
    public void processPayment(PaymentRequest request) {
        // Uses existing transaction (T1) from caller
        // If this fails, entire transaction rolls back
    }
}
```

**REQUIRES_NEW**: Always create a new transaction, suspend current one

```java
@Service
public class NotificationService {
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotification(String message) {
        // New transaction T2 created (T1 suspended)
        logNotification(message);
        sendEmail(message);
        // T2 commits independently
        // Even if caller's transaction fails, this notification is saved
    }
}

// Use case: Audit logging that should persist regardless of main transaction
@Service
public class OrderService {
    @Autowired
    private NotificationService notificationService;
    
    @Transactional
    public void createOrder(OrderRequest request) {
        // Transaction T1
        saveOrder(request);
        
        // Creates new transaction T2 (T1 suspended)
        notificationService.sendNotification("Order created");
        
        // Back to T1
        // If this fails, order rolls back but notification persists
        updateInventory(request);
    }
}
```

**SUPPORTS**: Use transaction if exists, otherwise execute non-transactionally

```java
@Transactional(propagation = Propagation.SUPPORTS)
public User getUser(Long id) {
    // If caller has transaction, joins it
    // If no transaction exists, executes without transaction
    return userRepository.findById(id).orElse(null);
}
```

**NOT_SUPPORTED**: Execute non-transactionally, suspend existing transaction

```java
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public void generateReport() {
    // Long-running operation
    // Suspends any existing transaction
    // Executes without transaction to avoid holding locks
}
```

**MANDATORY**: Must have existing transaction, throw exception otherwise

```java
@Transactional(propagation = Propagation.MANDATORY)
public void updateInventory(Long productId, int quantity) {
    // Must be called from within a transaction
    // Throws exception if no transaction exists
    // Enforces that this method should never run standalone
}
```

**NEVER**: Must not have transaction, throw exception if exists

```java
@Transactional(propagation = Propagation.NEVER)
public void performNonTransactionalOperation() {
    // Must execute outside transaction
    // Throws exception if called within transaction
}
```

**NESTED**: Execute within nested transaction (savepoint)

```java
@Transactional(propagation = Propagation.NESTED)
public void optionalOperation() {
    // Creates savepoint in existing transaction
    // Can rollback to savepoint without affecting outer transaction
    try {
        riskyOperation();
    } catch (Exception e) {
        // Only this nested transaction rolls back
        // Outer transaction can continue
    }
}
```

#### 2. Isolation

Controls how concurrent transactions see each other's changes. Higher isolation = more safety but less concurrency.

```java
public enum Isolation {
    DEFAULT,           // Use database default
    READ_UNCOMMITTED,  // Lowest isolation, highest performance
    READ_COMMITTED,    // Default for most databases
    REPEATABLE_READ,   // Default for MySQL
    SERIALIZABLE       // Highest isolation, lowest performance
}
```

**READ_UNCOMMITTED**: Can read uncommitted changes (dirty reads)

```java
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public BigDecimal getTotalSales() {
    // Can read data being modified by other transactions
    // Fastest but least safe
    // Problem: Might read data that gets rolled back (dirty read)
}

// Scenario:
// Transaction 1: Updates product price to $100 (not committed)
// Transaction 2 (READ_UNCOMMITTED): Reads price as $100
// Transaction 1: Rolls back (price stays $50)
// Transaction 2: Used wrong price!
```

**READ_COMMITTED**: Only read committed data (prevents dirty reads)

```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public List<Order> getOrders() {
    // Only sees committed data
    // Problem: Non-repeatable reads
    // Same query can return different results during transaction
}

// Scenario:
// Transaction 1: Reads order total = $100
// Transaction 2: Updates order total to $150, commits
// Transaction 1: Reads same order again = $150 (different!)
```

**REPEATABLE_READ**: Same query returns same results (prevents non-repeatable reads)

```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public void processOrder(Long orderId) {
    Order order1 = orderRepository.findById(orderId).get();
    // ... some processing ...
    Order order2 = orderRepository.findById(orderId).get();
    // order1 and order2 will have same data
    // Even if another transaction updates it
    
    // Problem: Phantom reads
    // New rows can still appear
}

// Scenario:
// Transaction 1: Reads all orders with status "PENDING" → 10 orders
// Transaction 2: Inserts new PENDING order, commits
// Transaction 1: Reads PENDING orders again → 11 orders (phantom!)
```

**SERIALIZABLE**: Strictest isolation (prevents phantom reads)

```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public void criticalOperation() {
    // Transactions execute as if they were serial (one after another)
    // Complete isolation, no dirty/non-repeatable/phantom reads
    // Slowest, can cause many deadlocks
    
    // Use for: Financial transactions, inventory management
}
```

**Isolation Levels Comparison:**

| Isolation Level | Dirty Read | Non-Repeatable Read | Phantom Read | Performance |
|----------------|------------|---------------------|--------------|-------------|
| READ_UNCOMMITTED | ✅ Possible | ✅ Possible | ✅ Possible | Fastest |
| READ_COMMITTED | ❌ Prevented | ✅ Possible | ✅ Possible | Fast |
| REPEATABLE_READ | ❌ Prevented | ❌ Prevented | ✅ Possible | Moderate |
| SERIALIZABLE | ❌ Prevented | ❌ Prevented | ❌ Prevented | Slowest |

#### 3. Timeout

Set maximum duration (in seconds) for transaction execution.

```java
@Transactional(timeout = 30)  // 30 seconds
public void longRunningOperation() {
    // If this takes more than 30 seconds
    // Transaction automatically rolls back
    // Throws TransactionTimedOutException
}

// Use cases:
// 1. Prevent long-running transactions holding locks
// 2. Fail fast instead of hanging indefinitely
// 3. Resource management in high-load systems

@Service
public class ReportService {
    @Transactional(timeout = 60)
    public Report generateMonthlyReport() {
        // Complex query that might take time
        // Better to timeout than lock resources indefinitely
    }
}
```

#### 4. readOnly

Optimization hint for read-only operations.

```java
@Transactional(readOnly = true)
public List<Order> getOrders() {
    return orderRepository.findAll();
}
```

**Benefits:**
1. **Performance Optimization**: Database/ORM can optimize read-only queries
2. **Prevents Accidental Writes**: Some databases enforce read-only constraint
3. **Flush Mode**: Hibernate won't flush changes (saves memory/CPU)
4. **Routing**: Can route to read replicas in master-slave setup

```java
@Service
public class ProductService {
    
    // ✅ Good: Explicit read-only for queries
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContaining(keyword, pageable);
    }
    
    @Transactional(readOnly = true)
    public ProductStatistics getStatistics() {
        // Complex read operations
        // Database knows it won't modify data
        return calculateStatistics();
    }
    
    // ✅ Good: Write operations without readOnly
    @Transactional
    public Product createProduct(ProductRequest request) {
        // Needs write access
        return productRepository.save(new Product(request));
    }
    
    // ❌ Bad: Using readOnly with write operations
    @Transactional(readOnly = true)
    public void updateProduct(Long id, String name) {
        Product product = productRepository.findById(id).get();
        product.setName(name);
        productRepository.save(product);  // May not work or throw exception!
    }
}
```

#### 5. rollbackFor / noRollbackFor

Control which exceptions trigger rollback.

**Default Behavior:**
- **Unchecked exceptions** (RuntimeException and Error): Rollback
- **Checked exceptions** (Exception): Commit (no rollback)

```java
// Default behavior
@Transactional
public void defaultBehavior() throws Exception {
    saveOrder();
    
    // RuntimeException → Rollback
    if (error) throw new IllegalStateException("Error");
    
    // Checked Exception → Commit (!)
    if (error) throw new Exception("Error");  // Transaction commits!
}
```

**rollbackFor**: Force rollback for specific exceptions

```java
// Rollback on checked exceptions
@Transactional(rollbackFor = Exception.class)
public void processOrder() throws Exception {
    saveOrder();
    
    // Now checked exceptions also trigger rollback
    if (error) throw new Exception("Error");  // Rollback!
}

// Multiple exceptions
@Transactional(rollbackFor = {
    SQLException.class,
    IOException.class,
    CustomBusinessException.class
})
public void complexOperation() {
    // Rolls back on any of these exceptions
}
```

**noRollbackFor**: Prevent rollback for specific exceptions

```java
// Don't rollback on validation errors
@Transactional(noRollbackFor = ValidationException.class)
public void createUser(UserRequest request) {
    validateUser(request);  // Might throw ValidationException
    saveUser(request);
    
    // If ValidationException occurs, transaction commits
    // Other exceptions still cause rollback
}

// Business scenario: Logging failed attempts
@Transactional(noRollbackFor = PaymentDeclinedException.class)
public void processPayment(PaymentRequest request) {
    logPaymentAttempt(request);  // Always saved
    
    if (!validateCard(request)) {
        throw new PaymentDeclinedException();  // No rollback, log persists
    }
    
    chargeCard(request);
}
```

**Best Practice: Explicit exception handling**

```java
@Service
public class OrderService {
    
    // Clear contract: All exceptions rollback
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(OrderRequest request) 
            throws OrderException, PaymentException {
        try {
            Order order = saveOrder(request);
            processPayment(request);
            return order;
        } catch (Exception e) {
            // Log exception
            log.error("Order creation failed", e);
            throw e;  // Rollback happens
        }
    }
}
```

### Complete Example: Real-World Scenario

```java
@Service
public class EcommerceService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AuditService auditService;
    
    /**
     * Create order with complete transaction management
     * - Saves order and decreases inventory in single transaction
     * - Rollback both if any fails
     * - Payment uses separate transaction
     * - Audit log always persists
     */
    @Transactional(
        propagation = Propagation.REQUIRED,
        isolation = Isolation.READ_COMMITTED,
        timeout = 30,
        rollbackFor = Exception.class
    )
    public Order createOrder(OrderRequest request) throws OrderException {
        try {
            // 1. Validate inventory (within this transaction)
            if (!hasInventory(request.getProductId(), request.getQuantity())) {
                throw new InsufficientInventoryException("Not enough stock");
            }
            
            // 2. Create order (within this transaction)
            Order order = new Order(request);
            order.setStatus(OrderStatus.PENDING);
            order = orderRepository.save(order);
            
            // 3. Decrease inventory (within this transaction)
            Inventory inventory = inventoryRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
            inventory.decreaseStock(request.getQuantity());
            inventoryRepository.save(inventory);
            
            // 4. Process payment (SEPARATE transaction - won't rollback with order)
            paymentService.processPayment(order);
            
            // 5. Update order status
            order.setStatus(OrderStatus.CONFIRMED);
            order = orderRepository.save(order);
            
            // 6. Send notification (SEPARATE transaction - always persists)
            notificationService.sendOrderConfirmation(order);
            
            // 7. Audit log (SEPARATE transaction - always persists)
            auditService.logOrderCreation(order);
            
            return order;
            
        } catch (Exception e) {
            // This catch doesn't prevent rollback
            // Transaction still rolls back because rollbackFor = Exception.class
            log.error("Order creation failed for request: {}", request, e);
            throw new OrderException("Failed to create order", e);
        }
    }
    
    /**
     * Read-only transaction for queries
     * Optimization: No flush, can use read replicas
     */
    @Transactional(
        readOnly = true,
        isolation = Isolation.READ_COMMITTED
    )
    public OrderDetails getOrderDetails(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        
        // Fetch related data
        order.getItems().size();  // Force lazy loading
        order.getCustomer().getName();
        
        return new OrderDetails(order);
    }
    
    /**
     * Batch operation with extended timeout
     */
    @Transactional(
        timeout = 300,  // 5 minutes
        rollbackFor = Exception.class
    )
    public void processBatchOrders(List<OrderRequest> requests) {
        for (OrderRequest request : requests) {
            processOrder(request);
        }
    }
}

@Service
public class PaymentService {
    
    /**
     * Separate transaction for payment
     * Uses REQUIRES_NEW to commit independently
     */
    @Transactional(
        propagation = Propagation.REQUIRES_NEW,
        isolation = Isolation.SERIALIZABLE,
        timeout = 15,
        rollbackFor = Exception.class
    )
    public void processPayment(Order order) throws PaymentException {
        // This runs in its own transaction
        // If order creation fails, this payment still commits
        // (Might need compensation logic)
        PaymentRecord record = new PaymentRecord(order);
        paymentRepository.save(record);
    }
}

@Service
public class AuditService {
    
    /**
     * Audit logs should always persist
     * Use REQUIRES_NEW to ensure independent commit
     */
    @Transactional(
        propagation = Propagation.REQUIRES_NEW,
        noRollbackFor = Exception.class
    )
    public void logOrderCreation(Order order) {
        AuditLog log = new AuditLog();
        log.setAction("ORDER_CREATED");
        log.setOrderId(order.getId());
        log.setTimestamp(LocalDateTime.now());
        auditRepository.save(log);
    }
}
```

### Transaction Best Practices

#### 1. Keep Transactions Short

```java
// ❌ Bad: Long transaction holding locks
@Transactional
public void processOrder(OrderRequest request) {
    Order order = saveOrder(request);
    
    // External API call - slow!
    String trackingNumber = shippingApi.createShipment(order);
    
    // Email sending - slow!
    emailService.sendConfirmation(order);
    
    // Payment processing - slow!
    paymentGateway.charge(order);
    
    updateOrder(order);
}

// ✅ Good: Minimal transactional code
@Transactional
public Order saveOrder(OrderRequest request) {
    return orderRepository.save(new Order(request));
}

public void processOrder(OrderRequest request) {
    // Only database operations in transaction
    Order order = saveOrder(request);
    
    // Non-transactional operations outside
    shippingApi.createShipment(order);
    emailService.sendConfirmation(order);
    paymentGateway.charge(order);
}
```

#### 2. Transaction at Service Layer

```java
// ✅ Good: Transaction at service layer
@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    
    public Order createOrder(OrderRequest request) {
        // Business logic with transaction
    }
}

// ❌ Bad: Transaction at controller layer
@RestController
@Transactional  // Don't do this
public class OrderController {
    // Controllers shouldn't manage transactions
}
```

#### 3. Avoid Nested Transactions Unnecessarily

```java
// ❌ Bad: Unnecessary nesting
@Service
@Transactional
public class ServiceA {
    @Autowired
    private ServiceB serviceB;
    
    public void methodA() {
        // Transaction started
        serviceB.methodB();  // Another transaction?
    }
}

@Service
@Transactional
public class ServiceB {
    public void methodB() {
        // Uses REQUIRED by default, joins parent transaction
        // Good! But understand propagation
    }
}
```

#### 4. Handle Exceptions Properly

```java
// ✅ Good: Clear exception handling
@Transactional(rollbackFor = Exception.class)
public Order createOrder(OrderRequest request) throws OrderException {
    try {
        return processOrder(request);
    } catch (InventoryException e) {
        // Log and rethrow - transaction rolls back
        log.error("Inventory error", e);
        throw new OrderException("Cannot create order", e);
    } catch (Exception e) {
        // Catch all other exceptions
        log.error("Unexpected error", e);
        throw e;
    }
}

// ❌ Bad: Swallowing exceptions
@Transactional
public void createOrder(OrderRequest request) {
    try {
        processOrder(request);
    } catch (Exception e) {
        // Transaction commits! Data may be inconsistent
        log.error("Error occurred", e);
        // Should rethrow or mark for rollback
    }
}
```

### Common Pitfalls and Solutions

#### Pitfall 1: @Transactional on Private Methods

```java
// ❌ Won't work: @Transactional on private method
@Service
public class OrderService {
    @Transactional
    private void createOrder() {
        // Transaction won't start - Spring AOP can't proxy private methods
    }
}

// ✅ Solution: Make it public or protected
@Service
public class OrderService {
    @Transactional
    public void createOrder() {
        // Works! Transaction starts
    }
}
```

#### Pitfall 2: Self-Invocation

```java
// ❌ Won't work: Calling transactional method from same class
@Service
public class OrderService {
    
    public void publicMethod() {
        // This calls the actual method, not the proxy
        this.transactionalMethod();  // Transaction won't start!
    }
    
    @Transactional
    public void transactionalMethod() {
        // No transaction here when called via self-invocation
    }
}

// ✅ Solution 1: Inject self
@Service
public class OrderService {
    @Autowired
    private OrderService self;
    
    public void publicMethod() {
        self.transactionalMethod();  // Goes through proxy, transaction starts
    }
    
    @Transactional
    public void transactionalMethod() {
        // Transaction works
    }
}

// ✅ Solution 2: Refactor to separate service
@Service
public class OrderService {
    @Autowired
    private OrderTransactionService transactionService;
    
    public void publicMethod() {
        transactionService.transactionalMethod();  // Works!
    }
}

@Service
class OrderTransactionService {
    @Transactional
    public void transactionalMethod() {
        // Transaction works
    }
}
```

#### Pitfall 3: Wrong Exception Type

```java
// ❌ Problem: Checked exception doesn't rollback by default
@Transactional
public void processOrder() throws Exception {
    saveOrder();
    if (error) {
        throw new Exception();  // Transaction commits!
    }
}

// ✅ Solution: Use rollbackFor
@Transactional(rollbackFor = Exception.class)
public void processOrder() throws Exception {
    saveOrder();
    if (error) {
        throw new Exception();  // Transaction rolls back
    }
}
```

#### Pitfall 4: LazyInitializationException

```java
// ❌ Problem: Accessing lazy relationship outside transaction
@Transactional(readOnly = true)
public Order getOrder(Long id) {
    return orderRepository.findById(id).get();
}

public void displayOrder(Long id) {
    Order order = getOrder(id);  // Transaction ended
    order.getItems().size();  // LazyInitializationException!
}

// ✅ Solution 1: Fetch within transaction
@Transactional(readOnly = true)
public Order getOrderWithItems(Long id) {
    Order order = orderRepository.findById(id).get();
    order.getItems().size();  // Force initialization
    return order;
}

// ✅ Solution 2: Use JOIN FETCH
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
Order findByIdWithItems(@Param("id") Long id);

// ✅ Solution 3: Use DTO
@Transactional(readOnly = true)
public OrderDTO getOrderDetails(Long id) {
    Order order = orderRepository.findById(id).get();
    return new OrderDTO(order);  // DTO fetches everything needed
}
```

### Testing Transactional Code

```java
@SpringBootTest
@Transactional  // Each test runs in transaction and rolls back
class OrderServiceTest {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Test
    void testCreateOrder_Success() {
        // Arrange
        OrderRequest request = new OrderRequest();
        request.setProductId(1L);
        request.setQuantity(2);
        
        // Act
        Order order = orderService.createOrder(request);
        
        // Assert
        assertNotNull(order.getId());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        
        // Transaction rolls back after test
    }
    
    @Test
    void testCreateOrder_Rollback() {
        // Arrange
        OrderRequest request = new OrderRequest();
        request.setQuantity(999999);  // Not enough inventory
        
        // Act & Assert
        assertThrows(InsufficientInventoryException.class, () -> {
            orderService.createOrder(request);
        });
        
        // Verify rollback - no order saved
        assertEquals(0, orderRepository.count());
    }
    
    @Test
    @Commit  // Don't rollback this test
    void testCreateOrder_Persist() {
        OrderRequest request = new OrderRequest();
        Order order = orderService.createOrder(request);
        
        // This data persists after test
    }
}
```

### Programmatic Transactions

While `@Transactional` is the recommended approach, Spring also supports programmatic transaction management for fine-grained control.

#### Using TransactionTemplate

```java
@Service
public class OrderService {
    
    @Autowired
    private PlatformTransactionManager transactionManager;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private InventoryService inventoryService;
    
    public Order createOrderProgrammatic(OrderRequest request) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        
        // Configure transaction properties
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        template.setTimeout(30);
        
        return template.execute(status -> {
            try {
                // Transaction logic
                Order order = new Order(request);
                order = orderRepository.save(order);
                
                inventoryService.decreaseStock(
                    request.getProductId(),
                    request.getQuantity()
                );
                
                return order;
            } catch (Exception e) {
                // Manually mark for rollback
                status.setRollbackOnly();
                throw e;
            }
        });
    }
    
    /**
     * Read-only programmatic transaction
     */
    public List<Order> getOrdersProgrammatic() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setReadOnly(true);
        
        return template.execute(status -> {
            return orderRepository.findAll();
        });
    }
}
```

#### Using PlatformTransactionManager Directly

```java
@Service
public class PaymentService {
    
    @Autowired
    private PlatformTransactionManager transactionManager;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    public void processPaymentManual(PaymentRequest request) {
        // Define transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
        def.setTimeout(30);
        
        // Start transaction
        TransactionStatus status = transactionManager.getTransaction(def);
        
        try {
            // Execute business logic
            Payment payment = new Payment(request);
            paymentRepository.save(payment);
            
            // More operations...
            
            // Commit transaction
            transactionManager.commit(status);
            
        } catch (Exception e) {
            // Rollback on error
            transactionManager.rollback(status);
            throw e;
        }
    }
}
```

#### When to Use Programmatic Transactions

**Use programmatic transactions when:**
1. You need conditional transaction logic
2. Managing multiple transactions in single method
3. Need fine-grained control over transaction boundaries
4. Working with legacy code

```java
@Service
public class DataMigrationService {
    
    @Autowired
    private TransactionTemplate transactionTemplate;
    
    /**
     * Conditional transaction based on runtime logic
     */
    public void migrateData(List<DataRecord> records) {
        for (DataRecord record : records) {
            if (record.needsTransaction()) {
                // Execute in transaction
                transactionTemplate.execute(status -> {
                    processRecord(record);
                    return null;
                });
            } else {
                // Execute without transaction
                processRecord(record);
            }
        }
    }
    
    /**
     * Multiple independent transactions in same method
     */
    public void processInBatches(List<Order> orders) {
        int batchSize = 100;
        
        for (int i = 0; i < orders.size(); i += batchSize) {
            List<Order> batch = orders.subList(i, 
                Math.min(i + batchSize, orders.size()));
            
            // Each batch in separate transaction
            transactionTemplate.execute(status -> {
                batch.forEach(order -> orderRepository.save(order));
                return null;
            });
        }
    }
}
```

### Transaction Synchronization

Spring provides callbacks for transaction lifecycle events:

```java
@Service
public class OrderService {
    
    @Transactional
    public void createOrder(OrderRequest request) {
        Order order = orderRepository.save(new Order(request));
        
        // Register callback after transaction commits
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // Executed only if transaction commits successfully
                    sendOrderConfirmationEmail(order);
                    clearCache();
                }
                
                @Override
                public void afterCompletion(int status) {
                    // Executed after transaction completes (commit or rollback)
                    if (status == STATUS_COMMITTED) {
                        log.info("Transaction committed");
                    } else if (status == STATUS_ROLLED_BACK) {
                        log.info("Transaction rolled back");
                    }
                }
            }
        );
    }
}
```

### Transaction Events (Spring 4.2+)

More elegant way to handle post-transaction actions:

```java
@Component
public class OrderEventListener {
    
    @Autowired
    private EmailService emailService;
    
    // Executes after transaction commits
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        emailService.sendOrderConfirmation(event.getOrder());
    }
    
    // Executes after transaction rolls back
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleOrderFailed(OrderCreatedEvent event) {
        log.error("Order creation failed: {}", event.getOrder().getId());
    }
    
    // Executes before transaction commits
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void beforeCommit(OrderCreatedEvent event) {
        // Final validation before commit
        validateOrder(event.getOrder());
    }
    
    // Executes after transaction completes (commit or rollback)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void afterCompletion(OrderCreatedEvent event) {
        // Cleanup or metrics
        recordMetrics(event);
    }
}

@Service
public class OrderService {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public Order createOrder(OrderRequest request) {
        Order order = orderRepository.save(new Order(request));
        
        // Publish event - listeners execute based on transaction phase
        eventPublisher.publishEvent(new OrderCreatedEvent(order));
        
        return order;
    }
}
```

### Advanced Transaction Configuration

#### Custom Transaction Manager

```java
@Configuration
@EnableTransactionManagement
public class TransactionConfig {
    
    @Bean
    public PlatformTransactionManager transactionManager(
            EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        
        // Custom settings
        transactionManager.setDefaultTimeout(30);
        transactionManager.setRollbackOnCommitFailure(true);
        
        return transactionManager;
    }
}
```

#### Multiple Transaction Managers

```java
@Configuration
@EnableTransactionManagement
public class MultipleDbConfig {
    
    @Bean
    @Primary
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
    
    @Bean
    public PlatformTransactionManager secondaryTransactionManager(
            @Qualifier("secondaryEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}

@Service
public class MultiDbService {
    
    @Transactional("primaryTransactionManager")
    public void saveToPrimary() {
        // Uses primary database
    }
    
    @Transactional("secondaryTransactionManager")
    public void saveToSecondary() {
        // Uses secondary database
    }
}
```

### Distributed Transactions (JTA)

For transactions spanning multiple resources (databases, message queues):

```java
@Configuration
@EnableTransactionManagement
public class JtaConfig {
    
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new JtaTransactionManager();
    }
}

@Service
public class DistributedTransactionService {
    
    @Autowired
    private OrderRepository orderRepository;  // Database 1
    
    @Autowired
    private InventoryRepository inventoryRepository;  // Database 2
    
    @Autowired
    private JmsTemplate jmsTemplate;  // Message Queue
    
    @Transactional  // JTA manages all resources
    public void createOrderDistributed(OrderRequest request) {
        // All operations in distributed transaction
        orderRepository.save(new Order(request));
        inventoryRepository.decreaseStock(request.getProductId());
        jmsTemplate.send("order-queue", new OrderMessage(request));
        
        // All commit together or all rollback
    }
}
```

### Transaction Performance Monitoring

```java
@Aspect
@Component
public class TransactionMonitoringAspect {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionMonitoringAspect.class);
    
    @Around("@annotation(transactional)")
    public Object monitorTransaction(ProceedingJoinPoint joinPoint, 
                                    Transactional transactional) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();
        
        log.info("Transaction started: {}", methodName);
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("Transaction committed: {} ({}ms)", methodName, duration);
            
            // Alert if transaction is slow
            if (duration > 5000) {
                log.warn("Slow transaction detected: {} ({}ms)", methodName, duration);
            }
            
            return result;
        } catch (Exception e) {
            log.error("Transaction rolled back: {}", methodName, e);
            throw e;
        }
    }
}
```

### Summary: @Transactional vs Programmatic

| Aspect | @Transactional | Programmatic |
|--------|---------------|--------------|
| **Simplicity** | ✅ Very simple | ❌ More verbose |
| **Readability** | ✅ Clean code | ❌ More boilerplate |
| **Flexibility** | ❌ Less flexible | ✅ Fine-grained control |
| **Testing** | ✅ Easy to test | ❌ Harder to test |
| **Maintenance** | ✅ Easy | ❌ More complex |
| **Use Case** | Most scenarios | Complex logic, conditional transactions |

**Recommendation**: Use `@Transactional` for 99% of cases. Use programmatic transactions only when you need fine-grained control or conditional transaction logic.

## Best Practices

### 1. Use Projections

```java
// Interface-based projection
public interface UserSummary {
    String getFirstName();
    String getLastName();
    String getEmail();
}

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<UserSummary> findBy();
}

// Class-based projection (DTO)
public class UserDTO {
    private String firstName;
    private String lastName;
    private String email;
    
    public UserDTO(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    // Getters
}

@Query("SELECT new com.example.dto.UserDTO(u.firstName, u.lastName, u.email) " +
       "FROM User u WHERE u.active = true")
List<UserDTO> findActiveUsersDTO();
```

### 2. Avoid N+1 Queries

```java
// Bad: N+1 query problem
@Query("SELECT u FROM User u")
List<User> findAll();  // Then accessing u.getOrders() causes N queries

// Good: Use JOIN FETCH
@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders")
List<User> findAllWithOrders();

// Or use @EntityGraph
@EntityGraph(attributePaths = {"orders", "profile"})
List<User> findAll();
```

### 3. Use Appropriate Fetch Types

```java
@Entity
public class User {
    // LAZY loading (default for collections)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<Order> orders;
    
    // EAGER loading (use sparingly)
    @ManyToOne(fetch = FetchType.EAGER)
    private Company company;
}
```

### 4. Handle Optimistic Locking

```java
@Entity
public class Product {
    @Id
    private Long id;
    
    @Version
    private Integer version;
    
    private String name;
    private Integer stock;
    
    // Getters, setters
}
```

## Exercises

### Exercise 1: Create Entity Model
Design and implement entities for a library system:
- Book (title, isbn, author, publishedDate)
- Author (name, bio)
- Member (name, email, membershipDate)
- Loan (book, member, loanDate, returnDate)

### Exercise 2: Repository Methods
Create custom repository methods:
- Find books by author name
- Find overdue loans
- Count books by publication year
- Find most active members

### Exercise 3: Complex Queries
Write JPQL queries for:
- Books never loaned
- Members with most loans
- Average loan duration
- Books due for return this week

### Exercise 4: Transactions
Implement a book loan system with transactions:
- Loan book (decrease availability)
- Return book (increase availability)
- Handle concurrent loans

### Exercise 5: Performance Optimization
Optimize queries:
- Implement pagination
- Add projections
- Fix N+1 queries
- Add appropriate indexes

## Key Takeaways

✅ JPA simplifies database operations with object mapping  
✅ Spring Data JPA provides repositories with zero implementation  
✅ Query methods follow naming conventions  
✅ Use @Query for complex queries  
✅ Manage relationships with annotations  
✅ @Transactional ensures data consistency  
✅ Optimize with projections and fetch strategies  

## Resources

### Official Documentation
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Hibernate Documentation](https://hibernate.org/orm/documentation/)
- [JPA Specification](https://jakarta.ee/specifications/persistence/)

### Guides
- [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
- [JPA Query Methods](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods)

## Next Steps

Tomorrow, we'll learn about **Exception Handling and Validation** to make our applications more robust!

---

[<< Previous: Day 05](../Day05_REST_APIs/README.md) | [Back to Main](../README.md) | [Next: Day 07 >>](../Day07_Exception_Validation/README.md)
