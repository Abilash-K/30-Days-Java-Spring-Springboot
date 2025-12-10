# Day 14: API Gateway with Spring Cloud Gateway

## 📋 Table of Contents
- [Introduction](#introduction)
- [API Gateway Pattern](#api-gateway-pattern)
- [Spring Cloud Gateway Overview](#spring-cloud-gateway-overview)
- [Setting Up Spring Cloud Gateway](#setting-up-spring-cloud-gateway)
- [Route Configuration](#route-configuration)
- [Predicates](#predicates)
- [Filters](#filters)
- [Rate Limiting](#rate-limiting)
- [Circuit Breaker Integration](#circuit-breaker-integration)
- [Security Integration](#security-integration)
- [Complete Example](#complete-example)
- [Best Practices](#best-practices)
- [Exercises](#exercises)
- [Key Takeaways](#key-takeaways)
- [Resources](#resources)

## Introduction

Welcome to Day 14! Today we'll explore **API Gateway with Spring Cloud Gateway** - a crucial component for building scalable microservices architectures.

### What You'll Learn
- Understanding the API Gateway pattern
- Setting up Spring Cloud Gateway
- Configuring routes dynamically
- Using predicates and filters
- Implementing rate limiting
- Integrating with service discovery
- Production-ready gateway patterns
- Security and authentication at gateway level

### Why API Gateway Matters
- ✅ **Single Entry Point**: One endpoint for all microservices
- ✅ **Security**: Centralized authentication and authorization
- ✅ **Routing**: Intelligent request routing to services
- ✅ **Load Balancing**: Distribute traffic across instances
- ✅ **Rate Limiting**: Protect services from overload
- ✅ **Monitoring**: Centralized logging and metrics
- ✅ **Protocol Translation**: REST to gRPC, HTTP to WebSocket
- ✅ **Response Aggregation**: Combine multiple service calls

## API Gateway Pattern

### The Problem: Direct Client Access

**Without API Gateway:**
```
┌──────────┐     ┌─────────────────┐
│  Mobile  │────>│  User Service   │
│   App    │     │  :8081          │
└──────────┘     └─────────────────┘
     │
     │           ┌─────────────────┐
     └──────────>│  Order Service  │
     │           │  :8082          │
     │           └─────────────────┘
     │
     │           ┌─────────────────┐
     └──────────>│ Payment Service │
                 │  :8083          │
                 └─────────────────┘

Problems:
❌ Multiple endpoints to manage
❌ CORS configuration on each service
❌ Authentication repeated in each service
❌ No centralized rate limiting
❌ Difficult to monitor and log
❌ No single point for SSL termination
```

### The Solution: API Gateway

**With API Gateway:**
```
┌──────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Mobile  │────>│   API Gateway   │────>│  User Service   │
│   App    │     │   :8080         │     │  :8081          │
└──────────┘     │                 │     └─────────────────┘
                 │  • Auth         │
┌──────────┐     │  • Rate Limit   │     ┌─────────────────┐
│   Web    │────>│  • Routing      │────>│  Order Service  │
│   App    │     │  • Monitoring   │     │  :8082          │
└──────────┘     │  • Load Balance │     └─────────────────┘
                 │                 │
                 │                 │     ┌─────────────────┐
                 │                 │────>│ Payment Service │
                 └─────────────────┘     │  :8083          │
                                         └─────────────────┘

Benefits:
✅ Single entry point
✅ Centralized security
✅ Unified monitoring
✅ Service abstraction
✅ Protocol translation
✅ Response aggregation
```

### API Gateway Responsibilities

**1. Request Routing**
- Route requests to appropriate microservices
- Path-based routing (`/users/**` → User Service)
- Header-based routing (API version in header)
- Query parameter routing

**2. Authentication & Authorization**
- Validate JWT tokens
- OAuth2 integration
- API key validation
- Rate limiting per user

**3. Protocol Translation**
- HTTP to gRPC
- REST to WebSocket
- Legacy SOAP to REST

**4. Request/Response Transformation**
- Add headers (correlation ID, trace ID)
- Modify request body
- Aggregate responses from multiple services
- Transform response format

**5. Cross-Cutting Concerns**
- Logging and monitoring
- Rate limiting and throttling
- Circuit breaking
- Caching
- SSL termination

## Spring Cloud Gateway Overview

### Architecture

Spring Cloud Gateway is built on:
- **Spring Framework 5**
- **Project Reactor** (reactive programming)
- **Spring Boot 3**
- **Spring WebFlux** (non-blocking)

### Key Components

**1. Route**: The basic building block
```java
Route = ID + Destination URI + Predicates + Filters
```

**2. Predicate**: Matches HTTP requests
```java
// Example: Match all GET requests to /users
Predicate: Method=GET AND Path=/users/**
```

**3. Filter**: Modify request/response
```java
// Example: Add header to response
Filter: AddResponseHeader=X-Response-Time, 10ms
```

### Spring Cloud Gateway vs Zuul

| Feature | Spring Cloud Gateway | Zuul 1.x | Zuul 2.x |
|---------|---------------------|----------|----------|
| Async/Non-blocking | ✅ Yes | ❌ No | ✅ Yes |
| Spring Boot 3 | ✅ Yes | ❌ No | ⚠️ Limited |
| Reactive (WebFlux) | ✅ Yes | ❌ No | ❌ No |
| Performance | ⚡ Excellent | ⚠️ Good | ⚡ Excellent |
| Active Development | ✅ Active | ❌ Maintenance | ⚠️ Limited |
| Netflix Support | N/A | ❌ Deprecated | ⚠️ Limited |
| Predicates & Filters | ✅ Built-in | ⚠️ Custom | ⚠️ Custom |

**Recommendation**: Use **Spring Cloud Gateway** for new projects.

## Setting Up Spring Cloud Gateway

### 1. Project Dependencies

**pom.xml**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>api-gateway</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>
    
    <dependencies>
        <!-- Spring Cloud Gateway -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        
        <!-- Service Discovery (Eureka Client) -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        
        <!-- Circuit Breaker (Resilience4j) -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
        </dependency>
        
        <!-- Redis for Rate Limiting -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
        </dependency>
        
        <!-- Actuator for Monitoring -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Security (Optional) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <!-- JWT -->
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
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 2. Main Application Class

**ApiGatewayApplication.java**
```java
package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient // Enable Eureka client
public class ApiGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

### 3. Basic Configuration

**application.yml**
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
    
  cloud:
    gateway:
      # Enable discovery locator for automatic route creation
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true

# Eureka Configuration
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}

# Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: gateway, health, metrics, prometheus
  endpoint:
    gateway:
      enabled: true
    health:
      show-details: always

# Logging
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    reactor.netty: INFO
```

## Route Configuration

### 1. YAML-Based Routes

**application.yml**
```yaml
spring:
  cloud:
    gateway:
      routes:
        # User Service Routes
        - id: user-service
          uri: lb://user-service  # lb = load-balanced via Eureka
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2  # Remove /api/users, forward /
            - AddRequestHeader=X-Request-Source, API-Gateway
            - AddResponseHeader=X-Response-Time, ${responseTime}
            
        # Order Service Routes
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
            - Method=GET,POST
          filters:
            - StripPrefix=2
            - name: CircuitBreaker
              args:
                name: orderCircuitBreaker
                fallbackUri: forward:/fallback/orders
                
        # Product Service Routes
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
            - Header=X-API-Version, v1  # Header-based routing
          filters:
            - StripPrefix=2
            - name: RequestRateLimiter
              args:
                redis-rate-limiter:
                  replenishRate: 10  # Tokens per second
                  burstCapacity: 20   # Max tokens
                  requestedTokens: 1
                  
        # Payment Service Routes with Query Parameter
        - id: payment-service
          uri: lb://payment-service
          predicates:
            - Path=/api/payments/**
            - Query=type, card  # Only if query param type=card
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Payment-Gateway, Stripe
            
        # Legacy Service with Rewrite
        - id: legacy-service
          uri: http://legacy-system.com:8090
          predicates:
            - Path=/legacy/**
          filters:
            - RewritePath=/legacy/(?<segment>.*), /$\{segment}
            - SetPath=/api/{segment}
```

### 2. Java-Based Route Configuration

**GatewayConfig.java**
```java
package com.example.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.time.Duration;

@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Route
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-Request-Source", "API-Gateway")
                                .addResponseHeader("X-Powered-By", "Spring Cloud Gateway")
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .setBackoff(Duration.ofSeconds(1), 
                                                   Duration.ofSeconds(5), 
                                                   2, true)
                                )
                        )
                        .uri("lb://user-service")
                )
                
                // Order Service with Circuit Breaker
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .and()
                        .method(HttpMethod.GET, HttpMethod.POST)
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(config -> config
                                        .setName("orderCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/orders")
                                )
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                )
                        )
                        .uri("lb://order-service")
                )
                
                // Product Service with Custom Filter
                .route("product-service", r -> r
                        .path("/api/products/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter(new CustomLoggingFilter())
                                .modifyRequestBody(String.class, String.class,
                                        (exchange, body) -> {
                                            // Modify request body
                                            return Mono.just(body.toUpperCase());
                                        })
                        )
                        .uri("lb://product-service")
                )
                
                // Conditional Routing based on Header
                .route("api-v2-route", r -> r
                        .path("/api/v2/**")
                        .and()
                        .header("X-API-Version", "2.0")
                        .filters(f -> f
                                .stripPrefix(2)
                                .setPath("/api/v2/{segment}")
                        )
                        .uri("lb://product-service-v2")
                )
                
                // WebSocket Route
                .route("websocket-route", r -> r
                        .path("/ws/**")
                        .uri("lb:ws://notification-service")
                )
                
                .build();
    }
    
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
        // replenishRate=10, burstCapacity=20, requestedTokens=1
    }
}
```

### 3. Dynamic Route Configuration

**DynamicRouteService.java**
```java
package com.example.gateway.service;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DynamicRouteService {
    
    private final RouteDefinitionWriter routeDefinitionWriter;
    private final ApplicationEventPublisher eventPublisher;
    
    public DynamicRouteService(RouteDefinitionWriter routeDefinitionWriter,
                               ApplicationEventPublisher eventPublisher) {
        this.routeDefinitionWriter = routeDefinitionWriter;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Add a new route dynamically
     */
    public Mono<Void> addRoute(RouteDefinition routeDefinition) {
        return routeDefinitionWriter.save(Mono.just(routeDefinition))
                .then(Mono.defer(() -> {
                    // Publish refresh event
                    eventPublisher.publishEvent(new RefreshRoutesEvent(this));
                    return Mono.empty();
                }));
    }
    
    /**
     * Update an existing route
     */
    public Mono<Void> updateRoute(RouteDefinition routeDefinition) {
        return deleteRoute(routeDefinition.getId())
                .then(addRoute(routeDefinition));
    }
    
    /**
     * Delete a route
     */
    public Mono<Void> deleteRoute(String routeId) {
        return routeDefinitionWriter.delete(Mono.just(routeId))
                .then(Mono.defer(() -> {
                    eventPublisher.publishEvent(new RefreshRoutesEvent(this));
                    return Mono.empty();
                }));
    }
}
```

**RouteController.java**
```java
package com.example.gateway.controller;

import com.example.gateway.service.DynamicRouteService;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/admin/routes")
public class RouteController {
    
    private final DynamicRouteService dynamicRouteService;
    
    public RouteController(DynamicRouteService dynamicRouteService) {
        this.dynamicRouteService = dynamicRouteService;
    }
    
    @PostMapping
    public Mono<ResponseEntity<String>> addRoute(@RequestBody RouteDefinition routeDefinition) {
        return dynamicRouteService.addRoute(routeDefinition)
                .then(Mono.just(ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body("Route added successfully")));
    }
    
    @PutMapping("/{id}")
    public Mono<ResponseEntity<String>> updateRoute(
            @PathVariable String id,
            @RequestBody RouteDefinition routeDefinition) {
        routeDefinition.setId(id);
        return dynamicRouteService.updateRoute(routeDefinition)
                .then(Mono.just(ResponseEntity.ok("Route updated successfully")));
    }
    
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> deleteRoute(@PathVariable String id) {
        return dynamicRouteService.deleteRoute(id)
                .then(Mono.just(ResponseEntity.ok("Route deleted successfully")));
    }
}
```

## Predicates

Spring Cloud Gateway includes many built-in route predicates:

### 1. Path-Based Predicates

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: path-route
          uri: lb://service
          predicates:
            - Path=/api/users/{id}  # Matches /api/users/123
            - Path=/api/orders/**   # Matches /api/orders/... (multiple segments)
```

### 2. Method-Based Predicates

```yaml
predicates:
  - Method=GET          # Only GET requests
  - Method=GET,POST     # GET or POST requests
```

### 3. Header-Based Predicates

```yaml
predicates:
  - Header=X-API-Version, v[12]  # Regex: v1 or v2
  - Header=X-Request-ID          # Header must exist
```

### 4. Query Parameter Predicates

```yaml
predicates:
  - Query=foo, ba.      # Regex: matches bar, baz, etc.
  - Query=page          # Query param 'page' must exist
  - Query=size, \d+     # Regex: size must be a number
```

### 5. Cookie-Based Predicates

```yaml
predicates:
  - Cookie=session, abc  # Cookie 'session' with value 'abc'
```

### 6. Host-Based Predicates

```yaml
predicates:
  - Host=**.example.com  # Matches api.example.com, www.example.com
  - Host={sub}.example.com  # Captures subdomain
```

### 7. Time-Based Predicates

```yaml
predicates:
  # After a specific datetime
  - After=2024-01-01T00:00:00+00:00[UTC]
  
  # Before a specific datetime
  - Before=2024-12-31T23:59:59+00:00[UTC]
  
  # Between two datetimes
  - Between=2024-01-01T00:00:00+00:00[UTC], 2024-12-31T23:59:59+00:00[UTC]
```

### 8. Weight-Based Predicates (A/B Testing)

```yaml
spring:
  cloud:
    gateway:
      routes:
        # 80% traffic to v1
        - id: service-v1
          uri: lb://service-v1
          predicates:
            - Path=/api/**
            - Weight=group1, 80
            
        # 20% traffic to v2
        - id: service-v2
          uri: lb://service-v2
          predicates:
            - Path=/api/**
            - Weight=group1, 20
```

### 9. Custom Predicates

**CustomHeaderRoutePredicateFactory.java**
```java
package com.example.gateway.predicate;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Predicate;

@Component
public class CustomHeaderRoutePredicateFactory 
        extends AbstractRoutePredicateFactory<CustomHeaderRoutePredicateFactory.Config> {
    
    public CustomHeaderRoutePredicateFactory() {
        super(Config.class);
    }
    
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            String headerValue = exchange.getRequest()
                    .getHeaders()
                    .getFirst(config.getHeaderName());
            
            return headerValue != null && 
                   headerValue.matches(config.getRegex());
        };
    }
    
    public static class Config {
        private String headerName;
        private String regex;
        
        // Getters and setters
        public String getHeaderName() {
            return headerName;
        }
        
        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
        
        public String getRegex() {
            return regex;
        }
        
        public void setRegex(String regex) {
            this.regex = regex;
        }
    }
}
```

**Usage:**
```yaml
predicates:
  - CustomHeader=X-User-Type, premium|gold
```

## Filters

### 1. Built-in Request Filters

**AddRequestHeader**
```yaml
filters:
  - AddRequestHeader=X-Request-ID, ${random.uuid}
  - AddRequestHeader=X-Forwarded-By, API-Gateway
```

**RemoveRequestHeader**
```yaml
filters:
  - RemoveRequestHeader=Cookie  # Remove sensitive headers
```

**SetRequestHeader**
```yaml
filters:
  - SetRequestHeader=X-API-Version, 2.0  # Override if exists
```

**AddRequestParameter**
```yaml
filters:
  - AddRequestParameter=source, gateway
```

**StripPrefix**
```yaml
filters:
  - StripPrefix=2  # /api/users/123 -> /123
```

**PrefixPath**
```yaml
filters:
  - PrefixPath=/api  # /users -> /api/users
```

**RewritePath**
```yaml
filters:
  - RewritePath=/api/(?<segment>.*), /$\{segment}
  # /api/users/123 -> /users/123
```

### 2. Built-in Response Filters

**AddResponseHeader**
```yaml
filters:
  - AddResponseHeader=X-Response-Time, ${responseTime}
  - AddResponseHeader=X-RateLimit-Remaining, ${remaining}
```

**RemoveResponseHeader**
```yaml
filters:
  - RemoveResponseHeader=Server  # Hide server info
```

**SetStatus**
```yaml
filters:
  - SetStatus=404  # Force specific status code
```

**DedupeResponseHeader**
```yaml
filters:
  - DedupeResponseHeader=Access-Control-Allow-Origin RETAIN_UNIQUE
```

### 3. Retry Filter

```yaml
filters:
  - name: Retry
    args:
      retries: 3
      statuses: BAD_GATEWAY, SERVICE_UNAVAILABLE
      methods: GET, POST
      backoff:
        firstBackoff: 100ms
        maxBackoff: 500ms
        factor: 2
        basedOnPreviousValue: true
```

### 4. Request Size Filter

```yaml
filters:
  - name: RequestSize
    args:
      maxSize: 5MB  # Reject requests larger than 5MB
```

### 5. Modify Request/Response Body

**Java Configuration:**
```java
.route("modify-body-route", r -> r
        .path("/api/data/**")
        .filters(f -> f
                // Modify Request Body
                .modifyRequestBody(
                        String.class, 
                        String.class,
                        (exchange, body) -> {
                            // Transform request
                            String modified = body.replace("foo", "bar");
                            return Mono.just(modified);
                        }
                )
                // Modify Response Body
                .modifyResponseBody(
                        String.class, 
                        String.class,
                        (exchange, body) -> {
                            // Transform response
                            String modified = body.toUpperCase();
                            return Mono.just(modified);
                        }
                )
        )
        .uri("lb://data-service")
)
```

### 6. Custom Global Filter

**LoggingGlobalFilter.java**
```java
package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingGlobalFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        
        logger.info("Request: {} {}", method, path);
        
        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            int statusCode = exchange.getResponse().getStatusCode().value();
            logger.info("Response: {} {} - Status: {} - Duration: {}ms",
                    method, path, statusCode, duration);
        }));
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;  // Execute first
    }
}
```

### 7. Custom Gateway Filter

**AuthenticationGatewayFilterFactory.java**
```java
package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationGatewayFilterFactory 
        extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {
    
    public AuthenticationGatewayFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Check for Authorization header
            if (!request.getHeaders().containsKey("Authorization")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            
            String token = request.getHeaders()
                    .getFirst("Authorization");
            
            // Validate token (simplified)
            if (!isValidToken(token)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            
            // Add user info to request
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-ID", extractUserId(token))
                    .build();
            
            return chain.filter(exchange.mutate()
                    .request(modifiedRequest)
                    .build());
        };
    }
    
    private boolean isValidToken(String token) {
        // Implement token validation logic
        return token != null && token.startsWith("Bearer ");
    }
    
    private String extractUserId(String token) {
        // Extract user ID from token
        return "user123";
    }
    
    public static class Config {
        // Configuration properties
    }
}
```

**Usage:**
```yaml
filters:
  - Authentication
```

## Rate Limiting

### 1. Redis-Based Rate Limiting

**application.yml**
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    
  cloud:
    gateway:
      routes:
        - id: rate-limited-route
          uri: lb://api-service
          predicates:
            - Path=/api/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter:
                  replenishRate: 10      # Tokens per second
                  burstCapacity: 20       # Maximum tokens
                  requestedTokens: 1      # Tokens per request
```

### 2. Custom Key Resolver

**UserKeyResolver.java**
```java
package com.example.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {
    
    /**
     * Rate limit by User ID (from header)
     */
    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest()
                        .getHeaders()
                        .getFirst("X-User-ID")
        );
    }
    
    /**
     * Rate limit by IP Address
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest()
                        .getRemoteAddress()
                        .getAddress()
                        .getHostAddress()
        );
    }
    
    /**
     * Rate limit by API Key
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest()
                        .getHeaders()
                        .getFirst("X-API-Key")
        );
    }
    
    /**
     * Rate limit by combination of user and path
     */
    @Bean
    public KeyResolver compositeKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-User-ID");
            String path = exchange.getRequest()
                    .getPath()
                    .value();
            
            return Mono.just(userId + ":" + path);
        };
    }
}
```

### 3. Custom Rate Limiter

**TieredRateLimiter.java**
```java
package com.example.gateway.ratelimiter;

import org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class TieredRateLimiter extends AbstractRateLimiter<TieredRateLimiter.Config> {
    
    private final Map<String, UserTier> userTiers = new HashMap<>();
    
    public TieredRateLimiter() {
        super(Config.class, "tiered-rate-limiter", null);
        
        // Initialize user tiers
        userTiers.put("free", new UserTier(10, 20));
        userTiers.put("premium", new UserTier(100, 200));
        userTiers.put("enterprise", new UserTier(1000, 2000));
    }
    
    @Override
    public Mono<Response> isAllowed(String routeId, String id) {
        Config config = getConfig().get(routeId);
        String userTier = getUserTier(id);
        UserTier tier = userTiers.getOrDefault(userTier, userTiers.get("free"));
        
        // Check rate limit based on tier
        boolean allowed = checkRateLimit(id, tier);
        
        return Mono.just(new Response(allowed, getHeaders(tier, allowed)));
    }
    
    private String getUserTier(String userId) {
        // Logic to determine user tier
        return "free";  // Simplified
    }
    
    private boolean checkRateLimit(String userId, UserTier tier) {
        // Implement rate limiting logic
        return true;  // Simplified
    }
    
    private Map<String, String> getHeaders(UserTier tier, boolean allowed) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-RateLimit-Replenish-Rate", 
                   String.valueOf(tier.getReplenishRate()));
        headers.put("X-RateLimit-Burst-Capacity", 
                   String.valueOf(tier.getBurstCapacity()));
        headers.put("X-RateLimit-Remaining", 
                   allowed ? "10" : "0");
        return headers;
    }
    
    public static class Config {
        // Configuration properties
    }
    
    private static class UserTier {
        private final int replenishRate;
        private final int burstCapacity;
        
        public UserTier(int replenishRate, int burstCapacity) {
            this.replenishRate = replenishRate;
            this.burstCapacity = burstCapacity;
        }
        
        public int getReplenishRate() {
            return replenishRate;
        }
        
        public int getBurstCapacity() {
            return burstCapacity;
        }
    }
}
```

## Circuit Breaker Integration

### Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: circuit-breaker-route
          uri: lb://backend-service
          predicates:
            - Path=/api/data/**
          filters:
            - name: CircuitBreaker
              args:
                name: backendCircuitBreaker
                fallbackUri: forward:/fallback/data
                
resilience4j:
  circuitbreaker:
    instances:
      backendCircuitBreaker:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        waitDurationInOpenState: 10s
        failureRateThreshold: 50
        slowCallDurationThreshold: 2s
        slowCallRateThreshold: 50
```

### Fallback Controller

**FallbackController.java**
```java
package com.example.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> dataFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "message", "Service temporarily unavailable",
                        "timestamp", LocalDateTime.now(),
                        "fallback", true
                ));
    }
    
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> ordersFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "message", "Order service is down. Please try again later.",
                        "timestamp", LocalDateTime.now(),
                        "fallback", true,
                        "supportContact", "support@example.com"
                ));
    }
}
```

## Security Integration

### 1. JWT Authentication Filter

**JwtAuthenticationFilter.java**
```java
package com.example.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter 
        extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    public JwtAuthenticationFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Extract JWT token
            String token = extractToken(request);
            
            if (token == null) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            
            try {
                // Validate and parse token
                Claims claims = validateToken(token);
                
                // Add claims to request headers
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-ID", claims.getSubject())
                        .header("X-User-Role", claims.get("role", String.class))
                        .header("X-User-Email", claims.get("email", String.class))
                        .build();
                
                return chain.filter(exchange.mutate()
                        .request(modifiedRequest)
                        .build());
                
            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        };
    }
    
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public static class Config {
        // Configuration properties
    }
}
```

### 2. CORS Configuration

**CorsConfig.java**
```java
package com.example.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {
    
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        
        // Allowed origins
        corsConfiguration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:4200",
                "https://app.example.com"
        ));
        
        // Allowed methods
        corsConfiguration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Allowed headers
        corsConfiguration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "X-API-Version"
        ));
        
        // Exposed headers
        corsConfiguration.setExposedHeaders(Arrays.asList(
                "X-Total-Count",
                "X-Response-Time"
        ));
        
        // Allow credentials
        corsConfiguration.setAllowCredentials(true);
        
        // Max age
        corsConfiguration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = 
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        
        return new CorsWebFilter(source);
    }
}
```

## Complete Example

### Project Structure
```
api-gateway/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/gateway/
│   │   │       ├── ApiGatewayApplication.java
│   │   │       ├── config/
│   │   │       │   ├── GatewayConfig.java
│   │   │       │   ├── CorsConfig.java
│   │   │       │   └── RateLimitConfig.java
│   │   │       ├── filter/
│   │   │       │   ├── LoggingGlobalFilter.java
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   └── AuthenticationGatewayFilterFactory.java
│   │   │       ├── controller/
│   │   │       │   ├── FallbackController.java
│   │   │       │   └── RouteController.java
│   │   │       └── service/
│   │   │           └── DynamicRouteService.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-prod.yml
│   └── test/
│       └── java/
│           └── com/example/gateway/
│               └── ApiGatewayApplicationTests.java
├── pom.xml
└── README.md
```

### Complete application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
    
  # Redis Configuration
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    timeout: 2000ms
    
  # Cloud Gateway Configuration
  cloud:
    gateway:
      # Global CORS Configuration
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "http://localhost:3000,http://localhost:4200"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600
            
      # Discovery Locator
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
          
      # Route Definitions
      routes:
        # User Service
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - name: CircuitBreaker
              args:
                name: userCircuitBreaker
                fallbackUri: forward:/fallback/users
            - name: RequestRateLimiter
              args:
                redis-rate-limiter:
                  replenishRate: 10
                  burstCapacity: 20
                  requestedTokens: 1
            - JwtAuthentication
            
        # Order Service
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=2
            - name: CircuitBreaker
              args:
                name: orderCircuitBreaker
                fallbackUri: forward:/fallback/orders
            - name: RequestRateLimiter
              args:
                redis-rate-limiter:
                  replenishRate: 20
                  burstCapacity: 40
            - JwtAuthentication
            
        # Product Service
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
          filters:
            - StripPrefix=2
            - name: RequestRateLimiter
              args:
                redis-rate-limiter:
                  replenishRate: 50
                  burstCapacity: 100
                  
        # Authentication Service (No JWT filter)
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=2

# Eureka Configuration
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER:http://localhost:8761/eureka/}
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30

# Resilience4j Configuration
resilience4j:
  circuitbreaker:
    instances:
      userCircuitBreaker:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        waitDurationInOpenState: 10s
        failureRateThreshold: 50
        slowCallDurationThreshold: 2s
        slowCallRateThreshold: 50
      orderCircuitBreaker:
        slidingWindowSize: 15
        minimumNumberOfCalls: 8
        waitDurationInOpenState: 15s
        failureRateThreshold: 60
        
  timelimiter:
    instances:
      userCircuitBreaker:
        timeoutDuration: 3s
      orderCircuitBreaker:
        timeoutDuration: 5s

# Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: health, metrics, gateway, prometheus
  endpoint:
    gateway:
      enabled: true
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

# JWT Configuration
jwt:
  secret: ${JWT_SECRET:your-secret-key-change-in-production}
  expiration: 86400000  # 24 hours

# Logging
logging:
  level:
    root: INFO
    com.example.gateway: DEBUG
    org.springframework.cloud.gateway: DEBUG
    reactor.netty: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

## Best Practices

### 1. Security Best Practices

**✅ DO:**
- Always validate JWT tokens at the gateway
- Use HTTPS in production
- Implement rate limiting to prevent abuse
- Remove sensitive headers before forwarding
- Use API keys for service-to-service communication
- Implement CORS properly
- Log security events

**❌ DON'T:**
- Don't store secrets in configuration files
- Don't expose internal service URLs
- Don't bypass authentication for convenience
- Don't use weak JWT secrets
- Don't ignore security headers

### 2. Performance Best Practices

**✅ DO:**
- Use WebFlux (reactive) for better performance
- Enable connection pooling
- Configure appropriate timeouts
- Use circuit breakers to fail fast
- Cache frequently accessed data
- Monitor gateway performance
- Use load balancing

**❌ DON'T:**
- Don't use blocking I/O operations
- Don't configure infinite timeouts
- Don't forget to tune thread pools
- Don't ignore memory management

### 3. Routing Best Practices

**✅ DO:**
- Use meaningful route IDs
- Keep routes simple and focused
- Use predicates to match requests accurately
- Document route configurations
- Version your APIs
- Use path-based routing when possible

**❌ DON'T:**
- Don't create overlapping routes
- Don't hardcode service URLs
- Don't use complex regex patterns unnecessarily
- Don't forget to handle edge cases

### 4. Monitoring Best Practices

**✅ DO:**
- Enable Actuator endpoints
- Export metrics to Prometheus
- Log all requests and responses
- Track response times
- Monitor circuit breaker status
- Set up alerts for failures

**❌ DON'T:**
- Don't log sensitive information
- Don't ignore error patterns
- Don't overlook slow queries

### 5. Configuration Best Practices

**✅ DO:**
- Use Spring profiles for different environments
- Externalize configuration
- Use environment variables for secrets
- Document configuration properties
- Validate configuration on startup

**❌ DON'T:**
- Don't commit secrets to version control
- Don't use default values in production
- Don't ignore configuration errors

## Complete Example

Let's build a complete API Gateway with authentication, rate limiting, and circuit breaker:

**TestController.java** (for testing)
```java
package com.example.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {
    
    @GetMapping("/gateway")
    public Map<String, Object> testGateway(
            @RequestHeader(value = "X-User-ID", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return Map.of(
                "message", "Gateway is working!",
                "userId", userId != null ? userId : "Not authenticated",
                "role", role != null ? role : "No role",
                "timestamp", System.currentTimeMillis()
        );
    }
}
```

**Testing the Gateway:**

```bash
# Test without authentication (should fail)
curl -X GET http://localhost:8080/api/users/123

# Test with JWT token
curl -X GET http://localhost:8080/api/users/123 \
  -H "Authorization: Bearer <your-jwt-token>"

# Test rate limiting
for i in {1..30}; do
  curl -X GET http://localhost:8080/api/products \
    -H "X-User-ID: user123"
done

# Test dynamic route creation
curl -X POST http://localhost:8080/admin/routes \
  -H "Content-Type: application/json" \
  -d '{
    "id": "new-service",
    "uri": "lb://new-service",
    "predicates": [
      {
        "name": "Path",
        "args": {
          "pattern": "/api/new/**"
        }
      }
    ],
    "filters": [
      {
        "name": "StripPrefix",
        "args": {
          "parts": 2
        }
      }
    ]
  }'

# View Gateway routes
curl -X GET http://localhost:8080/actuator/gateway/routes
```

## Exercises

### Exercise 1: Basic API Gateway Setup
**Task:** Create a simple API Gateway that routes requests to two microservices (user-service and order-service).

**Requirements:**
1. Set up Spring Cloud Gateway project
2. Configure routes for both services
3. Enable Eureka discovery
4. Test routing with sample requests

**Solution Hints:**
- Use `lb://` prefix for load-balanced URIs
- Configure StripPrefix filter to remove path segments
- Enable discovery locator for automatic route creation

### Exercise 2: Custom Global Filter
**Task:** Implement a custom global filter that adds a correlation ID to each request and logs request/response details.

**Requirements:**
1. Create CustomCorrelationFilter class
2. Generate unique correlation ID for each request
3. Add correlation ID to request and response headers
4. Log request method, path, and duration
5. Implement proper ordering

**Solution Hints:**
- Extend GlobalFilter and Ordered interfaces
- Use UUID.randomUUID() for correlation ID
- Capture start time before calling chain.filter()
- Use Mono.fromRunnable() for post-processing

### Exercise 3: JWT Authentication
**Task:** Implement JWT-based authentication at the gateway level.

**Requirements:**
1. Create JwtAuthenticationFilter
2. Validate JWT tokens from Authorization header
3. Extract user information and add to request headers
4. Return 401 for missing/invalid tokens
5. Bypass authentication for specific paths (/auth/**, /public/**)

**Solution Hints:**
- Use io.jsonwebtoken library for JWT parsing
- Create separate filter factory for flexibility
- Add X-User-ID and X-User-Role headers
- Consider using Spring Security for production

### Exercise 4: Rate Limiting
**Task:** Implement user-based rate limiting with Redis.

**Requirements:**
1. Configure Redis connection
2. Set up RequestRateLimiter filter
3. Create custom KeyResolver based on user ID
4. Configure different rate limits for different user tiers
5. Return appropriate headers (X-RateLimit-Remaining)

**Solution Hints:**
- Use spring-boot-starter-data-redis-reactive
- Implement KeyResolver bean
- Configure replenishRate and burstCapacity
- Test with multiple concurrent requests

### Exercise 5: Circuit Breaker and Fallback
**Task:** Implement circuit breaker pattern with Resilience4j and create fallback responses.

**Requirements:**
1. Add Resilience4j dependencies
2. Configure circuit breaker for a service route
3. Create fallback controller
4. Configure sliding window and failure threshold
5. Test circuit breaker behavior

**Solution Hints:**
- Use CircuitBreaker filter with fallbackUri
- Configure resilience4j.circuitbreaker properties
- Create @RestController with fallback endpoints
- Simulate service failure for testing

## Key Takeaways

1. **API Gateway is Essential** for microservices architectures
   - Single entry point for all clients
   - Centralized security and monitoring
   - Service abstraction and routing

2. **Spring Cloud Gateway** is the modern choice
   - Built on reactive stack (WebFlux)
   - High performance and scalability
   - Rich set of built-in features

3. **Predicates and Filters** provide flexibility
   - Predicates match requests
   - Filters modify requests/responses
   - Can be combined and customized

4. **Security at Gateway Level** is crucial
   - JWT authentication and validation
   - Rate limiting to prevent abuse
   - CORS configuration
   - SSL termination

5. **Circuit Breaker Integration** ensures resilience
   - Fail fast when services are down
   - Provide fallback responses
   - Prevent cascading failures

6. **Dynamic Routing** enables flexibility
   - Add/update/delete routes at runtime
   - Support for A/B testing
   - Feature flags and canary deployments

7. **Monitoring and Logging** are essential
   - Track all requests through gateway
   - Monitor performance metrics
   - Debug issues quickly

## Resources

### Official Documentation
- [Spring Cloud Gateway Reference](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Resilience4j Documentation](https://resilience4j.readme.io/)

### Tutorials and Guides
- [Spring Cloud Gateway Tutorial](https://spring.io/guides/gs/gateway/)
- [Microservices Patterns](https://microservices.io/patterns/apigateway.html)
- [API Gateway Pattern](https://docs.microsoft.com/en-us/azure/architecture/microservices/design/gateway)

### Books
- *Spring Microservices in Action* by John Carnell and Illary Huaylupo Sánchez
- *Microservices Patterns* by Chris Richardson
- *Building Microservices* by Sam Newman

### Videos
- [Spring Cloud Gateway Deep Dive](https://www.youtube.com/watch?v=TwVtlNX-2Hs)
- [Microservices with Spring Boot and Spring Cloud](https://www.youtube.com/watch?v=GtOkN4XrH1w)

### Tools
- [Postman](https://www.postman.com/) - API testing
- [Redis](https://redis.io/) - Rate limiting backend
- [Prometheus](https://prometheus.io/) - Metrics collection
- [Grafana](https://grafana.com/) - Metrics visualization

---

**Next Steps:**
- Complete the exercises above
- Experiment with custom predicates and filters
- Integrate with your microservices
- Monitor gateway performance
- Prepare for Day 15: Load Balancing

---
[<< Previous: Day 13](../Day13_Eureka_Discovery/README.md) | [Back to Main](../README.md) | [Next: Day 15 >>](../Day15_Load_Balancing/README.md)
