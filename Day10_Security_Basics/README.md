# Day 10: Spring Security Basics

## 📋 Table of Contents
- [Introduction](#introduction)
- [Security Fundamentals](#security-fundamentals)
- [Spring Security Architecture](#spring-security-architecture)
- [Getting Started](#getting-started)
- [In-Memory Authentication](#in-memory-authentication)
- [Database Authentication](#database-authentication)
- [Password Encoding](#password-encoding)
- [Authorization Configuration](#authorization-configuration)
- [Method-Level Security](#method-level-security)
- [Custom Authentication](#custom-authentication)
- [Security Best Practices](#security-best-practices)
- [Complete Example](#complete-example)
- [Exercises](#exercises)
- [Key Takeaways](#key-takeaways)
- [Resources](#resources)

## Introduction

Welcome to Day 10! Today we'll dive into **Spring Security** - the de facto standard for securing Spring-based applications.

### What You'll Learn
- Core security concepts (Authentication vs Authorization)
- Spring Security architecture and components
- Implementing in-memory and database authentication
- Password encoding with BCrypt
- Configuring authorization rules
- Method-level security
- Custom authentication providers
- Security best practices

### Why Spring Security Matters
- ✅ **Comprehensive**: Covers authentication, authorization, and protection against common attacks
- ✅ **Flexible**: Highly customizable to meet various security requirements
- ✅ **Battle-tested**: Used by thousands of enterprise applications
- ✅ **Active Development**: Regular updates for new security threats
- ✅ **Integration**: Works seamlessly with Spring ecosystem
- ✅ **Standards-based**: Supports OAuth2, SAML, LDAP, and more

## Security Fundamentals

### Authentication vs Authorization

**Authentication** (Who are you?)
- Process of verifying identity
- Answers: "Are you who you claim to be?"
- Examples: Username/password, biometrics, tokens

**Authorization** (What can you do?)
- Process of granting access to resources
- Answers: "What are you allowed to do?"
- Examples: Roles, permissions, access control lists

```
┌────────────────────────────────────────────┐
│           Security Flow                     │
├────────────────────────────────────────────┤
│                                            │
│  1. Authentication                         │
│     ↓                                      │
│     User provides credentials              │
│     ↓                                      │
│     System verifies credentials            │
│     ↓                                      │
│     User is authenticated                  │
│                                            │
│  2. Authorization                          │
│     ↓                                      │
│     User requests resource                 │
│     ↓                                      │
│     System checks permissions              │
│     ↓                                      │
│     Access granted or denied               │
│                                            │
└────────────────────────────────────────────┘
```

### Common Security Threats

1. **Cross-Site Request Forgery (CSRF)**
   - Unauthorized commands from a trusted user
   - Spring Security provides CSRF protection by default

2. **Cross-Site Scripting (XSS)**
   - Injecting malicious scripts
   - Mitigated with proper output encoding

3. **SQL Injection**
   - Malicious SQL code injection
   - Prevented with parameterized queries

4. **Session Fixation**
   - Hijacking user sessions
   - Spring Security handles session management

5. **Brute Force Attacks**
   - Multiple login attempts
   - Can be mitigated with account lockout

## Spring Security Architecture

### Core Components

```
┌──────────────────────────────────────────────────┐
│         Spring Security Architecture              │
├──────────────────────────────────────────────────┤
│                                                  │
│  HTTP Request                                    │
│       ↓                                          │
│  ┌────────────────────────────────────────┐     │
│  │   Security Filter Chain                │     │
│  │  (Multiple security filters)           │     │
│  └────────────────────────────────────────┘     │
│       ↓                                          │
│  ┌────────────────────────────────────────┐     │
│  │   Authentication Manager               │     │
│  │  (Coordinates authentication)          │     │
│  └────────────────────────────────────────┘     │
│       ↓                                          │
│  ┌────────────────────────────────────────┐     │
│  │   Authentication Provider              │     │
│  │  (Performs actual authentication)      │     │
│  └────────────────────────────────────────┘     │
│       ↓                                          │
│  ┌────────────────────────────────────────┐     │
│  │   UserDetailsService                   │     │
│  │  (Loads user-specific data)            │     │
│  └────────────────────────────────────────┘     │
│       ↓                                          │
│  ┌────────────────────────────────────────┐     │
│  │   Security Context                     │     │
│  │  (Stores authentication details)       │     │
│  └────────────────────────────────────────┘     │
│       ↓                                          │
│  Application                                     │
│                                                  │
└──────────────────────────────────────────────────┘
```

### Key Interfaces and Classes

1. **SecurityFilterChain**: Chain of security filters
2. **AuthenticationManager**: Manages authentication process
3. **AuthenticationProvider**: Performs authentication
4. **UserDetailsService**: Loads user data
5. **PasswordEncoder**: Encodes passwords
6. **SecurityContext**: Holds authentication information
7. **GrantedAuthority**: Represents an authority (role/permission)

## Getting Started

### 1. Add Spring Security Dependency

**Maven (pom.xml):**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Optional: For testing security -->
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**Gradle (build.gradle):**
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    testImplementation 'org.springframework.security:spring-security-test'
}
```

### 2. Default Security Behavior

Once you add Spring Security dependency:
- All endpoints are secured by default
- A default user is created
- Username: `user`
- Password: Generated and printed in console logs

```
Using generated security password: 8e557245-73e2-4286-969a-ff57fe326336
```

### 3. Test Default Security

```bash
# Without credentials - 401 Unauthorized
curl http://localhost:8080/api/products

# With credentials - Success
curl -u user:8e557245-73e2-4286-969a-ff57fe326336 \
  http://localhost:8080/api/products
```

## In-Memory Authentication

### Basic Configuration

**SecurityConfig.java:**
```java
package com.example.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()  // Public endpoints
                .requestMatchers("/admin/**").hasRole("ADMIN")  // Admin only
                .anyRequest().authenticated()  // All other endpoints require authentication
            )
            .formLogin(form -> form
                .loginPage("/login")  // Custom login page
                .permitAll()
            )
            .logout(logout -> logout
                .permitAll()
            )
            .httpBasic();  // Enable HTTP Basic authentication
        
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("ADMIN", "USER")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Multiple Users with Different Roles

```java
@Bean
public UserDetailsService userDetailsService() {
    // Regular user
    UserDetails user = User.builder()
            .username("john")
            .password(passwordEncoder().encode("john123"))
            .roles("USER")
            .build();

    // Manager with multiple roles
    UserDetails manager = User.builder()
            .username("jane")
            .password(passwordEncoder().encode("jane123"))
            .roles("USER", "MANAGER")
            .build();

    // Admin with all roles
    UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123"))
            .roles("USER", "MANAGER", "ADMIN")
            .build();

    // User with authorities (fine-grained permissions)
    UserDetails powerUser = User.builder()
            .username("power")
            .password(passwordEncoder().encode("power123"))
            .authorities("READ", "WRITE", "DELETE")
            .build();

    return new InMemoryUserDetailsManager(user, manager, admin, powerUser);
}
```

## Database Authentication

### 1. Database Schema

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

### 2. Entity Classes

**User.java:**
```java
package com.example.security.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private boolean enabled = true;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
```

**Role.java:**
```java
package com.example.security.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "roles")
@Data
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;  // ROLE_USER, ROLE_ADMIN, etc.
}
```

### 3. Repository

**UserRepository.java:**
```java
package com.example.security.repository;

import com.example.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

### 4. UserDetailsService Implementation

**CustomUserDetailsService.java:**
```java
package com.example.security.service;

import com.example.security.entity.User;
import com.example.security.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .authorities(getAuthorities(user))
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
}
```

### 5. Security Configuration with Database Auth

**DatabaseSecurityConfig.java:**
```java
package com.example.security.config;

import com.example.security.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class DatabaseSecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public DatabaseSecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")  // Disable CSRF for API endpoints
            );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## Password Encoding

### Why Password Encoding?

- ❌ **Never** store plain text passwords
- ✅ **Always** use one-way hashing
- ✅ Use **salt** to prevent rainbow table attacks
- ✅ Use **strong algorithms** (BCrypt, Argon2, SCrypt)

### BCrypt Password Encoder

**Advantages of BCrypt:**
- Adaptive: Can be made slower as hardware improves
- Includes salt automatically
- Industry standard
- Resistant to rainbow table attacks

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// Usage
String rawPassword = "myPassword123";
String encodedPassword = passwordEncoder.encode(rawPassword);

// Verification
boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
```

### Different Password Encoders

```java
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

// BCrypt (recommended for most cases)
@Bean
public PasswordEncoder bcryptEncoder() {
    return new BCryptPasswordEncoder();
}

// Argon2 (most secure, but slower)
@Bean
public PasswordEncoder argon2Encoder() {
    return new Argon2PasswordEncoder(16, 32, 1, 65536, 10);
}

// SCrypt
@Bean
public PasswordEncoder scryptEncoder() {
    return new SCryptPasswordEncoder();
}

// PBKDF2
@Bean
public PasswordEncoder pbkdf2Encoder() {
    return new Pbkdf2PasswordEncoder();
}
```

### Delegating Password Encoder (Multiple Algorithms)

```java
@Bean
public PasswordEncoder passwordEncoder() {
    Map<String, PasswordEncoder> encoders = new HashMap<>();
    encoders.put("bcrypt", new BCryptPasswordEncoder());
    encoders.put("scrypt", new SCryptPasswordEncoder());
    encoders.put("argon2", new Argon2PasswordEncoder());
    
    return new DelegatingPasswordEncoder("bcrypt", encoders);
}

// Passwords are stored with algorithm prefix:
// {bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
// {scrypt}$e0801$8bWJaSu2IKSn9Z9kM+TPXfOc/9bdYSrN1oD9qfVThWEwdRTnO7re7Ei+fUZRJ68k9lTyuTeUp4of4g24hHnazw==$OAOec05+bXxvuu/1qZ6NUR+xQYvYv7BeL1QxwRpY5Pc=
```

### User Registration with Password Encoding

**UserService.java:**
```java
package com.example.security.service;

import com.example.security.dto.RegisterRequest;
import com.example.security.entity.Role;
import com.example.security.entity.User;
import com.example.security.repository.RoleRepository;
import com.example.security.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, 
                      RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);

        // Assign default role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Default role not found"));
        user.setRoles(Set.of(userRole));

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
```

## Authorization Configuration

### URL-Based Authorization

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
        // Public endpoints
        .requestMatchers("/", "/home", "/public/**").permitAll()
        
        // Specific HTTP methods
        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
        
        // Role-based access
        .requestMatchers("/admin/**").hasRole("ADMIN")
        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
        .requestMatchers("/manager/**").hasRole("MANAGER")
        
        // Authority-based access
        .requestMatchers("/api/delete/**").hasAuthority("DELETE_PRIVILEGE")
        .requestMatchers("/api/write/**").hasAnyAuthority("WRITE_PRIVILEGE", "ADMIN")
        
        // Ant patterns
        .requestMatchers("/resources/**", "/static/**").permitAll()
        
        // Regular expressions
        .requestMatchers(RegexRequestMatcher.regexMatcher("/api/users/\\d+")).hasRole("USER")
        
        // All other requests require authentication
        .anyRequest().authenticated()
    );
    
    return http.build();
}
```

### Role Hierarchy

```java
@Bean
public RoleHierarchy roleHierarchy() {
    RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
    String hierarchy = "ROLE_ADMIN > ROLE_MANAGER \n ROLE_MANAGER > ROLE_USER";
    roleHierarchy.setHierarchy(hierarchy);
    return roleHierarchy;
}

// With this hierarchy:
// - ADMIN has all permissions of MANAGER and USER
// - MANAGER has all permissions of USER
// - USER has its own permissions
```

### Custom Access Decision

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/orders/**").access(
            new WebExpressionAuthorizationManager("hasRole('USER') and #oauth2.hasScope('read')")
        )
        .anyRequest().authenticated()
    );
    
    return http.build();
}
```

## Method-Level Security

### Enable Method Security

```java
@Configuration
@EnableMethodSecurity(
    prePostEnabled = true,   // Enable @PreAuthorize and @PostAuthorize
    securedEnabled = true,   // Enable @Secured
    jsr250Enabled = true     // Enable @RolesAllowed
)
public class MethodSecurityConfig {
}
```

### @PreAuthorize and @PostAuthorize

**ProductService.java:**
```java
package com.example.security.service;

import com.example.security.entity.Product;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    // Only users with ADMIN role can access
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(Long id) {
        // Delete logic
    }

    // Multiple conditions with AND
    @PreAuthorize("hasRole('USER') and #username == authentication.name")
    public void updateUserProfile(String username, UserProfile profile) {
        // Update logic
    }

    // Multiple conditions with OR
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<Product> getProducts() {
        // Fetch logic
        return null;
    }

    // Check if user owns the resource
    @PreAuthorize("#product.owner == authentication.name")
    public void updateProduct(Product product) {
        // Update logic
    }

    // Verify return value
    @PostAuthorize("returnObject.owner == authentication.name")
    public Product getProduct(Long id) {
        // Fetch product
        return null;
    }

    // Filter collection before processing
    @PreFilter("filterObject.owner == authentication.name")
    public void deleteProducts(List<Product> products) {
        // Only products owned by current user will be processed
    }

    // Filter returned collection
    @PostFilter("filterObject.owner == authentication.name")
    public List<Product> getAllProducts() {
        // Returns only products owned by current user
        return null;
    }

    // Custom expression
    @PreAuthorize("@productSecurityService.canAccess(#id, authentication)")
    public Product getSecureProduct(Long id) {
        return null;
    }
}
```

### Custom Security Expression

**ProductSecurityService.java:**
```java
package com.example.security.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("productSecurityService")
public class ProductSecurityService {

    private final ProductRepository productRepository;

    public ProductSecurityService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public boolean canAccess(Long productId, Authentication authentication) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return false;
        }
        
        String username = authentication.getName();
        return product.getOwner().equals(username) || 
               authentication.getAuthorities().stream()
                   .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
```

### @Secured Annotation

```java
import org.springframework.security.access.annotation.Secured;

@Service
public class OrderService {

    @Secured("ROLE_ADMIN")
    public void deleteOrder(Long id) {
        // Delete logic
    }

    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public void viewOrder(Long id) {
        // View logic
    }
}
```

### @RolesAllowed Annotation (JSR-250)

```java
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.PermitAll;
import javax.annotation.security.DenyAll;

@Service
public class PaymentService {

    @RolesAllowed("ADMIN")
    public void processRefund(Long orderId) {
        // Refund logic
    }

    @RolesAllowed({"USER", "ADMIN"})
    public void makePayment(Long orderId) {
        // Payment logic
    }

    @PermitAll
    public void getPaymentMethods() {
        // Public method
    }

    @DenyAll
    public void dangerousOperation() {
        // No one can access this
    }
}
```

## Custom Authentication

### Custom Authentication Provider

**CustomAuthenticationProvider.java:**
```java
package com.example.security.provider;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationProvider(UserDetailsService userDetailsService,
                                       PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) 
            throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Custom authentication logic
        if (passwordEncoder.matches(password, userDetails.getPassword())) {
            // Additional custom checks
            if (isUserLocked(username)) {
                throw new BadCredentialsException("Account is locked");
            }
            
            // Successful authentication
            return new UsernamePasswordAuthenticationToken(
                userDetails,
                password,
                userDetails.getAuthorities()
            );
        } else {
            // Failed authentication
            incrementFailedAttempts(username);
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private boolean isUserLocked(String username) {
        // Check if user account is locked
        return false;
    }

    private void incrementFailedAttempts(String username) {
        // Track failed login attempts
    }
}
```

### Getting Current User Information

**SecurityUtils.java:**
```java
package com.example.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        return null;
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.getAuthorities().stream()
                   .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }
}
```

**Usage in Controller:**
```java
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @GetMapping
    public UserProfile getCurrentUserProfile(Authentication authentication) {
        // Method 1: Via parameter injection
        String username = authentication.getName();
        
        // Method 2: Via SecurityContext
        String username2 = SecurityUtils.getCurrentUsername();
        
        // Method 3: Via @AuthenticationPrincipal
        return profileService.getProfile(username);
    }

    @GetMapping("/details")
    public UserProfile getProfileDetails(@AuthenticationPrincipal UserDetails userDetails) {
        // Direct access to UserDetails
        String username = userDetails.getUsername();
        return profileService.getProfile(username);
    }
}
```

## Security Best Practices

### 1. Password Security

```java
// ✅ Use strong password encoder
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);  // Strength: 4-31, default: 10
}

// ✅ Enforce password policy
public boolean isPasswordValid(String password) {
    return password.length() >= 8 &&
           password.matches(".*[A-Z].*") &&  // Uppercase
           password.matches(".*[a-z].*") &&  // Lowercase
           password.matches(".*\\d.*") &&    // Digit
           password.matches(".*[@#$%^&+=].*");  // Special char
}

// ❌ Never log or store plain text passwords
log.info("User password: " + password);  // NEVER DO THIS
```

### 2. Session Management

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .maximumSessions(1)  // Only one session per user
            .maxSessionsPreventsLogin(true)  // Prevent new login if session exists
            .expiredUrl("/login?expired=true")
        );
    
    return http.build();
}
```

### 3. CSRF Protection

```java
// ✅ Enable CSRF for form-based applications
http.csrf(Customizer.withDefaults());

// ✅ Disable CSRF only for stateless REST APIs with token auth
http.csrf(csrf -> csrf.disable());

// ✅ Configure CSRF token repository
http.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
);
```

### 4. CORS Configuration

```java
@Configuration
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://trusted-domain.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### 5. Secure Headers

```java
http.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp
        .policyDirectives("default-src 'self'")
    )
    .frameOptions(frame -> frame.deny())
    .xssProtection(xss -> xss.enable())
    .contentTypeOptions(Customizer.withDefaults())
);
```

## Complete Example

**Complete E-Commerce Security Configuration:**

```java
package com.example.ecommerce.config;

import com.example.ecommerce.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfiguration(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/",
                    "/home",
                    "/login",
                    "/register",
                    "/api/public/**",
                    "/css/**",
                    "/js/**",
                    "/images/**"
                ).permitAll()
                
                // Product browsing - public
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                
                // User endpoints
                .requestMatchers("/api/cart/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/orders/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/profile/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Form login configuration
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform-login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            
            // Logout configuration
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // Session management
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(true)
            )
            
            // Remember me
            .rememberMe(remember -> remember
                .key("uniqueAndSecretKey")
                .tokenValiditySeconds(86400) // 24 hours
            )
            
            // Exception handling
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            )
            
            // CSRF protection (enabled for form-based auth)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**") // Disable for REST API
            )
            
            // Security headers
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss.enable())
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'")
                )
            );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

## Exercises

### Exercise 1: In-Memory Authentication
Create a Spring Boot application with in-memory authentication:
- 3 users: admin, manager, user
- Different roles for each user
- Secure endpoints based on roles

### Exercise 2: Database Authentication
Implement database authentication:
- Create User and Role entities
- Implement UserDetailsService
- Create registration endpoint
- Test authentication flow

### Exercise 3: Method-Level Security
Create a service with method-level security:
- Use @PreAuthorize for access control
- Implement custom security expressions
- Test with different users and roles

### Exercise 4: Password Management
Implement password management features:
- User registration with password validation
- Change password functionality
- Password reset (with email)
- Track failed login attempts

### Exercise 5: Complete Security Setup
Build a complete e-commerce application with:
- Database authentication
- Role-based access control
- Method-level security
- Custom login/logout pages
- Remember me functionality

## Key Takeaways

1. **Spring Security** is the standard for securing Spring applications
2. **Authentication** verifies identity, **Authorization** controls access
3. **Never** store plain text passwords - use BCrypt or Argon2
4. **UserDetailsService** loads user-specific data
5. **SecurityFilterChain** configures security rules
6. **Method-level security** provides fine-grained access control
7. **CSRF protection** is important for form-based applications
8. **Session management** prevents session hijacking
9. **Security headers** protect against common attacks
10. **Database authentication** is standard for production applications

## Resources

### Official Documentation
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/index.html)
- [Spring Security Architecture](https://spring.io/guides/topicals/spring-security-architecture)
- [Spring Boot Security](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.security)

### Tutorials
- [Spring Security Basics](https://www.baeldung.com/spring-security-tutorial)
- [Spring Security Authentication](https://www.baeldung.com/spring-security-authentication-and-registration)
- [Method Security](https://www.baeldung.com/spring-security-method-security)

### Tools
- [BCrypt Calculator](https://bcrypt-generator.com/)
- [OWASP Security Guidelines](https://owasp.org/www-project-top-ten/)
- [JWT Decoder](https://jwt.io/)

---
[<< Previous: Day 09](../Day09_Actuator_Monitoring/README.md) | [Back to Main](../README.md) | [Next: Day 11 >>](../Day11_JWT_Auth/README.md)
