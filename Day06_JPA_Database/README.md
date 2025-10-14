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

### @Transactional Annotation

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
    
    @Transactional(readOnly = true)
    public List<Order> getOrders() {
        // Read-only transaction (optimization)
        return orderRepository.findAll();
    }
    
    @Transactional(
        propagation = Propagation.REQUIRES_NEW,
        isolation = Isolation.SERIALIZABLE,
        timeout = 30,
        rollbackFor = Exception.class
    )
    public void complexTransaction() {
        // Custom transaction settings
    }
}
```

### Programmatic Transactions

```java
@Service
public class OrderService {
    
    @Autowired
    private PlatformTransactionManager transactionManager;
    
    public void createOrderProgrammatic(OrderRequest request) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        
        template.execute(status -> {
            try {
                // Transaction logic
                Order order = new Order(request);
                orderRepository.save(order);
                
                inventoryService.decreaseStock(
                    request.getProductId(),
                    request.getQuantity()
                );
                
                return order;
            } catch (Exception e) {
                status.setRollbackOnly();
                throw e;
            }
        });
    }
}
```

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
