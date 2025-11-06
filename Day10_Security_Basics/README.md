# Day 10: Spring Security Basics

## 📋 Table of Contents
- [Introduction](#introduction)
- [What is Spring Security?](#what-is-spring-security)
- [Core Concepts](#core-concepts)
- [Getting Started](#getting-started)
- [Authentication vs Authorization](#authentication-vs-authorization)
- [Spring Security Architecture](#spring-security-architecture)
- [In-Memory Authentication](#in-memory-authentication)
- [Database Authentication](#database-authentication)
- [Password Encoding](#password-encoding)
- [Method-Level Security](#method-level-security)
- [CSRF Protection](#csrf-protection)
- [CORS Configuration](#cors-configuration)
- [Security Best Practices](#security-best-practices)
- [Complete Example](#complete-example)
- [Exercises](#exercises)
- [Key Takeaways](#key-takeaways)
- [Resources](#resources)

## Introduction

Welcome to Day 10! Today we'll explore **Spring Security**, the de facto standard for securing Spring-based applications.

### What You'll Learn
- Understanding authentication and authorization
- Spring Security architecture and components
- Implementing in-memory and database authentication
- Password encoding with BCrypt
- Method-level security
- CSRF and CORS configuration
- Security best practices

### Why Security Matters
- ✅ **Protect Sensitive Data**: Prevent unauthorized access
- ✅ **User Authentication**: Verify user identity
- ✅ **Authorization**: Control what users can do
- ✅ **Industry Standard**: Spring Security is widely adopted
- ✅ **Comprehensive**: Handles common security concerns

## What is Spring Security?

Spring Security is a powerful and highly customizable authentication and access-control framework. It provides:

- **Authentication** - Who are you?
- **Authorization** - What can you do?
- **Protection** - Against common attacks (CSRF, XSS, etc.)
- **Integration** - Works with various authentication mechanisms

### Key Features

```
┌─────────────────────────────────────┐
│    Spring Security Framework        │
├─────────────────────────────────────┤
│  Authentication                     │
│  ├── In-Memory                     │
│  ├── Database (JDBC/JPA)           │
│  ├── LDAP                           │
│  └── OAuth2/SAML                    │
├─────────────────────────────────────┤
│  Authorization                      │
│  ├── Role-Based (RBAC)             │
│  ├── Permission-Based              │
│  └── Expression-Based              │
├─────────────────────────────────────┤
│  Protection                         │
│  ├── CSRF Protection               │
│  ├── Session Management            │
│  ├── Password Encoding             │
│  └── Security Headers              │
└─────────────────────────────────────┘
```

## Core Concepts

### 1. Principal
The currently logged-in user or entity.

### 2. Authentication
Process of verifying the identity of a user.

### 3. Authorization
Process of determining what an authenticated user is allowed to do.

### 4. GrantedAuthority
Represents an authority/permission granted to the authenticated user (usually roles).

### 5. SecurityContext
Holds authentication information for the current execution thread.

### 6. SecurityContextHolder
Provides access to the SecurityContext.

## Getting Started

### 1. Add Spring Security Dependency

**Maven (pom.xml):**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**Gradle (build.gradle):**
```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
```

### 2. Default Behavior

When you add Spring Security, it automatically:
- Protects all endpoints
- Generates a default user with username `user`
- Generates a random password (printed in console logs)
- Enables HTTP Basic authentication
- Enables form-based login

**Console Output:**
```
Using generated security password: 8e557245-73e2-4286-969a-ff57fe326336
```

### 3. Test Default Security

Start your application and try to access any endpoint:
```
GET http://localhost:8080/api/users
```

You'll be redirected to `/login` or prompted for credentials.

## Authentication vs Authorization

### Authentication (Who are you?)

**Authentication** verifies the identity of a user.

```java
// Example: User provides credentials
Username: john@example.com
Password: mypassword123

// System verifies credentials
if (validateCredentials(username, password)) {
    // User is authenticated
    createSecurityContext(user);
}
```

### Authorization (What can you do?)

**Authorization** determines what an authenticated user can access.

```java
// User is authenticated as "john" with role "USER"
if (hasRole("ADMIN")) {
    // Can access admin functions
} else if (hasRole("USER")) {
    // Can access regular user functions
}
```

### Example Scenario

```
User: Alice
├── Authentication: Login with username/password ✓
└── Authorization:
    ├── Can view products? → YES (PUBLIC)
    ├── Can add products? → NO (needs ADMIN role)
    ├── Can edit own profile? → YES (authenticated)
    └── Can delete users? → NO (needs ADMIN role)
```

## Spring Security Architecture

### Security Filter Chain

Spring Security uses a chain of filters to process requests:

```
HTTP Request
    ↓
┌─────────────────────────┐
│  Security Filter Chain  │
├─────────────────────────┤
│  1. CSRF Filter         │ → Checks CSRF token
│  2. Authentication      │ → Authenticates user
│  3. Authorization       │ → Checks permissions
│  4. Exception Handler   │ → Handles security errors
└─────────────────────────┘
    ↓
Your Controller
```

### Core Components

**1. AuthenticationManager**
- Coordinates authentication process
- Delegates to AuthenticationProvider

**2. AuthenticationProvider**
- Performs actual authentication
- Validates credentials

**3. UserDetailsService**
- Loads user-specific data
- Used by AuthenticationProvider

**4. PasswordEncoder**
- Encodes and matches passwords
- BCrypt is recommended

## In-Memory Authentication

### Simple Configuration

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
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            .logout(logout -> logout
                .permitAll()
            );
        
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

### Understanding the Configuration

**1. authorizeHttpRequests:**
```java
.requestMatchers("/public/**").permitAll()  // No auth required
.requestMatchers("/admin/**").hasRole("ADMIN")  // Needs ADMIN role
.requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")  // Needs USER or ADMIN
.anyRequest().authenticated()  // All other requests need authentication
```

**2. formLogin:**
```java
.formLogin(form -> form
    .loginPage("/login")  // Custom login page
    .defaultSuccessUrl("/home")  // Redirect after successful login
    .failureUrl("/login?error=true")  // Redirect after failed login
    .permitAll()
)
```

**3. logout:**
```java
.logout(logout -> logout
    .logoutUrl("/logout")  // Logout URL
    .logoutSuccessUrl("/login?logout=true")  // Redirect after logout
    .invalidateHttpSession(true)  // Invalidate session
    .deleteCookies("JSESSIONID")  // Delete cookies
    .permitAll()
)
```

### HTTP Basic Authentication

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(authorize -> authorize
            .anyRequest().authenticated()
        )
        .httpBasic();  // Enable HTTP Basic authentication
    
    return http.build();
}
```

**Test with cURL:**
```bash
curl -u user:password http://localhost:8080/api/users
```

## Database Authentication

### 1. Entity Model

```java
package com.example.security.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String email;
    
    private boolean enabled = true;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}
```

```java
package com.example.security.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "roles")
@Data
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;  // e.g., "ROLE_USER", "ROLE_ADMIN"
}
```

### 2. Repository

```java
package com.example.security.repository;

import com.example.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
```

### 3. Custom UserDetails

```java
package com.example.security.service;

import com.example.security.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {
    
    private final User user;
    
    public CustomUserDetails(User user) {
        this.user = user;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    @Override
    public String getUsername() {
        return user.getUsername();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
    
    // Additional methods to access user data
    public Long getId() {
        return user.getId();
    }
    
    public String getEmail() {
        return user.getEmail();
    }
}
```

### 4. UserDetailsService Implementation

```java
package com.example.security.service;

import com.example.security.model.User;
import com.example.security.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
        
        return new CustomUserDetails(user);
    }
}
```

### 5. Security Configuration with Database

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
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home")
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
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

Never store passwords in plain text!

**Bad:**
```java
user.setPassword("mypassword123");  // ❌ Plain text
```

**Good:**
```java
user.setPassword(passwordEncoder.encode("mypassword123"));  // ✅ Encoded
```

### BCrypt Password Encoder

BCrypt is the recommended password encoder:
- **Adaptive**: Can increase rounds as computers get faster
- **Salted**: Each password gets a unique salt
- **One-way**: Cannot be reversed

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### Encoding Passwords

```java
package com.example.security.service;

import com.example.security.model.User;
import com.example.security.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public User registerUser(String username, String password, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));  // Encode password
        user.setEmail(email);
        user.setEnabled(true);
        
        return userRepository.save(user);
    }
    
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow();
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        // Update to new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
}
```

### Password Strength

```java
package com.example.security.util;

public class PasswordValidator {
    
    public static boolean isStrongPassword(String password) {
        // At least 8 characters
        if (password.length() < 8) {
            return false;
        }
        
        // Contains uppercase
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        
        // Contains lowercase
        if (!password.matches(".*[a-z].*")) {
            return false;
        }
        
        // Contains digit
        if (!password.matches(".*\\d.*")) {
            return false;
        }
        
        // Contains special character
        if (!password.matches(".*[!@#$%^&*()].*")) {
            return false;
        }
        
        return true;
    }
}
```

## Method-Level Security

### Enable Method Security

```java
package com.example.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(
    prePostEnabled = true,  // Enable @PreAuthorize and @PostAuthorize
    securedEnabled = true,  // Enable @Secured
    jsr250Enabled = true    // Enable @RolesAllowed
)
public class MethodSecurityConfig {
}
```

### @PreAuthorize

Check permissions before method execution:

```java
package com.example.security.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    
    // Only users with ADMIN role can execute
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(Long productId) {
        // Delete product logic
    }
    
    // Only users with USER or ADMIN role
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<String> getProducts() {
        // Get products logic
        return List.of("Product1", "Product2");
    }
    
    // Only the owner can update (using SpEL)
    @PreAuthorize("#userId == authentication.principal.id")
    public void updateUserProfile(Long userId, String name) {
        // Update profile logic
    }
    
    // Complex expression
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #userId == authentication.principal.id)")
    public void updateUser(Long userId, User user) {
        // Update user logic
    }
}
```

### @PostAuthorize

Check permissions after method execution:

```java
@Service
public class OrderService {
    
    // Only return if user owns the order
    @PostAuthorize("returnObject.userId == authentication.principal.id")
    public Order getOrder(Long orderId) {
        // Get order logic
        return orderRepository.findById(orderId).orElseThrow();
    }
}
```

### @Secured

Simple role-based security:

```java
@Service
public class ReportService {
    
    @Secured("ROLE_ADMIN")
    public void generateReport() {
        // Generate report logic
    }
    
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public void viewReport() {
        // View report logic
    }
}
```

### @RolesAllowed (JSR-250)

Java standard annotation:

```java
import javax.annotation.security.RolesAllowed;

@Service
public class AdminService {
    
    @RolesAllowed("ADMIN")
    public void performAdminTask() {
        // Admin task logic
    }
}
```

### Getting Current User

```java
package com.example.security.controller;

import com.example.security.service.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    
    @GetMapping("/api/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return new UserResponse(
            userDetails.getId(),
            userDetails.getUsername(),
            userDetails.getEmail()
        );
    }
}
```

## CSRF Protection

### What is CSRF?

Cross-Site Request Forgery (CSRF) is an attack that tricks users into executing unwanted actions.

### CSRF Protection in Spring Security

**Enabled by default for:**
- State-changing operations (POST, PUT, DELETE, PATCH)
- Form-based login

**Not needed for:**
- Stateless REST APIs (using JWT)
- GET requests (should be idempotent)

### Disable CSRF (for REST APIs)

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())  // Disable CSRF
        .authorizeHttpRequests(authorize -> authorize
            .anyRequest().authenticated()
        )
        .httpBasic();
    
    return http.build();
}
```

### Configure CSRF

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf
            .ignoringRequestMatchers("/api/public/**")  // Ignore CSRF for these paths
        )
        .authorizeHttpRequests(authorize -> authorize
            .anyRequest().authenticated()
        );
    
    return http.build();
}
```

### Using CSRF Token in Thymeleaf

```html
<form method="post" action="/submit">
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
    <input type="text" name="data"/>
    <button type="submit">Submit</button>
</form>
```

## CORS Configuration

### Global CORS Configuration

```java
package com.example.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://app.example.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### CORS in Security Configuration

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(authorize -> authorize
            .anyRequest().authenticated()
        );
    
    return http.build();
}
```

## Security Best Practices

### 1. Always Encode Passwords

```java
// ❌ Never do this
user.setPassword("plaintext");

// ✅ Always do this
user.setPassword(passwordEncoder.encode("password"));
```

### 2. Use HTTPS in Production

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
```

### 3. Implement Account Lockout

```java
@Entity
public class User {
    private int failedLoginAttempts = 0;
    private boolean accountLocked = false;
    private LocalDateTime lockTime;
}

public void handleFailedLogin(String username) {
    User user = userRepository.findByUsername(username).orElseThrow();
    user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
    
    if (user.getFailedLoginAttempts() >= 3) {
        user.setAccountLocked(true);
        user.setLockTime(LocalDateTime.now());
    }
    
    userRepository.save(user);
}
```

### 4. Implement Session Management

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .maximumSessions(1)  // Only one session per user
            .maxSessionsPreventsLogin(true)  // Prevent new login if max reached
        );
    
    return http.build();
}
```

### 5. Add Security Headers

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .headers(headers -> headers
            .contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'self'")
            )
            .frameOptions(frame -> frame.deny())
            .xssProtection(xss -> xss.block(true))
        );
    
    return http.build();
}
```

### 6. Implement Rate Limiting

```java
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final Map<String, List<Long>> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 100;
    private static final long TIME_WINDOW = 60000; // 1 minute
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientId = getClientId(request);
        
        if (isRateLimited(clientId)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isRateLimited(String clientId) {
        long currentTime = System.currentTimeMillis();
        requestCounts.putIfAbsent(clientId, new ArrayList<>());
        
        List<Long> timestamps = requestCounts.get(clientId);
        timestamps.removeIf(time -> currentTime - time > TIME_WINDOW);
        
        if (timestamps.size() >= MAX_REQUESTS) {
            return true;
        }
        
        timestamps.add(currentTime);
        return false;
    }
    
    private String getClientId(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
```

## Complete Example

### Project Structure

```
src/main/java/com/example/security/
├── SecurityApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── CorsConfig.java
├── model/
│   ├── User.java
│   └── Role.java
├── repository/
│   ├── UserRepository.java
│   └── RoleRepository.java
├── service/
│   ├── UserService.java
│   └── CustomUserDetailsService.java
├── controller/
│   ├── AuthController.java
│   └── UserController.java
└── dto/
    ├── LoginRequest.java
    └── UserResponse.java
```

### Auth Controller

```java
package com.example.security.controller;

import com.example.security.dto.LoginRequest;
import com.example.security.dto.UserResponse;
import com.example.security.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    
    public AuthController(AuthenticationManager authenticationManager, 
                         UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        UserResponse user = userService.registerUser(
            request.getUsername(),
            request.getPassword(),
            request.getEmail()
        );
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        // In a real app, you'd generate and return a JWT token here
        return ResponseEntity.ok("Login successful");
    }
}
```

### Application Configuration

**application.yml:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/security_db
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  security:
    user:
      name: admin
      password: admin123

logging:
  level:
    org.springframework.security: DEBUG
```

## Exercises

### Exercise 1: Basic Security Setup
Create a Spring Boot application with in-memory authentication.

**Requirements:**
1. Add Spring Security dependency
2. Configure two users: user (role USER) and admin (role ADMIN)
3. Protect /api/admin/** with ADMIN role
4. Protect /api/user/** with USER role
5. Allow public access to /api/public/**

### Exercise 2: Database Authentication
Implement database authentication with User and Role entities.

**Requirements:**
1. Create User and Role entities with many-to-many relationship
2. Implement UserRepository
3. Create CustomUserDetailsService
4. Configure Spring Security to use database authentication
5. Create registration endpoint

### Exercise 3: Method Security
Add method-level security to a service class.

**Requirements:**
1. Enable method security
2. Create ProductService with CRUD methods
3. Use @PreAuthorize to protect methods
4. Only ADMIN can delete products
5. Users can only update their own products

### Exercise 4: Password Management
Implement secure password management.

**Requirements:**
1. Create password change functionality
2. Validate old password before changing
3. Enforce password strength rules
4. Encode passwords with BCrypt
5. Add forgot password functionality

### Exercise 5: Production Security
Configure security for production deployment.

**Requirements:**
1. Enable HTTPS
2. Configure CORS for specific origins
3. Add security headers
4. Implement session management
5. Add rate limiting for login endpoint

## Key Takeaways

1. **Spring Security is Comprehensive**
   - Handles authentication and authorization
   - Protects against common attacks
   - Highly customizable

2. **Authentication vs Authorization**
   - Authentication: Who are you?
   - Authorization: What can you do?
   - Both are essential for security

3. **Never Store Plain Text Passwords**
   - Always use password encoders
   - BCrypt is the recommended choice
   - Salt and hash are automatic

4. **Method-Level Security**
   - @PreAuthorize for pre-execution checks
   - @PostAuthorize for post-execution checks
   - Use SpEL for complex expressions

5. **CSRF and CORS**
   - CSRF protection for state-changing operations
   - CORS configuration for cross-origin requests
   - Disable CSRF for stateless REST APIs

6. **Security Best Practices**
   - Use HTTPS in production
   - Implement account lockout
   - Add security headers
   - Use session management
   - Implement rate limiting

## Resources

### Official Documentation
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Spring Security Architecture](https://spring.io/guides/topicals/spring-security-architecture/)
- [Spring Security Samples](https://github.com/spring-projects/spring-security-samples)

### Tutorials
- [Spring Security Tutorial](https://www.baeldung.com/security-spring)
- [Spring Security in Action](https://www.manning.com/books/spring-security-in-action)
- [Spring Security with JWT](https://www.baeldung.com/spring-security-oauth-jwt)

### Tools
- [BCrypt Calculator](https://bcrypt-generator.com/)
- [OWASP Security Guidelines](https://owasp.org/)
- [Spring Security OAuth](https://spring.io/projects/spring-security-oauth)

---
[<< Previous: Day 09](../Day09_Actuator_Monitoring/README.md) | [Back to Main](../README.md) | [Next: Day 11 >>](../Day11_JWT_Auth/README.md)
