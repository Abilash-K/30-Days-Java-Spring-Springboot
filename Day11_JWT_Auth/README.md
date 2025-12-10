# Day 11: JWT Authentication and Authorization

## 📋 Table of Contents
- [Introduction](#introduction)
- [What is JWT?](#what-is-jwt)
- [JWT Structure](#jwt-structure)
- [JWT vs Session Authentication](#jwt-vs-session-authentication)
- [Setting Up JWT](#setting-up-jwt)
- [JWT Utility Class](#jwt-utility-class)
- [JWT Authentication Filter](#jwt-authentication-filter)
- [Authentication Endpoints](#authentication-endpoints)
- [Refresh Tokens](#refresh-tokens)
- [Role-Based Access Control](#role-based-access-control)
- [Best Practices](#best-practices)
- [Complete Example](#complete-example)
- [OAuth2 Introduction](#oauth2-introduction)
- [Exercises](#exercises)
- [Key Takeaways](#key-takeaways)
- [Resources](#resources)

## Introduction

Welcome to Day 11! Today we'll implement **JWT (JSON Web Token) Authentication** - a modern, stateless approach to securing REST APIs and microservices.

### What You'll Learn
- Understanding JWT structure and components
- Implementing JWT generation and validation
- Creating stateless authentication
- Handling refresh tokens
- Implementing role-based access control
- JWT security best practices
- Introduction to OAuth2

### Why JWT Matters
- ✅ **Stateless**: No server-side session storage required
- ✅ **Scalable**: Perfect for microservices and distributed systems
- ✅ **Cross-Domain**: Works across different domains
- ✅ **Mobile-Friendly**: Ideal for mobile and SPA applications
- ✅ **Self-Contained**: Carries user information in the token
- ✅ **Compact**: Smaller than XML-based tokens (SAML)

## What is JWT?

### Definition

**JSON Web Token (JWT)** is an open standard (RFC 7519) that defines a compact and self-contained way for securely transmitting information between parties as a JSON object.

### Key Characteristics

1. **Compact**: Small size, can be sent through URL, POST parameter, or HTTP header
2. **Self-Contained**: Contains all required information about the user
3. **Secure**: Can be signed and encrypted
4. **Verifiable**: Signature ensures the token hasn't been tampered with

### Use Cases

- **Authentication**: Most common use case
- **Information Exchange**: Secure information transfer between parties
- **Authorization**: Grant access to resources
- **Single Sign-On (SSO)**: Share authentication across systems

## JWT Structure

### Three Parts

A JWT consists of three parts separated by dots (`.`):

```
header.payload.signature
```

**Example JWT:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

### 1. Header

Contains metadata about the token:

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

- **alg**: Signing algorithm (HS256, RS256, etc.)
- **typ**: Token type (JWT)

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
- **iss** (issuer): Who issued the token
- **sub** (subject): Subject of the token (user ID)
- **aud** (audience): Intended recipient
- **exp** (expiration): When token expires
- **nbf** (not before): Token not valid before this time
- **iat** (issued at): When token was issued
- **jti** (JWT ID): Unique identifier for the token

**Custom Claims:**
- Any additional data you want to include
- Keep it minimal to reduce token size

### 3. Signature

Ensures the token hasn't been tampered with:

```
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret
)
```

**Verification Process:**
1. Server receives JWT
2. Decodes header and payload
3. Recreates signature using secret key
4. Compares signatures
5. If match → Valid token
6. If no match → Invalid token

## JWT vs Session Authentication

### Session-Based Authentication

```
┌──────────┐      Login       ┌──────────┐
│  Client  │ ────────────────> │  Server  │
│          │                   │          │
│          │ <──────────────── │          │
│          │   Session Cookie  │          │
│          │                   │          │
│          │   Request + Cookie│          │
│          │ ────────────────> │  Checks  │
│          │                   │  Session │
│          │ <──────────────── │  Store   │
│          │     Response      │          │
└──────────┘                   └──────────┘
                                    │
                               ┌────▼────┐
                               │ Session │
                               │  Store  │
                               └─────────┘
```

**Characteristics:**
- Server stores session data
- Client stores session ID cookie
- Stateful (server must maintain session)
- Horizontal scaling requires shared session store

### JWT-Based Authentication

```
┌──────────┐      Login       ┌──────────┐
│  Client  │ ────────────────> │  Server  │
│          │   Credentials     │          │
│          │                   │          │
│          │ <──────────────── │          │
│          │       JWT         │          │
│   Stores │                   │          │
│    JWT   │   Request + JWT   │          │
│          │ ────────────────> │ Validates│
│          │                   │    JWT   │
│          │ <──────────────── │          │
│          │     Response      │          │
└──────────┘                   └──────────┘

No server-side storage needed!
```

**Characteristics:**
- No server-side session storage
- Token contains all user information
- Stateless (server doesn't track sessions)
- Easy horizontal scaling

### Comparison

| Feature | Session | JWT |
|---------|---------|-----|
| **Storage** | Server-side | Client-side |
| **Scalability** | Complex | Easy |
| **State** | Stateful | Stateless |
| **Size** | Small (session ID) | Larger (full token) |
| **Revocation** | Easy | Complex |
| **Cross-Domain** | Complex | Easy |
| **Mobile Apps** | Complex | Easy |
| **Microservices** | Complex | Easy |

## Setting Up JWT

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
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### 2. JWT Configuration Properties

**application.yml:**
```yaml
jwt:
  secret: your-256-bit-secret-key-change-this-in-production
  expiration: 86400000  # 24 hours in milliseconds
  refresh-expiration: 604800000  # 7 days

spring:
  security:
    user:
      name: admin
      password: admin123
```

### 3. JWT Properties Class

**JwtProperties.java:**
```java
package com.example.jwt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private String secret;
    private Long expiration;
    private Long refreshExpiration;
}
```

## JWT Utility Class

**JwtUtil.java:**
```java
package com.example.jwt.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final String secret;
    private final Long expiration;

    public JwtUtil(JwtProperties jwtProperties) {
        this.secret = jwtProperties.getSecret();
        this.expiration = jwtProperties.getExpiration();
    }

    // Generate secret key from string
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract specific claim from token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Check if token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Generate token for user
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }

    // Generate token with custom claims
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return createToken(extraClaims, userDetails.getUsername());
    }

    // Create token with claims and subject
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // Validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Generate refresh token
    public String generateRefreshToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 7); // 7x longer

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
}
```

## JWT Authentication Filter

**JwtAuthenticationFilter.java:**
```java
package com.example.jwt.security;

import com.example.jwt.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
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
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Extract JWT from Authorization header
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String username = jwtUtil.extractUsername(jwt);

            // If username is extracted and user is not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Validate token
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }
}
```

## Authentication Endpoints

### 1. DTOs

**LoginRequest.java:**
```java
package com.example.jwt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
}
```

**AuthResponse.java:**
```java
package com.example.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private String username;
    private List<String> roles;
}
```

**RegisterRequest.java:**
```java
package com.example.jwt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String password;
}
```

### 2. Authentication Controller

**AuthController.java:**
```java
package com.example.jwt.controller;

import com.example.jwt.dto.AuthResponse;
import com.example.jwt.dto.LoginRequest;
import com.example.jwt.dto.RegisterRequest;
import com.example.jwt.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        String token = refreshToken.substring(7);
        return ResponseEntity.ok(authService.refreshToken(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // In stateless JWT, logout is handled client-side
        // Or implement token blacklist on server
        return ResponseEntity.ok("Logged out successfully");
    }
}
```

### 3. Authentication Service

**AuthService.java:**
```java
package com.example.jwt.service;

import com.example.jwt.dto.AuthResponse;
import com.example.jwt.dto.LoginRequest;
import com.example.jwt.dto.RegisterRequest;
import com.example.jwt.entity.Role;
import com.example.jwt.entity.User;
import com.example.jwt.repository.RoleRepository;
import com.example.jwt.repository.UserRepository;
import com.example.jwt.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthService(UserRepository userRepository,
                      RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil,
                      AuthenticationManager authenticationManager,
                      UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);

        // Assign default role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Default role not found"));
        user.setRoles(Set.of(userRole));

        userRepository.save(user);

        // Generate tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return buildAuthResponse(accessToken, refreshToken, userDetails);
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Generate tokens
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return buildAuthResponse(accessToken, refreshToken, userDetails);
    }

    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtUtil.validateToken(refreshToken, userDetails)) {
            String newAccessToken = jwtUtil.generateToken(userDetails);
            String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
            return buildAuthResponse(newAccessToken, newRefreshToken, userDetails);
        }

        throw new IllegalArgumentException("Invalid refresh token");
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L) // 24 hours
                .username(userDetails.getUsername())
                .roles(roles)
                .build();
    }
}
```

## Refresh Tokens

### Why Refresh Tokens?

- **Security**: Access tokens have short lifetime (15min - 1hr)
- **User Experience**: Don't force frequent re-login
- **Revocation**: Can revoke refresh tokens if needed

### Implementation Strategy

```
┌──────────┐                           ┌──────────┐
│  Client  │                           │  Server  │
└────┬─────┘                           └────┬─────┘
     │                                      │
     │  1. Login (username/password)        │
     │ ───────────────────────────────────> │
     │                                      │
     │  2. Access Token (short-lived)       │
     │     Refresh Token (long-lived)       │
     │ <─────────────────────────────────── │
     │                                      │
     │  3. API Request + Access Token       │
     │ ───────────────────────────────────> │
     │                                      │
     │  4. Response                         │
     │ <─────────────────────────────────── │
     │                                      │
     │  ... time passes ...                 │
     │  Access Token expires                │
     │                                      │
     │  5. API Request + Expired Token      │
     │ ───────────────────────────────────> │
     │                                      │
     │  6. 401 Unauthorized                 │
     │ <─────────────────────────────────── │
     │                                      │
     │  7. Refresh Request + Refresh Token  │
     │ ───────────────────────────────────> │
     │                                      │
     │  8. New Access Token                 │
     │     New Refresh Token                │
     │ <─────────────────────────────────── │
     │                                      │
```

### Refresh Token Storage

**Option 1: Database Storage**
```java
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String token;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private Instant expiryDate;
    
    private boolean revoked = false;
}
```

**Option 2: Redis Storage** (Recommended)
```java
@Service
public class RefreshTokenService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    public void storeRefreshToken(String username, String token, Duration expiration) {
        redisTemplate.opsForValue().set(
            "refresh:" + username,
            token,
            expiration
        );
    }
    
    public boolean isValidRefreshToken(String username, String token) {
        String stored = redisTemplate.opsForValue().get("refresh:" + username);
        return token.equals(stored);
    }
    
    public void revokeRefreshToken(String username) {
        redisTemplate.delete("refresh:" + username);
    }
}
```

## Role-Based Access Control

### Securing Endpoints with Roles

**ProductController.java:**
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    // Public endpoint - no authentication required
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.findAll());
    }

    // Requires USER or ADMIN role
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.save(product));
    }

    // Requires ADMIN role only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Custom authorization logic
    @PutMapping("/{id}")
    @PreAuthorize("@productSecurityService.canModify(#id, authentication)")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {
        return ResponseEntity.ok(productService.update(id, product));
    }
}
```

## Best Practices

### 1. Secret Key Management

```yaml
# ❌ Never hardcode secrets
jwt:
  secret: my-secret-key

# ✅ Use environment variables
jwt:
  secret: ${JWT_SECRET:fallback-secret-for-dev-only}
  
# ✅ Generate strong keys
# Use at least 256-bit key
# Store in secure vault (AWS Secrets Manager, Azure Key Vault)
```

### 2. Token Expiration

```java
// ✅ Short-lived access tokens
private static final long ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000; // 15 minutes

// ✅ Long-lived refresh tokens
private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 days

// ❌ Avoid long-lived access tokens
private static final long ACCESS_TOKEN_VALIDITY = 30 * 24 * 60 * 60 * 1000; // 30 days - TOO LONG!
```

### 3. Token Storage (Client-Side)

```javascript
// ✅ Secure storage options
// Option 1: Memory (most secure for SPAs)
let accessToken = null;
const setToken = (token) => { accessToken = token; };

// Option 2: HttpOnly Cookie (best for traditional web apps)
// Set cookie from server with HttpOnly flag

// Option 3: localStorage (acceptable for low-risk apps)
localStorage.setItem('accessToken', token);

// ❌ Avoid storing sensitive tokens in
// - Regular cookies (XSS vulnerability)
// - sessionStorage for sensitive data
```

### 4. Token Revocation

```java
// Implement token blacklist for logout
@Service
public class TokenBlacklistService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    public void blacklistToken(String token, Duration remainingValidity) {
        redisTemplate.opsForValue().set(
            "blacklist:" + token,
            "revoked",
            remainingValidity
        );
    }
    
    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }
}

// Check in JWT filter
if (tokenBlacklistService.isBlacklisted(jwt)) {
    throw new JwtException("Token has been revoked");
}
```

### 5. HTTPS Only

```yaml
# ✅ Always use HTTPS in production
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_PASSWORD}
    key-store-type: PKCS12

# Add to SecurityConfig
http.requiresChannel(channel -> channel
    .anyRequest().requiresSecure()
);
```

### 6. Claims Validation

```java
// ✅ Validate all claims
public Boolean validateToken(String token, UserDetails userDetails) {
    try {
        Claims claims = extractAllClaims(token);
        
        // Validate username
        if (!claims.getSubject().equals(userDetails.getUsername())) {
            return false;
        }
        
        // Validate expiration
        if (claims.getExpiration().before(new Date())) {
            return false;
        }
        
        // Validate issuer
        if (!"your-app".equals(claims.getIssuer())) {
            return false;
        }
        
        // Validate audience
        if (!"your-audience".equals(claims.getAudience())) {
            return false;
        }
        
        return true;
    } catch (Exception e) {
        return false;
    }
}
```

### 7. Error Handling

```java
@RestControllerAdvice
public class JwtExceptionHandler {

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(JwtException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid or expired token", ex.getMessage()));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwtException(ExpiredJwtException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Token expired", "Please refresh your token"));
    }
}
```

## Complete Example

### Security Configuration

**JwtSecurityConfig.java:**
```java
package com.example.jwt.config;

import com.example.jwt.security.JwtAuthenticationFilter;
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
@EnableMethodSecurity(prePostEnabled = true)
public class JwtSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;

    public JwtSecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            UserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                
                // Secured endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                
                // All other requests need authentication
                .anyRequest().authenticated()
            )
            
            // Stateless session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Exception handling
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            // Add JWT filter
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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) 
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### Testing the API

```bash
# 1. Register a new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@example.com",
    "password": "password123"
  }'

# Response:
# {
#   "accessToken": "eyJhbGc...",
#   "refreshToken": "eyJhbGc...",
#   "tokenType": "Bearer",
#   "expiresIn": 86400000,
#   "username": "john",
#   "roles": ["ROLE_USER"]
# }

# 2. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123"
  }'

# 3. Access protected endpoint
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer eyJhbGc..."

# 4. Refresh token
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Authorization: Bearer <refresh-token>"
```

## OAuth2 Introduction

### What is OAuth2?

OAuth2 is an **authorization framework** that enables applications to obtain limited access to user accounts on an HTTP service.

### OAuth2 Flow

```
┌──────────┐                                       ┌──────────────┐
│  Client  │                                       │  Auth Server │
│   App    │                                       │   (Google)   │
└────┬─────┘                                       └──────┬───────┘
     │                                                    │
     │  1. Authorization Request                         │
     │ ─────────────────────────────────────────────────>│
     │                                                    │
     │  2. User logs in and grants permission            │
     │                                                    │
     │  3. Authorization Code                            │
     │ <─────────────────────────────────────────────────│
     │                                                    │
     │  4. Exchange code for tokens                      │
     │ ─────────────────────────────────────────────────>│
     │                                                    │
     │  5. Access Token + Refresh Token                  │
     │ <─────────────────────────────────────────────────│
     │                                                    │
     │  6. Access protected resources with token         │
     │ ──────────────────────────────────────────> ┌──────────┐
     │                                             │ Resource │
     │  7. Protected data                          │  Server  │
     │ <────────────────────────────────────────── └──────────┘
```

### OAuth2 Grant Types

1. **Authorization Code**: Most secure, for web apps
2. **Client Credentials**: For server-to-server communication
3. **Refresh Token**: Obtain new access token
4. **Password Grant**: Legacy, not recommended

### Spring Security OAuth2

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: profile, email
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope: user:email
```

## Exercises

### Exercise 1: Basic JWT Implementation
Implement a complete JWT authentication system with:
- User registration
- Login endpoint
- Token generation
- Token validation
- Protected endpoints

### Exercise 2: Refresh Token
Add refresh token functionality:
- Store refresh tokens in database/Redis
- Implement token refresh endpoint
- Handle expired access tokens
- Revoke refresh tokens on logout

### Exercise 3: Role-Based Access
Create an API with role-based access control:
- Admin can CRUD all products
- Manager can read/update products
- User can only read products
- Use @PreAuthorize annotations

### Exercise 4: Token Blacklist
Implement token blacklisting:
- Store revoked tokens
- Check blacklist on each request
- Clean up expired tokens
- Handle logout properly

### Exercise 5: OAuth2 Integration
Add OAuth2 social login:
- Configure Google OAuth2
- Configure GitHub OAuth2
- Handle OAuth2 callback
- Generate JWT after OAuth2 login

## Key Takeaways

1. **JWT** provides stateless authentication perfect for microservices
2. **Three parts**: Header, Payload, Signature
3. **Access tokens** should be short-lived (15-60 minutes)
4. **Refresh tokens** allow obtaining new access tokens
5. **Never store secrets** in code - use environment variables
6. **HTTPS** is mandatory for production
7. **Token blacklisting** enables logout functionality
8. **@PreAuthorize** provides method-level security
9. **Claims** carry user information in the token
10. **OAuth2** enables social login integration

## Resources

### Official Documentation
- [JWT.io](https://jwt.io/) - JWT debugger and documentation
- [Spring Security](https://docs.spring.io/spring-security/reference/index.html)
- [JJWT Library](https://github.com/jwtk/jjwt)
- [OAuth2 Specification](https://oauth.net/2/)

### Tutorials
- [Spring Security JWT](https://www.baeldung.com/spring-security-oauth-jwt)
- [JWT Best Practices](https://curity.io/resources/learn/jwt-best-practices/)
- [OAuth2 with Spring Boot](https://www.baeldung.com/spring-security-oauth)

### Tools
- [JWT.io Debugger](https://jwt.io/#debugger)
- [JSON Web Key Generator](https://mkjwk.org/)
- [OAuth2 Playground](https://www.oauth.com/playground/)

---
[<< Previous: Day 10](../Day10_Security_Basics/README.md) | [Back to Main](../README.md) | [Next: Day 12 >>](../Day12_Microservices_Intro/README.md)
