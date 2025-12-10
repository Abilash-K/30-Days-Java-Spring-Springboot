# Day 09: Spring Boot Actuator and Monitoring

## 📋 Table of Contents
- [Introduction](#introduction)
- [Spring Boot Actuator Overview](#spring-boot-actuator-overview)
- [Getting Started](#getting-started)
- [Built-in Endpoints](#built-in-endpoints)
- [Health Indicators](#health-indicators)
- [Custom Health Indicators](#custom-health-indicators)
- [Metrics with Micrometer](#metrics-with-micrometer)
- [Custom Metrics](#custom-metrics)
- [Custom Endpoints](#custom-endpoints)
- [Securing Actuator Endpoints](#securing-actuator-endpoints)
- [Integration with Prometheus](#integration-with-prometheus)
- [Integration with Grafana](#integration-with-grafana)
- [Best Practices](#best-practices)
- [Complete Example](#complete-example)
- [Exercises](#exercises)
- [Key Takeaways](#key-takeaways)
- [Resources](#resources)

## Introduction

Welcome to Day 9! Today we'll explore **Spring Boot Actuator** - a powerful tool for monitoring and managing your Spring Boot applications in production.

### What You'll Learn
- Understanding Spring Boot Actuator and its benefits
- Exposing and customizing actuator endpoints
- Creating custom health indicators
- Collecting and exposing metrics with Micrometer
- Integrating with monitoring systems (Prometheus, Grafana)
- Securing actuator endpoints
- Production monitoring best practices

### Why Actuator Matters
- ✅ **Production Monitoring**: Real-time visibility into application health
- ✅ **Metrics Collection**: Track performance, memory, and custom metrics
- ✅ **Operational Management**: Manage application at runtime
- ✅ **Health Checks**: Automated health status for load balancers
- ✅ **Troubleshooting**: Access logs, thread dumps, and heap dumps
- ✅ **Integration Ready**: Works with Prometheus, Grafana, and other tools

## Spring Boot Actuator Overview

### What is Spring Boot Actuator?

Spring Boot Actuator provides **production-ready features** to help you monitor and manage your application. It exposes operational information about the running application via HTTP endpoints or JMX.

### Key Features

1. **Health Checks**: Application and dependency health status
2. **Metrics**: Application metrics (memory, CPU, HTTP requests, etc.)
3. **Info**: Custom application information
4. **Environment**: Configuration properties
5. **Loggers**: View and modify logging levels at runtime
6. **Thread Dump**: Thread information for troubleshooting
7. **HTTP Trace**: HTTP request/response traces
8. **Custom Endpoints**: Create your own management endpoints

### Architecture

```
┌─────────────────────────────────────────┐
│        Spring Boot Application          │
├─────────────────────────────────────────┤
│                                         │
│  ┌───────────────────────────────────┐ │
│  │    Application Components         │ │
│  │  (Controllers, Services, etc.)    │ │
│  └───────────────────────────────────┘ │
│                 ↓                       │
│  ┌───────────────────────────────────┐ │
│  │      Actuator Endpoints           │ │
│  │  (/actuator/health, /metrics...)  │ │
│  └───────────────────────────────────┘ │
│                 ↓                       │
│  ┌───────────────────────────────────┐ │
│  │         Micrometer                │ │
│  │    (Metrics Abstraction Layer)    │ │
│  └───────────────────────────────────┘ │
│                 ↓                       │
└─────────────────┼───────────────────────┘
                  ↓
    ┌─────────────────────────────┐
    │  Monitoring Systems         │
    │  (Prometheus, Grafana, etc.)│
    └─────────────────────────────┘
```

## Getting Started

### 1. Add Actuator Dependency

**Maven (pom.xml):**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Optional: For Prometheus integration -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**Gradle (build.gradle):**
```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
// Optional: For Prometheus integration
implementation 'io.micrometer:micrometer-registry-prometheus'
```

### 2. Basic Configuration

**application.yml:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics  # Expose specific endpoints
        # include: '*'  # Expose all endpoints (not recommended for production)
      base-path: /actuator  # Base path for actuator endpoints (default)
  
  endpoint:
    health:
      show-details: always  # Show detailed health information
      show-components: always
    
  info:
    env:
      enabled: true  # Enable info from environment
```

### 3. Test Actuator Endpoints

Start your application and access:
- Health: `http://localhost:8080/actuator/health`
- Info: `http://localhost:8080/actuator/info`
- Metrics: `http://localhost:8080/actuator/metrics`

## Built-in Endpoints

### Common Actuator Endpoints

| Endpoint | Description | Example |
|----------|-------------|---------|
| `/actuator` | Lists all available endpoints | GET /actuator |
| `/actuator/health` | Application health status | GET /actuator/health |
| `/actuator/info` | Application information | GET /actuator/info |
| `/actuator/metrics` | Application metrics list | GET /actuator/metrics |
| `/actuator/metrics/{name}` | Specific metric details | GET /actuator/metrics/jvm.memory.used |
| `/actuator/env` | Environment properties | GET /actuator/env |
| `/actuator/loggers` | Logger configuration | GET /actuator/loggers |
| `/actuator/loggers/{name}` | Specific logger level | POST /actuator/loggers/com.example |
| `/actuator/httptrace` | HTTP request traces | GET /actuator/httptrace |
| `/actuator/threaddump` | Thread dump | GET /actuator/threaddump |
| `/actuator/heapdump` | Heap dump (downloads file) | GET /actuator/heapdump |
| `/actuator/prometheus` | Prometheus metrics | GET /actuator/prometheus |

### Health Endpoint Response

```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 336210669568,
        "threshold": 10485760,
        "exists": true
      }
    },
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

### Metrics Endpoint Response

```json
{
  "names": [
    "jvm.memory.used",
    "jvm.memory.max",
    "jvm.gc.pause",
    "process.cpu.usage",
    "http.server.requests",
    "system.cpu.usage",
    "tomcat.sessions.active.current"
  ]
}
```

**Accessing specific metric:**
```bash
GET /actuator/metrics/jvm.memory.used

Response:
{
  "name": "jvm.memory.used",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 268435456
    }
  ],
  "availableTags": [
    {
      "tag": "area",
      "values": ["heap", "nonheap"]
    }
  ]
}
```

## Health Indicators

### Built-in Health Indicators

Spring Boot provides several built-in health indicators:

| Health Indicator | Description | Auto-configured when |
|-----------------|-------------|---------------------|
| `DataSourceHealthIndicator` | Database connectivity | DataSource is available |
| `DiskSpaceHealthIndicator` | Disk space availability | Always |
| `PingHealthIndicator` | Always returns UP | Always |
| `RedisHealthIndicator` | Redis connectivity | RedisConnectionFactory |
| `MongoHealthIndicator` | MongoDB connectivity | MongoTemplate |
| `RabbitHealthIndicator` | RabbitMQ connectivity | RabbitTemplate |
| `CassandraHealthIndicator` | Cassandra connectivity | CassandraOperations |
| `ElasticsearchHealthIndicator` | Elasticsearch connectivity | ElasticsearchRestClient |

### Configuring Health Details

**application.yml:**
```yaml
management:
  endpoint:
    health:
      show-details: when-authorized  # Options: never, when-authorized, always
      show-components: when-authorized
      roles: ADMIN  # Required role to see details
      
      # Individual indicator configuration
      probes:
        enabled: true  # Enable liveness and readiness probes
      
  health:
    # Disable specific health indicators
    db:
      enabled: true
    diskspace:
      enabled: true
      threshold: 10MB  # Alert if free space < 10MB
    redis:
      enabled: false  # Disable Redis health check
```

### Health Status Aggregation

Health status follows this order (worst to best):
- `DOWN` → `OUT_OF_SERVICE` → `UP` → `UNKNOWN`

The overall status is determined by the worst component status.

## Custom Health Indicators

### Creating a Custom Health Indicator

**ExternalServiceHealthIndicator.java:**
```java
package com.example.actuator.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ExternalServiceHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate;
    private static final String EXTERNAL_SERVICE_URL = "https://api.example.com/status";

    public ExternalServiceHealthIndicator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Health health() {
        try {
            // Check external service availability
            String response = restTemplate.getForObject(EXTERNAL_SERVICE_URL, String.class);
            
            return Health.up()
                    .withDetail("externalService", "Available")
                    .withDetail("url", EXTERNAL_SERVICE_URL)
                    .withDetail("response", response)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("externalService", "Unavailable")
                    .withDetail("url", EXTERNAL_SERVICE_URL)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
```

### Advanced Custom Health Indicator

**DatabaseConnectionPoolHealthIndicator.java:**
```java
package com.example.actuator.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseConnectionPoolHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseConnectionPoolHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1000)) {
                return Health.up()
                        .withDetail("database", "Connected")
                        .withDetail("validationTimeout", "1000ms")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "Invalid connection")
                        .build();
            }
        } catch (SQLException e) {
            return Health.down()
                    .withDetail("database", "Connection failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
```

### Reactive Health Indicator

For reactive applications using WebFlux:

**ReactiveExternalServiceHealthIndicator.java:**
```java
package com.example.actuator.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ReactiveExternalServiceHealthIndicator implements ReactiveHealthIndicator {

    private final WebClient webClient;
    private static final String SERVICE_URL = "https://api.example.com/status";

    public ReactiveExternalServiceHealthIndicator(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(SERVICE_URL).build();
    }

    @Override
    public Mono<Health> health() {
        return webClient.get()
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> Health.up()
                        .withDetail("service", "Available")
                        .withDetail("response", response)
                        .build())
                .onErrorResume(ex -> Mono.just(Health.down()
                        .withDetail("service", "Unavailable")
                        .withDetail("error", ex.getMessage())
                        .build()));
    }
}
```

## Metrics with Micrometer

### Understanding Micrometer

Micrometer is a metrics instrumentation library that provides a **facade over various monitoring systems** (Prometheus, Datadog, New Relic, etc.).

### Supported Monitoring Systems

- Prometheus
- Datadog
- New Relic
- InfluxDB
- Graphite
- CloudWatch
- AppOptics
- Azure Monitor
- Dynatrace
- Elastic
- Ganglia
- Humio
- JMX
- KairosDB
- SignalFx
- Stackdriver
- StatsD
- Wavefront

### Common Metric Types

1. **Counter**: Monotonically increasing value
2. **Gauge**: Current value that can go up or down
3. **Timer**: Measures duration and count
4. **DistributionSummary**: Distribution of events
5. **LongTaskTimer**: Long-running task duration

### Available Metrics

**JVM Metrics:**
```
jvm.memory.used          # Memory usage
jvm.memory.max           # Max memory
jvm.gc.pause             # Garbage collection pauses
jvm.threads.live         # Live thread count
jvm.threads.daemon       # Daemon thread count
jvm.classes.loaded       # Loaded class count
```

**System Metrics:**
```
system.cpu.usage         # System CPU usage
system.cpu.count         # Number of CPUs
process.cpu.usage        # Process CPU usage
process.uptime           # Process uptime
```

**Application Metrics:**
```
http.server.requests     # HTTP request metrics
logback.events           # Logging events
tomcat.sessions.active   # Active sessions
```

### Metric Tags

Tags allow you to drill down into metrics:

```bash
GET /actuator/metrics/http.server.requests?tag=uri:/api/users&tag=status:200

{
  "name": "http.server.requests",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 1523
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 45.234
    },
    {
      "statistic": "MAX",
      "value": 0.523
    }
  ],
  "availableTags": [
    {"tag": "exception", "values": ["None"]},
    {"tag": "method", "values": ["GET"]},
    {"tag": "uri", "values": ["/api/users"]},
    {"tag": "status", "values": ["200"]}
  ]
}
```

## Custom Metrics

### Using MeterRegistry

**UserService.java:**
```java
package com.example.actuator.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    private final Counter userRegistrationCounter;
    private final Counter userLoginCounter;
    private final Timer userOperationTimer;
    
    public UserService(MeterRegistry meterRegistry) {
        // Counter for user registrations
        this.userRegistrationCounter = Counter.builder("user.registrations")
                .description("Total number of user registrations")
                .tag("service", "user")
                .register(meterRegistry);
        
        // Counter for user logins
        this.userLoginCounter = Counter.builder("user.logins")
                .description("Total number of user logins")
                .tag("service", "user")
                .register(meterRegistry);
        
        // Timer for user operations
        this.userOperationTimer = Timer.builder("user.operations")
                .description("Time taken for user operations")
                .tag("service", "user")
                .register(meterRegistry);
    }
    
    public void registerUser(String username) {
        Timer.Sample sample = Timer.start();
        
        try {
            // Registration logic
            Thread.sleep(100); // Simulate work
            
            // Increment counter
            userRegistrationCounter.increment();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Record timing
            sample.stop(userOperationTimer);
        }
    }
    
    public void loginUser(String username) {
        userLoginCounter.increment();
        // Login logic
    }
}
```

### Custom Gauge

**ApplicationMetricsService.java:**
```java
package com.example.actuator.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ApplicationMetricsService {

    private final MeterRegistry meterRegistry;
    private final AtomicInteger activeUsers = new AtomicInteger(0);
    private final AtomicInteger queueSize = new AtomicInteger(0);

    public ApplicationMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        // Register gauge for active users
        Gauge.builder("app.users.active", activeUsers, AtomicInteger::get)
                .description("Number of active users")
                .tag("type", "users")
                .register(meterRegistry);
        
        // Register gauge for queue size
        Gauge.builder("app.queue.size", queueSize, AtomicInteger::get)
                .description("Size of processing queue")
                .tag("type", "queue")
                .register(meterRegistry);
    }

    public void userLoggedIn() {
        activeUsers.incrementAndGet();
    }

    public void userLoggedOut() {
        activeUsers.decrementAndGet();
    }

    public void addToQueue() {
        queueSize.incrementAndGet();
    }

    public void removeFromQueue() {
        queueSize.decrementAndGet();
    }
}
```

### Distribution Summary

**OrderService.java:**
```java
package com.example.actuator.service;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final DistributionSummary orderAmountSummary;

    public OrderService(MeterRegistry meterRegistry) {
        this.orderAmountSummary = DistributionSummary.builder("order.amount")
                .description("Distribution of order amounts")
                .baseUnit("USD")
                .scale(1.0)
                .register(meterRegistry);
    }

    public void processOrder(double amount) {
        // Record order amount
        orderAmountSummary.record(amount);
        
        // Process order logic
    }
}
```

### Using @Timed Annotation

**ProductController.java:**
```java
package com.example.actuator.controller;

import io.micrometer.core.annotation.Timed;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    @Timed(value = "product.get.all", description = "Time taken to retrieve all products")
    public List<Product> getAllProducts() {
        // Automatically timed
        return productService.findAll();
    }

    @GetMapping("/{id}")
    @Timed(value = "product.get.by.id", description = "Time taken to retrieve product by ID")
    public Product getProductById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping
    @Timed(value = "product.create", description = "Time taken to create product")
    public Product createProduct(@RequestBody Product product) {
        return productService.save(product);
    }
}
```

**Enable @Timed annotation:**
```java
@Configuration
public class MetricsConfiguration {
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
```

## Custom Endpoints

### Creating a Custom Actuator Endpoint

**CustomEndpoint.java:**
```java
package com.example.actuator.endpoint;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "custom")  // Access via /actuator/custom
public class CustomEndpoint {

    private final Map<String, String> cache = new HashMap<>();

    // GET /actuator/custom
    @ReadOperation
    public Map<String, String> getAll() {
        return cache;
    }

    // GET /actuator/custom/{key}
    @ReadOperation
    public String get(@Selector String key) {
        return cache.get(key);
    }

    // POST /actuator/custom (with request body)
    @WriteOperation
    public void set(String key, String value) {
        cache.put(key, value);
    }
}
```

### Feature Toggle Endpoint

**FeatureToggleEndpoint.java:**
```java
package com.example.actuator.endpoint;

import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "features")
public class FeatureToggleEndpoint {

    private final Map<String, Boolean> features = new HashMap<>();

    public FeatureToggleEndpoint() {
        // Initialize default features
        features.put("newUI", false);
        features.put("betaFeature", false);
        features.put("premiumFeature", false);
    }

    // GET /actuator/features
    @ReadOperation
    public Map<String, Boolean> getFeatures() {
        return features;
    }

    // GET /actuator/features/{name}
    @ReadOperation
    public Boolean getFeature(@Selector String name) {
        return features.getOrDefault(name, false);
    }

    // POST /actuator/features/{name}
    @WriteOperation
    public void toggleFeature(@Selector String name, boolean enabled) {
        features.put(name, enabled);
    }

    // DELETE /actuator/features/{name}
    @DeleteOperation
    public void removeFeature(@Selector String name) {
        features.remove(name);
    }
}
```

### Application Info Endpoint

**AppInfoEndpoint.java:**
```java
package com.example.actuator.endpoint;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "appinfo")
public class AppInfoEndpoint {

    @ReadOperation
    public Map<String, Object> getApplicationInfo() {
        Map<String, Object> info = new HashMap<>();
        
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        
        info.put("applicationName", "E-Commerce Platform");
        info.put("version", "1.0.0");
        info.put("uptime", runtimeMXBean.getUptime() + "ms");
        info.put("jvmVersion", System.getProperty("java.version"));
        info.put("environment", System.getProperty("spring.profiles.active", "default"));
        
        return info;
    }
}
```

## Securing Actuator Endpoints

### Spring Security Configuration

**SecurityConfig.java:**
```java
package com.example.actuator.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                
                // Secured actuator endpoints
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
                
                // Application endpoints
                .anyRequest().authenticated()
            )
            .httpBasic();
        
        return http.build();
    }
}
```

### Configuration-based Security

**application.yml:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  
  endpoint:
    health:
      show-details: when-authorized
      roles: ADMIN
    
    metrics:
      enabled: true
    
    prometheus:
      enabled: true

spring:
  security:
    user:
      name: admin
      password: admin123
      roles: ADMIN
```

### Custom Security for Specific Endpoints

**ActuatorSecurityConfig.java:**
```java
package com.example.actuator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ActuatorSecurityConfig {

    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/actuator/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/actuator/prometheus").permitAll()  // For Prometheus scraping
                .requestMatchers("/actuator/metrics/**").hasRole("METRICS_ADMIN")
                .requestMatchers("/actuator/**").hasRole("ADMIN")
            )
            .httpBasic();
        
        return http.build();
    }
}
```

## Integration with Prometheus

### 1. Add Prometheus Dependency

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### 2. Configure Prometheus Endpoint

**application.yml:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  
  metrics:
    export:
      prometheus:
        enabled: true
    
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active:default}
```

### 3. Prometheus Configuration

**prometheus.yml:**
```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'spring-boot-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
        labels:
          application: 'my-spring-app'
          environment: 'production'
```

### 4. Run Prometheus

```bash
# Using Docker
docker run -d \
  --name prometheus \
  -p 9090:9090 \
  -v $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus

# Access Prometheus UI
# http://localhost:9090
```

### 5. Prometheus Queries

```promql
# HTTP request rate
rate(http_server_requests_seconds_count[5m])

# Average response time
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# JVM memory usage
jvm_memory_used_bytes{area="heap"}

# CPU usage
system_cpu_usage
```

## Integration with Grafana

### 1. Run Grafana

```bash
# Using Docker
docker run -d \
  --name grafana \
  -p 3000:3000 \
  grafana/grafana

# Default credentials
# Username: admin
# Password: admin
```

### 2. Add Prometheus Data Source

1. Access Grafana: `http://localhost:3000`
2. Go to Configuration → Data Sources
3. Add Prometheus data source
4. URL: `http://prometheus:9090` (or `http://localhost:9090`)
5. Save & Test

### 3. Create Dashboard

**Sample Dashboard JSON:**
```json
{
  "dashboard": {
    "title": "Spring Boot Application Dashboard",
    "panels": [
      {
        "title": "HTTP Request Rate",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count[5m])"
          }
        ]
      },
      {
        "title": "JVM Memory Usage",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes{area='heap'}"
          }
        ]
      },
      {
        "title": "CPU Usage",
        "targets": [
          {
            "expr": "system_cpu_usage"
          }
        ]
      }
    ]
  }
}
```

### 4. Import Pre-built Dashboards

Grafana provides community dashboards:
- Dashboard ID: **4701** (JVM Micrometer)
- Dashboard ID: **11378** (JVM Dashboard)
- Dashboard ID: **12900** (Spring Boot 2.1 Statistics)

**Import Steps:**
1. Go to Dashboards → Import
2. Enter Dashboard ID
3. Select Prometheus data source
4. Click Import

## Best Practices

### 1. Endpoint Exposure

```yaml
# ❌ Don't expose all endpoints in production
management:
  endpoints:
    web:
      exposure:
        include: '*'

# ✅ Expose only necessary endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### 2. Health Check Details

```yaml
# ✅ Control health details visibility
management:
  endpoint:
    health:
      show-details: when-authorized  # Not 'always' in production
      roles: ADMIN
```

### 3. Security

```java
// ✅ Always secure actuator endpoints
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health").permitAll()
            .requestMatchers("/actuator/**").hasRole("ADMIN")
        );
        return http.build();
    }
}
```

### 4. Custom Metrics

```java
// ✅ Use meaningful metric names and tags
Counter.builder("user.registrations")
    .description("Total number of user registrations")
    .tag("service", "user")
    .tag("environment", environment)
    .register(meterRegistry);

// ❌ Avoid generic names without context
Counter.builder("count").register(meterRegistry);
```

### 5. Metric Cardinality

```java
// ❌ Avoid high cardinality tags (many unique values)
Timer.builder("api.request")
    .tag("userId", userId)  // Bad: millions of unique users
    .register(meterRegistry);

// ✅ Use bounded/categorical tags
Timer.builder("api.request")
    .tag("endpoint", "/api/users")
    .tag("method", "GET")
    .tag("status", "200")
    .register(meterRegistry);
```

### 6. Performance

```java
// ✅ Use caching for expensive health checks
@Component
public class ExpensiveHealthIndicator implements HealthIndicator {
    
    private Health cachedHealth;
    private Instant lastCheck;
    
    @Override
    public Health health() {
        if (lastCheck == null || Duration.between(lastCheck, Instant.now()).getSeconds() > 60) {
            cachedHealth = performExpensiveCheck();
            lastCheck = Instant.now();
        }
        return cachedHealth;
    }
}
```

### 7. Monitoring Strategy

```yaml
# ✅ Configure appropriate intervals
management:
  metrics:
    export:
      prometheus:
        step: 1m  # Scrape interval
  
  endpoint:
    health:
      cache:
        time-to-live: 10s  # Cache health for 10 seconds
```

## Complete Example

### Application Setup

**pom.xml:**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
</dependencies>
```

**application.yml:**
```yaml
spring:
  application:
    name: ecommerce-service
  profiles:
    active: production

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  
  endpoint:
    health:
      show-details: when-authorized
      show-components: when-authorized
      roles: ADMIN
      probes:
        enabled: true
    
    metrics:
      enabled: true
    
    prometheus:
      enabled: true
  
  metrics:
    export:
      prometheus:
        enabled: true
        step: 1m
    
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}
    
    distribution:
      percentiles-histogram:
        http.server.requests: true
  
  info:
    env:
      enabled: true
    
    build:
      enabled: true
    
    git:
      enabled: true
      mode: full

info:
  app:
    name: E-Commerce Service
    description: Complete E-Commerce Platform with Monitoring
    version: 1.0.0
  company:
    name: Example Corp
```

**Complete Application:**

```java
package com.example.actuator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class ActuatorMonitoringApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ActuatorMonitoringApplication.class, args);
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

// REST Controller with metrics
@RestController
@RequestMapping("/api/orders")
class OrderController {
    
    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @PostMapping
    public Map<String, String> createOrder(@RequestBody Map<String, Object> order) {
        orderService.processOrder((Double) order.get("amount"));
        return Map.of("status", "created", "orderId", "ORD-" + System.currentTimeMillis());
    }
    
    @GetMapping
    public Map<String, Object> getOrderStats() {
        return orderService.getStats();
    }
}

// Service with custom metrics
@Service
class OrderService {
    
    private final Counter orderCounter;
    private final Timer orderProcessingTimer;
    private final AtomicInteger ordersInProgress = new AtomicInteger(0);
    
    public OrderService(MeterRegistry meterRegistry) {
        this.orderCounter = Counter.builder("orders.created")
                .description("Total number of orders created")
                .tag("service", "order")
                .register(meterRegistry);
        
        this.orderProcessingTimer = Timer.builder("orders.processing.time")
                .description("Time taken to process orders")
                .tag("service", "order")
                .register(meterRegistry);
        
        // Gauge for orders in progress
        meterRegistry.gauge("orders.in.progress", ordersInProgress);
    }
    
    public void processOrder(double amount) {
        Timer.Sample sample = Timer.start();
        ordersInProgress.incrementAndGet();
        
        try {
            // Simulate order processing
            Thread.sleep((long) (Math.random() * 100));
            
            orderCounter.increment();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            ordersInProgress.decrementAndGet();
            sample.stop(orderProcessingTimer);
        }
    }
    
    public Map<String, Object> getStats() {
        return Map.of(
            "totalOrders", orderCounter.count(),
            "ordersInProgress", ordersInProgress.get()
        );
    }
}

// Custom Health Indicator
@Component
class PaymentServiceHealthIndicator implements HealthIndicator {
    
    private final RestTemplate restTemplate;
    
    public PaymentServiceHealthIndicator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Override
    public Health health() {
        try {
            // Simulate payment service check
            boolean isAvailable = checkPaymentService();
            
            if (isAvailable) {
                return Health.up()
                        .withDetail("paymentService", "Available")
                        .withDetail("lastChecked", System.currentTimeMillis())
                        .build();
            } else {
                return Health.down()
                        .withDetail("paymentService", "Unavailable")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("paymentService", "Error")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
    
    private boolean checkPaymentService() {
        // Simulate check
        return Math.random() > 0.1; // 90% uptime
    }
}
```

### Testing the Application

```bash
# 1. Start the application
mvn spring-boot:run

# 2. Create some orders (generate metrics)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"amount": 99.99}'

# 3. Check health
curl http://localhost:8080/actuator/health

# 4. View metrics
curl http://localhost:8080/actuator/metrics

# 5. View specific metric
curl http://localhost:8080/actuator/metrics/orders.created

# 6. Prometheus format
curl http://localhost:8080/actuator/prometheus
```

## Exercises

### Exercise 1: Custom Health Indicator
Create a custom health indicator that checks if a file exists and has been modified in the last 24 hours.

**Requirements:**
- Check if `/tmp/heartbeat.txt` exists
- Return UP if file exists and is recent
- Return DOWN if file doesn't exist or is old
- Include file modification time in details

### Exercise 2: Custom Metrics
Create a service that tracks:
- Number of successful logins
- Number of failed login attempts
- Average login time
- Current active sessions (gauge)

### Exercise 3: Custom Endpoint
Create a custom actuator endpoint `/actuator/cache` that:
- GET: Returns all cached items
- GET /{key}: Returns specific cached item
- POST: Adds item to cache
- DELETE /{key}: Removes item from cache

### Exercise 4: Prometheus Integration
1. Add Prometheus dependency
2. Configure Prometheus scraping
3. Create custom metric for API response times
4. Query the metric in Prometheus

### Exercise 5: Grafana Dashboard
1. Set up Prometheus and Grafana
2. Create a dashboard with panels for:
   - HTTP request rate
   - Average response time
   - JVM memory usage
   - Custom business metrics
3. Add alerts for high error rates

## Key Takeaways

1. **Spring Boot Actuator** provides production-ready monitoring features
2. **Health indicators** monitor application and dependency health
3. **Micrometer** provides metrics abstraction for various monitoring systems
4. **Custom metrics** track business-specific KPIs
5. **Custom endpoints** expose application-specific management operations
6. **Security** is critical for actuator endpoints
7. **Prometheus + Grafana** provide powerful visualization
8. **Proper tagging** makes metrics more useful
9. **Avoid high cardinality** in metric tags
10. **Monitor what matters** - focus on business and operational metrics

## Resources

### Official Documentation
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)

### Tutorials
- [Spring Boot Actuator Tutorial](https://www.baeldung.com/spring-boot-actuator)
- [Custom Metrics with Micrometer](https://www.baeldung.com/micrometer)
- [Monitoring Spring Boot with Prometheus](https://spring.io/blog/2018/03/16/micrometer-spring-boot-2-s-new-application-metrics-collector)

### Tools
- [Prometheus](https://prometheus.io/)
- [Grafana](https://grafana.com/)
- [Micrometer](https://micrometer.io/)
- [Spring Boot Admin](https://github.com/codecentric/spring-boot-admin)

---
[<< Previous: Day 08](../Day08_Configuration/README.md) | [Back to Main](../README.md) | [Next: Day 10 >>](../Day10_Security_Basics/README.md)
