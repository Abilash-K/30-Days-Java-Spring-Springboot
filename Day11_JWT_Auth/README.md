# Day 11: JWT Authentication and Authorization

## 📋 Table of Contents
- [Introduction](#introduction)
- [What is JWT?](#what-is-jwt)
- [JWT Structure](#jwt-structure)
- [JWT vs Session-Based Auth](#jwt-vs-session-based-auth)
- [Getting Started](#getting-started)
- [JWT Token Generation](#jwt-token-generation)
- [JWT Token Validation](#jwt-token-validation)
- [JWT Filter Implementation](#jwt-filter-implementation)
- [Complete Authentication Flow](#complete-authentication-flow)
- [Refresh Tokens](#refresh-tokens)
- [Role-Based Access Control](#role-based-access-control)
- [Token Blacklisting](#token-blacklisting)
- [Security Best Practices](#security-best-practices)
- [OAuth2 Introduction](#oauth2-introduction)
- [Complete Example](#complete-example)
- [Exercises](#exercises)
- [Key Takeaways](#key-takeaways)
- [Resources](#resources)

## Introduction

Welcome to Day 11! Today we'll explore **JWT (JSON Web Tokens)**, a modern approach to stateless authentication perfect for microservices and REST APIs.

### What You'll Learn
- Understanding JWT structure and components
- Implementing JWT token generation and validation
- Creating JWT authentication filters
- Managing refresh tokens
- Role-based access control with JWT
- Security best practices for JWT
- Introduction to OAuth2

### Why JWT Matters
- ✅ **Stateless**: No server-side session storage
- ✅ **Scalable**: Perfect for microservices
- ✅ **Self-Contained**: Contains all necessary information
- ✅ **Cross-Domain**: Works across different domains
- ✅ **Mobile-Friendly**: Ideal for mobile applications

## What is JWT?

JWT (JSON Web Token) is an open standard (RFC 7519) for securely transmitting information between parties as a JSON object.

### Key Characteristics

```
Traditional Session-Based Auth:
User → Login → Session stored in server → Cookie with session ID
      ↓
Every request checks session in database (Stateful)

JWT-Based Auth:
User → Login → JWT token generated → Token sent to client
      ↓
Every request validates token locally (Stateless)
```

### When to Use JWT

**Good for:**
- REST APIs
- Microservices architecture
- Mobile applications
- Single Page Applications (SPAs)
- Cross-domain authentication

**Not ideal for:**
- Traditional web applications with server-side rendering
- Applications requiring immediate logout across all devices
- Short-lived sessions

## JWT Structure

A JWT consists of three parts separated by dots (.):

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c

        Header           .           Payload           .         Signature
```

### 1. Header

Contains the token type and hashing algorithm:

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Encoded (Base64Url):**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9
```

### 2. Payload

Contains the claims (user data and metadata):

```json
{
  "sub": "1234567890",
  "name": "John Doe",
  "email": "john@example.com",
  "roles": ["USER", "ADMIN"],
  "iat": 1516239022,
  "exp": 1516242622
}
```

**Standard Claims:**
- `sub` (subject): User identifier
- `iat` (issued at): Token creation time
- `exp` (expiration): Token expiration time
- `iss` (issuer): Token issuer
- `aud` (audience): Token audience

**Encoded (Base64Url):**
```
eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ
```

### 3. Signature

Ensures token integrity:

```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

**Result:**
```
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

### Complete Token

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9
.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ
.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

## JWT vs Session-Based Auth

### Session-Based Authentication

```
┌──────────┐                    ┌──────────┐                    ┌──────────┐
│  Client  │                    │  Server  │                    │ Database │
└──────────┘                    └──────────┘                    └──────────┘
     │                                │                                │
     │  1. Login (credentials)        │                                │
     │───────────────────────────────>│                                │
     │                                │  2. Validate & Create Session  │
     │                                │───────────────────────────────>│
     │                                │<───────────────────────────────│
     │  3. Return Session ID          │                                │
     │<───────────────────────────────│                                │
     │                                │                                │
     │  4. Request + Session ID       │                                │
     │───────────────────────────────>│                                │
     │                                │  5. Validate Session           │
     │                                │───────────────────────────────>│
     │                                │<───────────────────────────────│
     │  6. Response                   │                                │
     │<───────────────────────────────│                                │
```

**Pros:**
- Easy to invalidate (logout)
- Server has full control
- Secure (session data on server)

**Cons:**
- Requires server-side storage
- Doesn't scale well
- Not suitable for microservices
- CORS issues

### JWT-Based Authentication

```
┌──────────┐                    ┌──────────┐
│  Client  │                    │  Server  │
└──────────┘                    └──────────┘
     │                                │
     │  1. Login (credentials)        │
     │───────────────────────────────>│
     │                                │ 2. Validate & Generate JWT
     │  3. Return JWT Token           │
     │<───────────────────────────────│
     │                                │
     │  4. Request + JWT Token        │
     │───────────────────────────────>│
     │                                │ 5. Validate JWT (locally)
     │  6. Response                   │
     │<───────────────────────────────│
```

**Pros:**
- Stateless (no server storage)
- Scalable
- Perfect for microservices
- Cross-domain friendly
- Mobile-friendly

**Cons:**
- Larger token size
- Cannot invalidate easily
- Requires careful expiration handling
- Vulnerable if secret is compromised

## Getting Started

### 1. Add Dependencies

**Maven (pom.xml):**
```xml
<dependencies>
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- JWT Library -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### 2. Configure JWT Properties

**application.yml:**
```yaml
jwt:
  secret: your-256-bit-secret-key-change-this-in-production-min-32-chars
  expiration: 86400000  # 24 hours in milliseconds
  refresh-expiration: 604800000  # 7 days in milliseconds
```

## JWT Token Generation

### JWT Utility Class

```java
package com.example.jwt.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    // Generate token with username
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }
    
    // Generate token with custom claims
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }
    
    // Create token
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    // Extract expiration date
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    // Extract specific claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    // Extract all claims
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    // Check if token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    // Validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    // Validate token without UserDetails
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

### Enhanced JWT Utility with Roles

```java
package com.example.jwt.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EnhancedJwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    public String generateToken(UserDetails userDetails, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", userDetails.getUsername());
        
        // Extract roles
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roles);
        
        return createToken(claims, userDetails.getUsername());
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setIssuer("my-app")
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }
    
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("roles");
    }
    
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("JWT token is expired", e);
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        } catch (SignatureException e) {
            throw new RuntimeException("Invalid JWT signature", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("JWT claims string is empty", e);
        }
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
```

## JWT Token Validation

### JWT Authentication Filter

```java
package com.example.jwt.filter;

import com.example.jwt.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String token = extractTokenFromRequest(request);
            
            if (token != null && jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

### JWT Entry Point (Handle Auth Errors)

```java
package com.example.jwt.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" 
                + authException.getMessage() + "\"}");
    }
}
```

## JWT Filter Implementation

### Security Configuration

```java
package com.example.jwt.config;

import com.example.jwt.filter.JwtAuthenticationFilter;
import com.example.jwt.security.JwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                         JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                         UserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.userDetailsService = userDetailsService;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Disable CSRF for stateless JWT
            .cors()  // Enable CORS
            .and()
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/auth/**").permitAll()  // Public endpoints
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")  // Admin only
                .anyRequest().authenticated()  // All other requests need auth
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Stateless
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
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

## Complete Authentication Flow

### DTOs

```java
package com.example.jwt.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
```

```java
package com.example.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    
    public JwtResponse(String token, Long id, String username, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}
```

```java
package com.example.jwt.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
}
```

### Authentication Controller

```java
package com.example.jwt.controller;

import com.example.jwt.dto.JwtResponse;
import com.example.jwt.dto.LoginRequest;
import com.example.jwt.dto.RegisterRequest;
import com.example.jwt.model.User;
import com.example.jwt.service.UserService;
import com.example.jwt.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    
    public AuthController(AuthenticationManager authenticationManager,
                         JwtUtil jwtUtil,
                         UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Generate JWT token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(userDetails);
        
        // Get user details
        User user = userService.findByUsername(userDetails.getUsername());
        
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new JwtResponse(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        ));
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        // Check if username already exists
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body("Error: Username is already taken!");
        }
        
        // Check if email already exists
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body("Error: Email is already in use!");
        }
        
        // Create new user
        User user = userService.registerUser(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword()
        );
        
        return ResponseEntity.ok("User registered successfully!");
    }
}
```

### Test Authentication

**1. Register a new user:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@example.com",
    "password": "password123"
  }'
```

**2. Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "john",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

**3. Access protected endpoint:**
```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Refresh Tokens

### Why Refresh Tokens?

- Access tokens should have short expiration (15-30 minutes)
- Refresh tokens have longer expiration (7-30 days)
- Refresh tokens used to get new access tokens without re-login

### Refresh Token Model

```java
package com.example.jwt.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Data
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(nullable = false)
    private Instant expiryDate;
}
```

### Refresh Token Service

```java
package com.example.jwt.service;

import com.example.jwt.model.RefreshToken;
import com.example.jwt.model.User;
import com.example.jwt.repository.RefreshTokenRepository;
import com.example.jwt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    
    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenDurationMs;
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                              UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }
    
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();
        
        refreshToken.setUser(userRepository.findById(userId).get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        
        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }
    
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        
        return token;
    }
    
    @Transactional
    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}
```

### Refresh Token Endpoint

```java
@PostMapping("/refreshtoken")
public ResponseEntity<?> refreshtoken(@RequestBody TokenRefreshRequest request) {
    String requestRefreshToken = request.getRefreshToken();
    
    return refreshTokenService.findByToken(requestRefreshToken)
            .map(refreshTokenService::verifyExpiration)
            .map(RefreshToken::getUser)
            .map(user -> {
                String token = jwtUtil.generateToken(user.getUsername());
                return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
            })
            .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
}

@PostMapping("/logout")
public ResponseEntity<?> logoutUser(@AuthenticationPrincipal UserDetails userDetails) {
    User user = userService.findByUsername(userDetails.getUsername());
    refreshTokenService.deleteByUserId(user.getId());
    return ResponseEntity.ok("Log out successful!");
}
```

## Role-Based Access Control

### Using JWT Claims for Roles

```java
@RestController
@RequestMapping("/api")
public class ResourceController {
    
    // Only ADMIN can access
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/data")
    public ResponseEntity<?> getAdminData() {
        return ResponseEntity.ok("Admin data");
    }
    
    // USER or ADMIN can access
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/user/data")
    public ResponseEntity<?> getUserData() {
        return ResponseEntity.ok("User data");
    }
    
    // Check specific permission
    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @PostMapping("/data")
    public ResponseEntity<?> createData(@RequestBody DataRequest request) {
        return ResponseEntity.ok("Data created");
    }
    
    // Complex expression
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id")
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok("User details");
    }
}
```

## Token Blacklisting

### Why Token Blacklisting?

JWT tokens cannot be invalidated once issued. Blacklisting helps with:
- Immediate logout
- Compromised token revocation
- Session management

### Redis-Based Blacklist

```java
package com.example.jwt.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";
    
    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public void blacklistToken(String token, long expirationTime) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", expirationTime, TimeUnit.MILLISECONDS);
    }
    
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
```

### Update JWT Filter

```java
@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {
    
    try {
        String token = extractTokenFromRequest(request);
        
        if (token != null && !tokenBlacklistService.isBlacklisted(token) && jwtUtil.validateToken(token)) {
            // ... authentication logic
        }
    } catch (Exception e) {
        logger.error("Cannot set user authentication: {}", e);
    }
    
    filterChain.doFilter(request, response);
}
```

### Logout with Blacklisting

```java
@PostMapping("/logout")
public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
    String token = authHeader.substring(7);  // Remove "Bearer "
    
    long expirationTime = jwtUtil.extractExpiration(token).getTime() - System.currentTimeMillis();
    tokenBlacklistService.blacklistToken(token, expirationTime);
    
    return ResponseEntity.ok("Logged out successfully");
}
```

## Security Best Practices

### 1. Use Strong Secrets

```yaml
# ❌ Bad
jwt:
  secret: secret

# ✅ Good (minimum 256 bits)
jwt:
  secret: ${JWT_SECRET:your-very-long-secret-key-with-at-least-32-characters-for-hs256}
```

### 2. Short Expiration Times

```yaml
jwt:
  expiration: 900000  # 15 minutes (good)
  # expiration: 86400000  # 24 hours (too long)
```

### 3. Use HTTPS Only

```yaml
server:
  ssl:
    enabled: true
```

### 4. Validate Token Claims

```java
public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    final String issuer = extractClaim(token, Claims::getIssuer);
    
    return (username.equals(userDetails.getUsername()) 
            && !isTokenExpired(token)
            && "my-app".equals(issuer));  // Validate issuer
}
```

### 5. Implement Rate Limiting

```java
@Component
public class LoginRateLimiter {
    
    private final LoadingCache<String, Integer> attemptsCache;
    
    public LoginRateLimiter() {
        attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }
    
    public void loginFailed(String key) {
        int attempts = attemptsCache.getUnchecked(key);
        attempts++;
        attemptsCache.put(key, attempts);
    }
    
    public boolean isBlocked(String key) {
        return attemptsCache.getUnchecked(key) >= 5;
    }
}
```

### 6. Store Sensitive Data in Environment Variables

```java
@Configuration
public class JwtConfig {
    
    @Bean
    public String jwtSecret() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters");
        }
        return secret;
    }
}
```

### 7. Sanitize JWT Claims

```java
public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    // ❌ Don't include sensitive data
    // claims.put("password", user.getPassword());
    // claims.put("ssn", user.getSsn());
    
    // ✅ Only include necessary, non-sensitive data
    claims.put("roles", userDetails.getAuthorities());
    claims.put("email", userDetails.getUsername());
    
    return createToken(claims, userDetails.getUsername());
}
```

## OAuth2 Introduction

### What is OAuth2?

OAuth2 is an authorization framework that enables applications to obtain limited access to user accounts on an HTTP service.

### OAuth2 Flow

```
┌─────────┐                              ┌─────────────┐
│  User   │                              │   Client    │
│         │                              │ Application │
└────┬────┘                              └──────┬──────┘
     │                                          │
     │  1. User clicks "Login with Google"     │
     │<─────────────────────────────────────────┤
     │                                          │
     │  2. Redirect to Google OAuth            │
     ├─────────────────────────────────────────>│
     │                                          │
     │                    ┌─────────────────────┴─────┐
     │                    │   Google (OAuth Provider)  │
     │                    └─────────────────────┬─────┘
     │  3. User logs in                        │
     ├────────────────────────────────────────>│
     │                                          │
     │  4. Authorization code                  │
     │<─────────────────────────────────────────┤
     │                                          │
     │  5. Exchange code for access token      │
     ├─────────────────────────────────────────>│
     │                                          │
     │  6. Access token                        │
     │<─────────────────────────────────────────┤
```

### OAuth2 with Spring Security

```java
@Configuration
@EnableWebSecurity
public class OAuth2SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/home")
                .failureUrl("/login?error=true")
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
            );
        
        return http.build();
    }
}
```

**application.yml:**
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: your-client-id
            client-secret: your-client-secret
            scope:
              - email
              - profile
```

## Complete Example

### Project Structure

```
src/main/java/com/example/jwt/
├── JwtApplication.java
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   └── UserController.java
├── dto/
│   ├── LoginRequest.java
│   ├── JwtResponse.java
│   └── RegisterRequest.java
├── filter/
│   └── JwtAuthenticationFilter.java
├── model/
│   ├── User.java
│   ├── Role.java
│   └── RefreshToken.java
├── repository/
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   └── RefreshTokenRepository.java
├── security/
│   └── JwtAuthenticationEntryPoint.java
├── service/
│   ├── UserService.java
│   ├── CustomUserDetailsService.java
│   └── RefreshTokenService.java
└── util/
    └── JwtUtil.java
```

### Complete application.yml

```yaml
spring:
  application:
    name: jwt-auth-demo
  datasource:
    url: jdbc:postgresql://localhost:5432/jwt_db
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-change-this-in-production-minimum-32-characters-required}
  expiration: 900000  # 15 minutes
  refresh-expiration: 604800000  # 7 days

server:
  port: 8080

logging:
  level:
    com.example.jwt: DEBUG
    org.springframework.security: DEBUG
```

## Exercises

### Exercise 1: Basic JWT Implementation
Create a Spring Boot application with JWT authentication.

**Requirements:**
1. Add JWT dependencies
2. Implement JwtUtil for token generation and validation
3. Create JWT authentication filter
4. Configure Spring Security
5. Create login endpoint that returns JWT token

### Exercise 2: User Registration and Login
Implement complete user registration and login flow.

**Requirements:**
1. Create User and Role entities
2. Implement registration endpoint
3. Implement login endpoint
4. Return JWT token on successful login
5. Test with Postman/cURL

### Exercise 3: Protected Endpoints
Create protected endpoints with role-based access.

**Requirements:**
1. Create /api/user endpoint (requires USER role)
2. Create /api/admin endpoint (requires ADMIN role)
3. Use @PreAuthorize for method security
4. Test access with different roles

### Exercise 4: Refresh Token Implementation
Add refresh token functionality.

**Requirements:**
1. Create RefreshToken entity
2. Implement RefreshTokenService
3. Create refresh token endpoint
4. Implement logout with token invalidation
5. Test refresh token flow

### Exercise 5: Token Blacklisting
Implement token blacklisting with Redis.

**Requirements:**
1. Set up Redis
2. Create TokenBlacklistService
3. Update JWT filter to check blacklist
4. Implement logout with blacklisting
5. Test token invalidation

## Key Takeaways

1. **JWT is Stateless**
   - No server-side session storage
   - Perfect for microservices and REST APIs
   - Scalable and efficient

2. **JWT Structure**
   - Header: Algorithm and token type
   - Payload: Claims (user data)
   - Signature: Ensures integrity

3. **Token Management**
   - Short expiration for access tokens (15-30 min)
   - Long expiration for refresh tokens (7-30 days)
   - Implement token blacklisting for logout

4. **Security Best Practices**
   - Use strong secrets (256+ bits)
   - Always use HTTPS
   - Validate all token claims
   - Implement rate limiting
   - Don't store sensitive data in tokens

5. **Role-Based Access Control**
   - Include roles in JWT claims
   - Use @PreAuthorize for method security
   - Validate roles on every request

6. **Refresh Tokens**
   - Extend user sessions without re-login
   - Store securely in database
   - Implement expiration and revocation

## Resources

### Official Documentation
- [JWT.io](https://jwt.io/)
- [RFC 7519 - JWT Specification](https://tools.ietf.org/html/rfc7519)
- [JJWT Library](https://github.com/jwtk/jjwt)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)

### Tutorials
- [Spring Security with JWT](https://www.baeldung.com/spring-security-oauth-jwt)
- [JWT Authentication Tutorial](https://www.bezkoder.com/spring-boot-jwt-authentication/)
- [OAuth2 with Spring Boot](https://spring.io/guides/tutorials/spring-boot-oauth2/)

### Tools
- [JWT Debugger](https://jwt.io/)
- [Postman](https://www.postman.com/)
- [Online JWT Generator](https://jwtbuilder.jamiekurtz.com/)

### Security
- [OWASP JWT Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)

---
[<< Previous: Day 10](../Day10_Security_Basics/README.md) | [Back to Main](../README.md) | [Next: Day 12 >>](../Day12_Microservices_Intro/README.md)
