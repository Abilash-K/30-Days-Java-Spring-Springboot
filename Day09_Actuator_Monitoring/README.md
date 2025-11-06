# Day 09: Spring Boot Actuator and Monitoring

## 📋 Table of Contents
- [Introduction](#introduction)
- [What is Spring Boot Actuator?](#what-is-spring-boot-actuator)
- [Getting Started](#getting-started)
- [Built-in Endpoints](#built-in-endpoints)
- [Health Indicators](#health-indicators)
- [Custom Health Indicators](#custom-health-indicators)
- [Metrics with Micrometer](#metrics-with-micrometer)
- [Custom Metrics](#custom-metrics)
- [Custom Endpoints](#custom-endpoints)
- [Info Endpoint](#info-endpoint)
- [Securing Actuator Endpoints](#securing-actuator-endpoints)
- [Integration with Prometheus](#integration-with-prometheus)
- [Integration with Grafana](#integration-with-grafana)
- [Production Best Practices](#production-best-practices)
- [Complete Example](#complete-example)
- [Exercises](#exercises)
- [Key Takeaways](#key-takeaways)
- [Resources](#resources)

## Introduction

Welcome to Day 9! Today we'll explore **Spring Boot Actuator**, a powerful tool for monitoring and managing your Spring Boot applications in production.

### What You'll Learn
- How to use Spring Boot Actuator endpoints
- Creating custom health indicators
- Collecting and exposing application metrics
- Integrating with monitoring tools like Prometheus and Grafana
- Security considerations for production
- Building custom actuator endpoints

### Why Actuator Matters
- ✅ **Observability**: Understand what's happening in your application
- ✅ **Health Monitoring**: Quickly identify issues
- ✅ **Metrics Collection**: Track performance and behavior
- ✅ **Production Ready**: Built-in production-grade features
- ✅ **Easy Integration**: Works with popular monitoring tools

## What is Spring Boot Actuator?

Spring Boot Actuator provides production-ready features to help you monitor and manage your application. It includes:

- **Health checks** - Check application health and dependencies
- **Metrics** - Collect runtime metrics (CPU, memory, HTTP requests)
- **Auditing** - Track user actions and events
- **HTTP trace** - View recent HTTP requests
- **Application info** - Display application metadata

### Key Concepts

```
┌─────────────────────────────────────┐
│    Spring Boot Application          │
├─────────────────────────────────────┤
│  Business Logic                     │
│  Controllers                        │
│  Services                           │
├─────────────────────────────────────┤
│  Spring Boot Actuator               │
│  ┌──────────┐ ┌──────────┐        │
│  │ Health   │ │ Metrics  │        │
│  └──────────┘ └──────────┘        │
│  ┌──────────┐ ┌──────────┐        │
│  │ Info     │ │ Custom   │        │
│  └──────────┘ └──────────┘        │
└─────────────────────────────────────┘
         ↓
┌─────────────────────────────────────┐
│   Monitoring Tools                  │
│   (Prometheus, Grafana, etc.)      │
└─────────────────────────────────────┘
```

## Getting Started

### 1. Add Dependency

**Maven (pom.xml):**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Gradle (build.gradle):**
```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

### 2. Basic Configuration

**application.yml:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
```

### 3. Run and Test

Start your application and visit:
- `http://localhost:8080/actuator`
- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/info`

## Built-in Endpoints

### Common Actuator Endpoints

| Endpoint | Purpose | Sensitive |
|----------|---------|-----------|
| `/actuator` | List all available endpoints | No |
| `/actuator/health` | Application health status | No |
| `/actuator/info` | Application information | No |
| `/actuator/metrics` | Application metrics | Yes |
| `/actuator/env` | Environment properties | Yes |
| `/actuator/loggers` | Application loggers | Yes |
| `/actuator/beans` | All Spring beans | Yes |
| `/actuator/mappings` | All @RequestMapping paths | Yes |
| `/actuator/threaddump` | Thread dump | Yes |
| `/actuator/heapdump` | Heap dump (downloads file) | Yes |

### Enabling Endpoints

**Enable specific endpoints:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

**Enable all endpoints (NOT for production):**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: '*'
```

**Exclude specific endpoints:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: '*'
        exclude: heapdump,threaddump
```

## Health Indicators

### Understanding Health Status

Health indicators check the status of various components:

**Health Status Values:**
- `UP` - Component is working
- `DOWN` - Component is not working
- `OUT_OF_SERVICE` - Component is temporarily unavailable
- `UNKNOWN` - Component status is unknown

### Built-in Health Indicators

Spring Boot includes several built-in health indicators:

1. **DataSourceHealthIndicator** - Database connectivity
2. **DiskSpaceHealthIndicator** - Disk space
3. **PingHealthIndicator** - Simple ping check
4. **RabbitHealthIndicator** - RabbitMQ connectivity
5. **RedisHealthIndicator** - Redis connectivity
6. **MongoHealthIndicator** - MongoDB connectivity

### Configuration

```yaml
management:
  endpoint:
    health:
      show-details: always  # Options: never, when-authorized, always
      show-components: always
  health:
    defaults:
      enabled: true
    diskspace:
      enabled: true
      threshold: 10MB  # Minimum disk space required
    db:
      enabled: true
```

### Health Response Example

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 265821753344,
        "threshold": 10485760,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

## Custom Health Indicators

### Creating a Custom Health Indicator

**Simple Health Indicator:**
```java
package com.example.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // Perform health check logic
        boolean isHealthy = checkExternalService();
        
        if (isHealthy) {
            return Health.up()
                    .withDetail("service", "External API")
                    .withDetail("status", "Available")
                    .build();
        } else {
            return Health.down()
                    .withDetail("service", "External API")
                    .withDetail("status", "Unavailable")
                    .withDetail("error", "Connection timeout")
                    .build();
        }
    }
    
    private boolean checkExternalService() {
        // Add your health check logic here
        try {
            // Simulate checking an external service
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

**Database Connection Health Indicator:**
```java
package com.example.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final JdbcTemplate jdbcTemplate;
    
    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public Health health() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Health.up()
                    .withDetail("database", "Connected")
                    .withDetail("query", "SELECT 1")
                    .withDetail("result", result)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "Disconnected")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
```

**External API Health Indicator:**
```java
package com.example.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ExternalApiHealthIndicator implements HealthIndicator {
    
    private final RestTemplate restTemplate;
    private static final String API_URL = "https://api.example.com/health";
    
    public ExternalApiHealthIndicator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Override
    public Health health() {
        try {
            long startTime = System.currentTimeMillis();
            restTemplate.getForEntity(API_URL, String.class);
            long responseTime = System.currentTimeMillis() - startTime;
            
            return Health.up()
                    .withDetail("api", "External API")
                    .withDetail("url", API_URL)
                    .withDetail("responseTime", responseTime + "ms")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("api", "External API")
                    .withDetail("url", API_URL)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
```

## Metrics with Micrometer

### What is Micrometer?

Micrometer is a metrics instrumentation library that provides a vendor-neutral interface for metrics collection. It's like SLF4J but for metrics.

### Built-in Metrics

Spring Boot automatically records:
- **JVM metrics** - Memory, threads, garbage collection
- **CPU metrics** - CPU usage
- **File descriptor metrics** - Open files
- **Uptime metrics** - Application uptime
- **Tomcat metrics** - Request count, errors, sessions
- **Spring MVC metrics** - HTTP request latency and counts
- **Data Source metrics** - Connection pool statistics

### Viewing Metrics

**List all metrics:**
```
GET http://localhost:8080/actuator/metrics
```

**Response:**
```json
{
  "names": [
    "jvm.memory.used",
    "jvm.gc.pause",
    "system.cpu.usage",
    "http.server.requests",
    "hikaricp.connections.active"
  ]
}
```

**View specific metric:**
```
GET http://localhost:8080/actuator/metrics/http.server.requests
```

**Response:**
```json
{
  "name": "http.server.requests",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 245
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 5.3
    },
    {
      "statistic": "MAX",
      "value": 0.5
    }
  ],
  "availableTags": [
    {
      "tag": "exception",
      "values": ["None", "ResourceNotFoundException"]
    },
    {
      "tag": "method",
      "values": ["GET", "POST", "PUT", "DELETE"]
    },
    {
      "tag": "uri",
      "values": ["/api/users", "/api/products"]
    },
    {
      "tag": "status",
      "values": ["200", "404", "500"]
    }
  ]
}
```

### Filtering Metrics

**Filter by tag:**
```
GET http://localhost:8080/actuator/metrics/http.server.requests?tag=status:200&tag=uri:/api/users
```

## Custom Metrics

### Counter - Track Event Counts

```java
package com.example.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    private final Counter userRegistrationCounter;
    private final Counter userLoginCounter;
    
    public UserService(MeterRegistry meterRegistry) {
        this.userRegistrationCounter = Counter.builder("user.registration")
                .description("Number of user registrations")
                .tag("type", "signup")
                .register(meterRegistry);
                
        this.userLoginCounter = Counter.builder("user.login")
                .description("Number of user logins")
                .tag("type", "authentication")
                .register(meterRegistry);
    }
    
    public void registerUser(String username) {
        // Register user logic
        userRegistrationCounter.increment();
    }
    
    public void loginUser(String username) {
        // Login logic
        userLoginCounter.increment();
    }
}
```

### Timer - Track Duration

```java
package com.example.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    
    private final Timer orderProcessingTimer;
    private final MeterRegistry meterRegistry;
    
    public OrderService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.orderProcessingTimer = Timer.builder("order.processing")
                .description("Order processing time")
                .tag("service", "order")
                .register(meterRegistry);
    }
    
    public void processOrder(Long orderId) {
        orderProcessingTimer.record(() -> {
            // Process order logic (this will be timed)
            simulateOrderProcessing();
        });
    }
    
    // Alternative using Timer.Sample
    public void processOrderAlternative(Long orderId) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // Process order logic
            simulateOrderProcessing();
        } finally {
            sample.stop(orderProcessingTimer);
        }
    }
    
    private void simulateOrderProcessing() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### Gauge - Track Current Value

```java
package com.example.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class QueueMonitor {
    
    private final AtomicInteger queueSize = new AtomicInteger(0);
    
    public QueueMonitor(MeterRegistry meterRegistry) {
        Gauge.builder("queue.size", queueSize, AtomicInteger::get)
                .description("Current queue size")
                .tag("queue", "orders")
                .register(meterRegistry);
    }
    
    public void addToQueue() {
        queueSize.incrementAndGet();
    }
    
    public void removeFromQueue() {
        queueSize.decrementAndGet();
    }
}
```

### Distribution Summary - Track Distribution

```java
package com.example.metrics;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    
    private final DistributionSummary paymentAmountSummary;
    
    public PaymentService(MeterRegistry meterRegistry) {
        this.paymentAmountSummary = DistributionSummary.builder("payment.amount")
                .description("Payment amount distribution")
                .baseUnit("dollars")
                .tag("currency", "USD")
                .register(meterRegistry);
    }
    
    public void processPayment(double amount) {
        // Payment processing logic
        paymentAmountSummary.record(amount);
    }
}
```

## Custom Endpoints

### Creating a Custom Actuator Endpoint

```java
package com.example.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "customstatus")
public class CustomStatusEndpoint {
    
    private String status = "OK";
    
    @ReadOperation
    public Map<String, Object> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "CustomService");
        return response;
    }
    
    @WriteOperation
    public void setStatus(String newStatus) {
        this.status = newStatus;
    }
}
```

**Enable the custom endpoint:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: customstatus,health,info
```

**Access the endpoint:**
```
GET http://localhost:8080/actuator/customstatus
```

### Advanced Custom Endpoint

```java
package com.example.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Endpoint(id = "features")
public class FeatureFlagEndpoint {
    
    private final Map<String, Boolean> features = new HashMap<>();
    
    public FeatureFlagEndpoint() {
        features.put("newUI", true);
        features.put("betaFeature", false);
        features.put("paymentGateway", true);
    }
    
    @ReadOperation
    public Map<String, Boolean> getAllFeatures() {
        return features;
    }
    
    @ReadOperation
    public Boolean getFeature(@Selector String featureName) {
        return features.getOrDefault(featureName, false);
    }
}
```

## Info Endpoint

### Configure Application Information

**application.yml:**
```yaml
info:
  app:
    name: My Spring Boot Application
    description: A comprehensive Spring Boot application
    version: 1.0.0
    encoding: @project.build.sourceEncoding@
    java:
      version: @java.version@
  company:
    name: Example Corp
    email: info@example.com
    website: https://example.com
```

**Maven - include Git information:**

Add to pom.xml:
```xml
<plugin>
    <groupId>pl.project13.maven</groupId>
    <artifactId>git-commit-id-plugin</artifactId>
</plugin>
```

### Custom Info Contributors

```java
package com.example.actuator;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomInfoContributor implements InfoContributor {
    
    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> customInfo = new HashMap<>();
        customInfo.put("activeUsers", getActiveUserCount());
        customInfo.put("totalRequests", getTotalRequestCount());
        customInfo.put("serverTime", System.currentTimeMillis());
        
        builder.withDetail("statistics", customInfo);
    }
    
    private int getActiveUserCount() {
        // Logic to get active user count
        return 150;
    }
    
    private long getTotalRequestCount() {
        // Logic to get total requests
        return 50000L;
    }
}
```

## Securing Actuator Endpoints

### Using Spring Security

**Add Spring Security dependency:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**Security Configuration:**
```java
package com.example.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ActuatorSecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic();
        
        return http.build();
    }
    
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("admin123")
                .roles("ADMIN")
                .build();
        
        return new InMemoryUserDetailsManager(admin);
    }
}
```

### Role-Based Access Configuration

**application.yml:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: '*'
  endpoint:
    health:
      show-details: when-authorized
      roles: ADMIN
```

## Integration with Prometheus

### Add Micrometer Prometheus Dependency

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### Configure Prometheus Endpoint

**application.yml:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Access Prometheus Metrics

```
GET http://localhost:8080/actuator/prometheus
```

**Response (Prometheus format):**
```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="PS Eden Space",} 2.4567808E7
jvm_memory_used_bytes{area="heap",id="PS Survivor Space",} 0.0
jvm_memory_used_bytes{area="heap",id="PS Old Gen",} 1.8432E7

# HELP http_server_requests_seconds  
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/api/users",} 245.0
http_server_requests_seconds_sum{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/api/users",} 5.3
```

### Prometheus Configuration

**prometheus.yml:**
```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'spring-boot-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

## Integration with Grafana

### Setup Steps

1. **Install Grafana**
2. **Add Prometheus as Data Source**
3. **Create Dashboards**

### Sample Grafana Dashboard Queries

**JVM Memory Usage:**
```promql
jvm_memory_used_bytes{area="heap"}
```

**HTTP Request Rate:**
```promql
rate(http_server_requests_seconds_count[5m])
```

**HTTP Request Duration (95th percentile):**
```promql
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le, uri))
```

**Error Rate:**
```promql
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
```

### Pre-built Dashboards

Import these popular dashboard IDs in Grafana:
- **4701** - JVM (Micrometer)
- **11378** - Spring Boot Statistics
- **12900** - Spring Boot APM Dashboard

## Production Best Practices

### 1. Security

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
        exclude: heapdump,threaddump,env
  endpoint:
    health:
      show-details: when-authorized
```

### 2. Network Configuration

```yaml
# Run actuator on different port
management:
  server:
    port: 9090
  endpoints:
    web:
      base-path: /management
```

Access: `http://localhost:9090/management/health`

### 3. CORS Configuration

```java
@Configuration
public class ActuatorCorsConfig {
    
    @Bean
    public WebMvcConfigurer actuatorCorsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/actuator/**")
                        .allowedOrigins("https://monitoring.example.com")
                        .allowedMethods("GET");
            }
        };
    }
}
```

### 4. Custom Health Groups

```yaml
management:
  endpoint:
    health:
      group:
        readiness:
          include: db,redis
          show-details: always
        liveness:
          include: ping
          show-details: always
```

Access:
- `http://localhost:8080/actuator/health/readiness`
- `http://localhost:8080/actuator/health/liveness`

### 5. Metrics Filtering

```yaml
management:
  metrics:
    enable:
      jvm: true
      process: true
      system: true
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}
```

## Complete Example

### Application Structure

```
src/main/java/com/example/monitoring/
├── MonitoringApplication.java
├── controller/
│   └── UserController.java
├── service/
│   └── UserService.java
├── health/
│   ├── DatabaseHealthIndicator.java
│   └── ExternalApiHealthIndicator.java
├── metrics/
│   └── CustomMetricsService.java
├── actuator/
│   ├── CustomStatusEndpoint.java
│   └── CustomInfoContributor.java
└── config/
    └── ActuatorSecurityConfig.java
```

### Main Application

```java
package com.example.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class MonitoringApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MonitoringApplication.class, args);
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### User Controller with Metrics

```java
package com.example.monitoring.controller;

import com.example.monitoring.service.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    private final Counter userRequestCounter;
    
    public UserController(UserService userService, MeterRegistry meterRegistry) {
        this.userService = userService;
        this.userRequestCounter = Counter.builder("user.requests")
                .description("Total user API requests")
                .tag("api", "user")
                .register(meterRegistry);
    }
    
    @GetMapping
    public ResponseEntity<List<String>> getAllUsers() {
        userRequestCounter.increment();
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody String username) {
        userRequestCounter.increment();
        userService.registerUser(username);
        return ResponseEntity.ok("User created");
    }
}
```

### Complete application.yml

```yaml
spring:
  application:
    name: monitoring-demo
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,customstatus
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      show-components: always
  health:
    diskspace:
      enabled: true
      threshold: 10MB
  metrics:
    tags:
      application: ${spring.application.name}
      environment: development
    export:
      prometheus:
        enabled: true

info:
  app:
    name: Monitoring Demo Application
    version: 1.0.0
    description: Spring Boot Actuator demonstration
  company:
    name: Example Corp
    email: info@example.com

server:
  port: 8080
```

## Exercises

### Exercise 1: Basic Setup
Create a Spring Boot application with Actuator enabled and configure it to expose health, info, and metrics endpoints.

**Tasks:**
1. Add Spring Boot Actuator dependency
2. Configure endpoints in application.yml
3. Test each endpoint
4. Customize the info endpoint with your application details

### Exercise 2: Custom Health Indicator
Create a custom health indicator that checks if a file exists on the file system.

**Requirements:**
- Check if `/tmp/app-ready.txt` exists
- Return UP if file exists, DOWN if it doesn't
- Include file path in the details

### Exercise 3: Custom Metrics
Implement custom metrics for a shopping cart service.

**Requirements:**
- Counter for items added to cart
- Counter for items removed from cart
- Gauge for current cart size
- Timer for checkout duration
- Distribution summary for order amounts

### Exercise 4: Custom Endpoint
Create a custom actuator endpoint that manages feature flags.

**Requirements:**
- GET operation to list all features
- POST operation to toggle a feature
- Include feature status and last modified time

### Exercise 5: Production Configuration
Configure actuator for production use with security.

**Requirements:**
- Run actuator on port 9090
- Secure all endpoints except health and info
- Create separate health groups for liveness and readiness
- Configure Prometheus integration

## Key Takeaways

1. **Actuator Provides Production-Ready Features**
   - Health checks, metrics, and monitoring out of the box
   - Easy to extend with custom endpoints and indicators

2. **Health Indicators Show System Status**
   - Built-in indicators for common dependencies
   - Custom indicators for application-specific checks
   - Different health statuses (UP, DOWN, OUT_OF_SERVICE)

3. **Micrometer Provides Vendor-Neutral Metrics**
   - Support for multiple monitoring systems
   - Rich set of metric types (Counter, Timer, Gauge, Summary)
   - Easy integration with Prometheus and Grafana

4. **Security is Critical**
   - Don't expose sensitive endpoints publicly
   - Use authentication and authorization
   - Consider separate management port

5. **Custom Endpoints Enable Business Monitoring**
   - Create endpoints specific to your application
   - Expose business metrics and feature flags
   - Enable operational insights

6. **Integration with Monitoring Tools**
   - Prometheus for metrics collection
   - Grafana for visualization
   - Standard format for easy integration

## Resources

### Official Documentation
- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)

### Tutorials
- [Spring Boot Actuator Tutorial](https://www.baeldung.com/spring-boot-actuators)
- [Custom Actuator Endpoints](https://www.baeldung.com/spring-boot-actuator-custom-endpoint)
- [Micrometer Metrics](https://www.baeldung.com/micrometer)

### Tools
- [Prometheus](https://prometheus.io/)
- [Grafana](https://grafana.com/)
- [Spring Boot Admin](https://github.com/codecentric/spring-boot-admin)

---
[<< Previous: Day 08](../Day08_Configuration/README.md) | [Back to Main](../README.md) | [Next: Day 10 >>](../Day10_Security_Basics/README.md)
