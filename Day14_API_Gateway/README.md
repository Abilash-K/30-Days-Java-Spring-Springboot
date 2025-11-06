# Day 14: API Gateway with Spring Cloud Gateway

## 📋 Table of Contents
- [Introduction](#introduction)
- [What is an API Gateway?](#what-is-an-api-gateway)
- [API Gateway Pattern](#api-gateway-pattern)
- [Spring Cloud Gateway vs Zuul](#spring-cloud-gateway-vs-zuul)
- [Getting Started](#getting-started)
- [Route Configuration](#route-configuration)
- [Predicates](#predicates)
- [Filters](#filters)
- [Global Filters](#global-filters)
- [Custom Filters](#custom-filters)
- [Rate Limiting](#rate-limiting)
- [Circuit Breaker Integration](#circuit-breaker-integration)
- [Load Balancing](#load-balancing)
- [Request/Response Transformation](#requestresponse-transformation)
- [Security](#security)
- [Monitoring](#monitoring)
- [Best Practices](#best-practices)
- [Complete Example](#complete-example)
- [Exercises](#exercises)
- [Key Takeaways](#key-takeaways)
- [Resources](#resources)

## Introduction

Welcome to Day 14! Today we'll explore **Spring Cloud Gateway**, a powerful API Gateway built on Spring Framework 5, Project Reactor, and Spring Boot 2.

### What You'll Learn
- Understanding API Gateway pattern
- Setting up Spring Cloud Gateway
- Configuring routes and predicates
- Implementing filters (pre and post)
- Rate limiting and throttling
- Request/Response transformation
- Security and monitoring

### Why API Gateway Matters
- ✅ **Single Entry Point**: One URL for all microservices
- ✅ **Cross-Cutting Concerns**: Authentication, logging, rate limiting
- ✅ **Routing**: Direct requests to appropriate services
- ✅ **Protocol Translation**: HTTP to gRPC, REST to SOAP
- ✅ **Load Balancing**: Distribute traffic across instances

## What is an API Gateway?

An API Gateway is a server that acts as an entry point for a collection of microservices. It sits between clients and services, routing requests, enforcing policies, and aggregating results.

### Without API Gateway

```
┌─────────┐
│ Client  │
└────┬────┘
     │
     ├──────────────────────────────────┐
     │                                  │
┌────▼────────┐     ┌──────────────┐   │
│ User        │     │ Product      │   │
│ Service     │     │ Service      │   │
│ :8081       │     │ :8082        │   │
└─────────────┘     └──────────────┘   │
                                       │
     ┌─────────────────────────────────┘
     │
┌────▼────────┐     ┌──────────────┐
│ Order       │     │ Payment      │
│ Service     │     │ Service      │
│ :8083       │     │ :8084        │
└─────────────┘     └──────────────┘

Problems:
❌ Client knows all service URLs
❌ No centralized security
❌ Cross-cutting concerns in each service
❌ Hard to manage
```

### With API Gateway

```
┌─────────┐
│ Client  │
└────┬────┘
     │
     │ Single Entry Point
     │ http://api-gateway:8080
     │
┌────▼─────────────────────────────────┐
│         API Gateway                   │
│  ┌─────────────────────────────────┐ │
│  │ Routing | Security | Rate Limit │ │
│  │ Logging | Caching  | Transform  │ │
│  └─────────────────────────────────┘ │
└──────┬──────┬──────┬──────┬──────────┘
       │      │      │      │
   ┌───▼──┐ ┌─▼───┐ ┌▼────┐ ┌▼─────┐
   │User  │ │Prod │ │Order│ │Pay   │
   │:8081 │ │:8082│ │:8083│ │:8084 │
   └──────┘ └─────┘ └─────┘ └──────┘

Benefits:
✅ Single entry point
✅ Centralized cross-cutting concerns
✅ Protocol translation
✅ Service discovery integration
```

## API Gateway Pattern

### Responsibilities

**1. Request Routing:**
- Route requests to appropriate microservice
- Path-based routing
- Header-based routing
- Query parameter routing

**2. Authentication & Authorization:**
- Verify JWT tokens
- OAuth2 integration
- API key validation
- User authentication

**3. Rate Limiting & Throttling:**
- Limit requests per user/IP
- Prevent DDoS attacks
- Quota management

**4. Load Balancing:**
- Distribute requests across instances
- Health-based routing
- Failover support

**5. Request/Response Transformation:**
- Modify headers
- Transform request/response bodies
- Protocol conversion

**6. Caching:**
- Cache responses
- Reduce backend load
- Improve performance

**7. Logging & Monitoring:**
- Centralized logging
- Request tracking
- Performance metrics

**8. Circuit Breaking:**
- Fail fast on service failures
- Fallback responses
- Service health monitoring

## Spring Cloud Gateway vs Zuul

### Zuul (Netflix)

**Architecture:**
- Servlet-based (blocking I/O)
- Synchronous processing
- Netflix OSS (maintenance mode)

**Pros:**
- Mature and stable
- Large community
- Many existing integrations

**Cons:**
- Blocking I/O (not as scalable)
- No longer actively developed by Netflix
- Limited to Servlet containers

### Spring Cloud Gateway

**Architecture:**
- Reactive (non-blocking I/O)
- Built on Spring WebFlux
- Project Reactor
- Async processing

**Pros:**
- Non-blocking, more scalable
- Modern reactive architecture
- Actively developed
- Better performance
- Predicates and filters

**Cons:**
- Newer (less mature than Zuul)
- Requires understanding of reactive programming

### Comparison

| Feature | Zuul | Spring Cloud Gateway |
|---------|------|----------------------|
| Architecture | Servlet (blocking) | Reactive (non-blocking) |
| Performance | Good | Excellent |
| Scalability | Medium | High |
| Maintenance | Limited | Active |
| Learning Curve | Easy | Medium |
| Recommended | No | Yes |

## Getting Started

### 1. Create Gateway Project

**Maven (pom.xml):**
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
        <version>3.1.0</version>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>api-gateway</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2022.0.3</spring-cloud.version>
    </properties>
    
    <dependencies>
        <!-- Spring Cloud Gateway -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        
        <!-- Eureka Client for Service Discovery -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        
        <!-- Actuator for Monitoring -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
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
</project>
```

### 2. Main Application

```java
package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ApiGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

### 3. Basic Configuration

**application.yml:**
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
        
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## Route Configuration

### Basic Route Structure

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: route-id              # Unique route identifier
          uri: http://localhost:8081  # Destination URI
          predicates:               # Conditions to match
            - Path=/api/**
          filters:                  # Filters to apply
            - StripPrefix=1
```

### URI Types

**1. Direct URI:**
```yaml
uri: http://localhost:8081
```

**2. Load-Balanced URI (with Eureka):**
```yaml
uri: lb://user-service  # lb:// prefix for load balancing
```

**3. WebSocket URI:**
```yaml
uri: ws://localhost:8081
```

### Multiple Routes Example

```yaml
spring:
  cloud:
    gateway:
      routes:
        # User Service
        - id: user-service-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - RewritePath=/api/users/(?<segment>.*), /${segment}
        
        # Product Service
        - id: product-service-route
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
          filters:
            - StripPrefix=1
        
        # Order Service
        - id: order-service-route
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
            - Method=GET,POST
          filters:
            - AddRequestHeader=X-Gateway, ApiGateway
            - AddResponseHeader=X-Response-From, Gateway
```

## Predicates

Predicates determine whether a route should be matched. Spring Cloud Gateway includes many built-in predicates.

### Path Predicate

```yaml
predicates:
  - Path=/api/users/**  # Matches any path starting with /api/users/
```

### Method Predicate

```yaml
predicates:
  - Method=GET,POST  # Only GET and POST requests
```

### Header Predicate

```yaml
predicates:
  - Header=X-Request-Id, \d+  # Header must match regex
```

### Query Predicate

```yaml
predicates:
  - Query=userId, \d+  # Query parameter must exist and match regex
```

### Host Predicate

```yaml
predicates:
  - Host=**.example.com  # Host must match pattern
```

### Cookie Predicate

```yaml
predicates:
  - Cookie=session, abc.*  # Cookie must exist and match regex
```

### DateTime Predicates

**After:**
```yaml
predicates:
  - After=2024-01-01T00:00:00+00:00[UTC]  # Only after this date
```

**Before:**
```yaml
predicates:
  - Before=2024-12-31T23:59:59+00:00[UTC]  # Only before this date
```

**Between:**
```yaml
predicates:
  - Between=2024-01-01T00:00:00+00:00[UTC], 2024-12-31T23:59:59+00:00[UTC]
```

### RemoteAddr Predicate

```yaml
predicates:
  - RemoteAddr=192.168.1.1/24  # IP address must match CIDR
```

### Weight Predicate (A/B Testing)

```yaml
routes:
  - id: service-v1
    uri: lb://service-v1
    predicates:
      - Path=/api/**
      - Weight=group1, 8  # 80% traffic
  
  - id: service-v2
    uri: lb://service-v2
    predicates:
      - Path=/api/**
      - Weight=group1, 2  # 20% traffic
```

### Combining Predicates

```yaml
predicates:
  - Path=/api/users/**
  - Method=GET
  - Header=X-Request-Id
  - Query=version, v2
# All predicates must match (AND logic)
```

## Filters

Filters modify incoming requests and outgoing responses.

### Built-in Filters

**1. Add Headers:**
```yaml
filters:
  - AddRequestHeader=X-Request-Source, Gateway
  - AddResponseHeader=X-Response-Time, ${timestamp}
```

**2. Remove Headers:**
```yaml
filters:
  - RemoveRequestHeader=X-Internal-Header
  - RemoveResponseHeader=X-Sensitive-Data
```

**3. Rewrite Path:**
```yaml
filters:
  - RewritePath=/api/(?<segment>.*), /${segment}
  # /api/users/1 → /users/1
```

**4. Strip Prefix:**
```yaml
filters:
  - StripPrefix=1
  # /api/users/1 → /users/1 (removes first segment)
  - StripPrefix=2
  # /api/v1/users/1 → /users/1 (removes first two segments)
```

**5. Set Path:**
```yaml
filters:
  - SetPath=/api/v2/{segment}
```

**6. Set Status:**
```yaml
filters:
  - SetStatus=401  # Set HTTP status code
```

**7. Redirect:**
```yaml
filters:
  - RedirectTo=302, https://example.com
```

**8. Retry:**
```yaml
filters:
  - name: Retry
    args:
      retries: 3
      statuses: BAD_GATEWAY,GATEWAY_TIMEOUT
      methods: GET,POST
      backoff:
        firstBackoff: 10ms
        maxBackoff: 50ms
        factor: 2
        basedOnPreviousValue: false
```

**9. Request Rate Limiter:**
```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 10  # Tokens per second
      redis-rate-limiter.burstCapacity: 20  # Max burst size
      redis-rate-limiter.requestedTokens: 1  # Tokens per request
```

**10. Circuit Breaker:**
```yaml
filters:
  - name: CircuitBreaker
    args:
      name: myCircuitBreaker
      fallbackUri: forward:/fallback
```

### Filter Chain Order

Filters are applied in this order:
1. Global filters (ordered)
2. Route-specific filters (defined order)

```yaml
spring:
  cloud:
    gateway:
      default-filters:  # Applied to all routes
        - AddRequestHeader=X-Gateway, Global
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:  # Route-specific filters
            - AddRequestHeader=X-Service, UserService
            - StripPrefix=1
```

## Global Filters

Global filters apply to all routes.

### Configuration-Based Global Filters

```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - AddRequestHeader=X-Gateway-Request, true
        - AddResponseHeader=X-Gateway-Response, true
        - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin
```

### Custom Global Filter

```java
package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant startTime = Instant.now();
        String path = exchange.getRequest().getPath().toString();
        String method = exchange.getRequest().getMethod().toString();
        
        System.out.println("Request: " + method + " " + path);
        
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    Duration duration = Duration.between(startTime, Instant.now());
                    System.out.println("Response: " + method + " " + path + 
                            " - " + exchange.getResponse().getStatusCode() + 
                            " (" + duration.toMillis() + "ms)");
                }));
    }
    
    @Override
    public int getOrder() {
        return -1;  // Higher priority (lower number runs first)
    }
}
```

### Authentication Global Filter

```java
package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(request.getPath().toString())) {
            return chain.filter(exchange);
        }
        
        // Check for Authorization header
        if (!request.getHeaders().containsKey("Authorization")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        String token = request.getHeaders().getFirst("Authorization");
        
        // Validate token (simplified)
        if (!isValidToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        // Add user info to request headers
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", extractUserId(token))
                .build();
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
    
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/public") || 
               path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register");
    }
    
    private boolean isValidToken(String token) {
        // Implement token validation logic
        return token != null && token.startsWith("Bearer ");
    }
    
    private String extractUserId(String token) {
        // Extract user ID from token
        return "user123";
    }
    
    @Override
    public int getOrder() {
        return -100;  // Run before other filters
    }
}
```

## Custom Filters

### Gateway Filter Factory

```java
package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomHeaderGatewayFilterFactory 
        extends AbstractGatewayFilterFactory<CustomHeaderGatewayFilterFactory.Config> {
    
    public CustomHeaderGatewayFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            exchange.getRequest().mutate()
                    .header(config.getHeaderName(), config.getHeaderValue())
                    .build();
            
            return chain.filter(exchange);
        };
    }
    
    public static class Config {
        private String headerName;
        private String headerValue;
        
        // Getters and setters
        public String getHeaderName() {
            return headerName;
        }
        
        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
        
        public String getHeaderValue() {
            return headerValue;
        }
        
        public void setHeaderValue(String headerValue) {
            this.headerValue = headerValue;
        }
    }
}
```

**Usage in configuration:**
```yaml
filters:
  - name: CustomHeader
    args:
      headerName: X-Custom-Header
      headerValue: CustomValue
```

### Request Time Filter

```java
package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RequestTimeGatewayFilterFactory 
        extends AbstractGatewayFilterFactory<RequestTimeGatewayFilterFactory.Config> {
    
    private static final String REQUEST_TIME_BEGIN = "requestTimeBegin";
    
    public RequestTimeGatewayFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            exchange.getAttributes().put(REQUEST_TIME_BEGIN, System.currentTimeMillis());
            
            return chain.filter(exchange).then(
                    Mono.fromRunnable(() -> {
                        Long startTime = exchange.getAttribute(REQUEST_TIME_BEGIN);
                        if (startTime != null) {
                            long duration = System.currentTimeMillis() - startTime;
                            
                            if (duration > config.getThreshold()) {
                                System.out.println("⚠️ Slow request detected: " + 
                                        exchange.getRequest().getURI() + 
                                        " took " + duration + "ms");
                            }
                        }
                    })
            );
        };
    }
    
    public static class Config {
        private long threshold = 1000; // Default 1 second
        
        public long getThreshold() {
            return threshold;
        }
        
        public void setThreshold(long threshold) {
            this.threshold = threshold;
        }
    }
}
```

## Rate Limiting

### Redis-Based Rate Limiting

**1. Add Redis dependency:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

**2. Configure Redis:**
```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

**3. Configure Rate Limiter:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: rate-limited-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10  # 10 requests per second
                redis-rate-limiter.burstCapacity: 20  # Max 20 requests in burst
                redis-rate-limiter.requestedTokens: 1
                key-resolver: "#{@userKeyResolver}"
```

### Custom Key Resolver

```java
package com.example.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {
    
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getQueryParams().getFirst("userId")
        );
    }
    
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }
    
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getHeaders().getFirst("X-API-Key")
        );
    }
}
```

## Circuit Breaker Integration

### Add Resilience4j Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
</dependency>
```

### Configure Circuit Breaker

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-cb
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: CircuitBreaker
              args:
                name: userServiceCircuitBreaker
                fallbackUri: forward:/fallback/users

resilience4j:
  circuitbreaker:
    instances:
      userServiceCircuitBreaker:
        slidingWindowSize: 10
        permittedNumberOfCallsInHalfOpenState: 3
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
        registerHealthIndicator: true
```

### Fallback Controller

```java
package com.example.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    @GetMapping("/users")
    public Map<String, Object> userFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User service is temporarily unavailable");
        response.put("status", "fallback");
        return response;
    }
}
```

## Load Balancing

### Client-Side Load Balancing with Eureka

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service  # lb:// enables load balancing
          predicates:
            - Path=/api/users/**
```

### Custom Load Balancer Configuration

```java
package com.example.gateway.config;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class LoadBalancerConfig {
    
    @Bean
    public ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {
        
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new RandomLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class),
                name
        );
    }
}
```

## Request/Response Transformation

### Modify Request Body

```java
package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class RequestTransformFilter 
        extends AbstractGatewayFilterFactory<RequestTransformFilter.Config> {
    
    private final ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;
    
    public RequestTransformFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter) {
        super(Config.class);
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Add transformation logic
            return modifyRequestBodyFilter.apply(c -> 
                c.setRewriteFunction(String.class, String.class, (serverWebExchange, body) -> {
                    // Transform request body
                    String transformedBody = body.toUpperCase(); // Example transformation
                    return Mono.just(transformedBody);
                })
            ).filter(exchange, chain);
        };
    }
    
    public static class Config {
        // Configuration properties
    }
}
```

### Modify Response Body

```java
package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ResponseTransformFilter 
        extends AbstractGatewayFilterFactory<ResponseTransformFilter.Config> {
    
    private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilter;
    
    public ResponseTransformFilter(ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilter) {
        super(Config.class);
        this.modifyResponseBodyFilter = modifyResponseBodyFilter;
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return modifyResponseBodyFilter.apply(c ->
                c.setRewriteFunction(String.class, String.class, (serverWebExchange, body) -> {
                    // Add timestamp to response
                    String modifiedBody = "{\"timestamp\":" + System.currentTimeMillis() + 
                            ",\"data\":" + body + "}";
                    return Mono.just(modifiedBody);
                })
            ).filter(exchange, chain);
        };
    }
    
    public static class Config {
        // Configuration properties
    }
}
```

## Security

### CORS Configuration

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "http://localhost:3000"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600
```

### JWT Validation Filter

```java
package com.example.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    
    private static final String SECRET_KEY = "your-secret-key";
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Skip for public endpoints
        if (isPublicPath(request.getPath().toString())) {
            return chain.filter(exchange);
        }
        
        String authHeader = request.getHeaders().getFirst("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        String token = authHeader.substring(7);
        
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
            
            // Add user info to headers
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Roles", claims.get("roles", String.class))
                    .build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
            
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
    
    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/") || path.startsWith("/api/public/");
    }
    
    @Override
    public int getOrder() {
        return -100;
    }
}
```

## Monitoring

### Actuator Endpoints

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,gateway
  endpoint:
    gateway:
      enabled: true
```

**Gateway Actuator Endpoints:**
- `/actuator/gateway/routes` - List all routes
- `/actuator/gateway/routes/{id}` - Get specific route
- `/actuator/gateway/refresh` - Refresh route cache
- `/actuator/gateway/globalfilters` - List global filters
- `/actuator/gateway/routefilters` - List route filters

### Metrics

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
```

**Gateway Metrics:**
- `gateway.requests` - Total requests
- `spring.cloud.gateway.requests` - Request count by route
- Response times
- Error rates

## Best Practices

### 1. Use Service Discovery

```yaml
uri: lb://service-name  # Instead of hardcoded URLs
```

### 2. Implement Circuit Breakers

```yaml
filters:
  - name: CircuitBreaker
    args:
      name: myCircuitBreaker
      fallbackUri: forward:/fallback
```

### 3. Add Request Timeout

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 1000
        response-timeout: 5s
```

### 4. Enable CORS Properly

```yaml
globalcors:
  cors-configurations:
    '[/**]':
      allowedOrigins: "https://yourdomain.com"  # Don't use "*" in production
      allowedMethods: [GET, POST]
```

### 5. Implement Rate Limiting

```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 10
      redis-rate-limiter.burstCapacity: 20
```

### 6. Add Logging

```java
@Component
public class LoggingFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("Request: {} {}", 
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI());
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return -1;
    }
}
```

### 7. Externalize Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: ${USER_SERVICE_URL:lb://user-service}
          predicates:
            - Path=/api/users/**
```

### 8. Monitor Gateway Health

```yaml
management:
  health:
    circuitbreakers:
      enabled: true
```

## Complete Example

### Complete application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  
  cloud:
    gateway:
      # Global CORS configuration
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "http://localhost:3000"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600
      
      # Global filters
      default-filters:
        - name: Retry
          args:
            retries: 3
            statuses: BAD_GATEWAY
      
      # Routes
      routes:
        # User Service
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
            - name: CircuitBreaker
              args:
                name: userServiceCB
                fallbackUri: forward:/fallback/users
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
        
        # Product Service
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
          filters:
            - RewritePath=/api/products/(?<segment>.*), /${segment}
            - AddRequestHeader=X-Request-Source, ApiGateway
            - AddResponseHeader=X-Response-From, ProductService
        
        # Order Service
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
            - Method=GET,POST
          filters:
            - StripPrefix=1
      
      # HTTP client configuration
      httpclient:
        connect-timeout: 1000
        response-timeout: 5s
  
  # Redis for rate limiting
  redis:
    host: localhost
    port: 6379

# Eureka configuration
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

# Resilience4j Circuit Breaker
resilience4j:
  circuitbreaker:
    instances:
      userServiceCB:
        slidingWindowSize: 10
        permittedNumberOfCallsInHalfOpenState: 3
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
        registerHealthIndicator: true

# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,gateway,prometheus
  endpoint:
    gateway:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true

# Logging
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    reactor.netty: INFO
```

## Exercises

### Exercise 1: Basic Gateway Setup
Create an API Gateway that routes to multiple services.

**Requirements:**
1. Set up API Gateway on port 8080
2. Configure routes for user-service and product-service
3. Use path-based routing
4. Test with Postman/cURL

### Exercise 2: Add Filters
Implement custom filters for logging and headers.

**Requirements:**
1. Create logging filter that logs all requests
2. Add request/response headers
3. Implement request timing filter
4. Strip prefix from paths

### Exercise 3: Implement Rate Limiting
Add rate limiting to protect your services.

**Requirements:**
1. Set up Redis
2. Configure rate limiter (10 req/sec)
3. Use IP-based key resolver
4. Test rate limiting behavior

### Exercise 4: Circuit Breaker
Implement circuit breaker with fallback.

**Requirements:**
1. Add Resilience4j dependency
2. Configure circuit breaker for user-service
3. Create fallback controller
4. Test failover behavior

### Exercise 5: Security
Add JWT authentication to the gateway.

**Requirements:**
1. Implement JWT validation filter
2. Add public/private endpoint distinction
3. Pass user info to downstream services
4. Test with valid and invalid tokens

## Key Takeaways

1. **Single Entry Point**
   - API Gateway provides one URL for all services
   - Simplifies client configuration
   - Centralized routing and security

2. **Spring Cloud Gateway is Reactive**
   - Built on Spring WebFlux
   - Non-blocking I/O
   - Better performance than Zuul

3. **Predicates Determine Routing**
   - Multiple predicate types (Path, Method, Header, etc.)
   - Can combine predicates (AND logic)
   - Flexible routing rules

4. **Filters Modify Requests/Responses**
   - Built-in filters for common tasks
   - Custom filters for specific needs
   - Global and route-specific filters

5. **Integration with Service Discovery**
   - Use lb:// URI scheme
   - Automatic load balancing
   - Health-based routing

6. **Cross-Cutting Concerns**
   - Authentication/Authorization
   - Rate limiting
   - Circuit breaking
   - Logging and monitoring

## Resources

### Official Documentation
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Gateway Documentation](https://cloud.spring.io/spring-cloud-gateway/reference/html/)
- [Spring WebFlux](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)

### Tutorials
- [API Gateway Tutorial](https://www.baeldung.com/spring-cloud-gateway)
- [Gateway Filters](https://www.baeldung.com/spring-cloud-gateway-filter-factory)
- [Rate Limiting](https://www.baeldung.com/spring-cloud-gateway-rate-limit-by-client-ip)

### Tools
- [Gateway Actuator](http://localhost:8080/actuator/gateway)
- [Redis](https://redis.io/)
- [Resilience4j](https://resilience4j.readme.io/)

---
[<< Previous: Day 13](../Day13_Eureka_Discovery/README.md) | [Back to Main](../README.md) | [Next: Day 15 >>](../Day15_Load_Balancing/README.md)
