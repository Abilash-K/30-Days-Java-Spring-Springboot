# Day 13: Service Discovery with Eureka

## 📋 Table of Contents
- [Introduction](#introduction)
- [Service Discovery Pattern](#service-discovery-pattern)
- [Netflix Eureka Overview](#netflix-eureka-overview)
- [Setting Up Eureka Server](#setting-up-eureka-server)
- [Registering Services with Eureka](#registering-services-with-eureka)
- [Service Discovery Client](#service-discovery-client)
- [Multiple Eureka Instances](#multiple-eureka-instances)
- [Self-Preservation Mode](#self-preservation-mode)
- [Health Checks](#health-checks)
- [Eureka Dashboard](#eureka-dashboard)
- [Best Practices](#best-practices)
- [Complete Example](#complete-example)
- [Exercises](#exercises)
- [Key Takeaways](#key-takeaways)
- [Resources](#resources)

## Introduction

Welcome to Day 13! Today we'll explore **Service Discovery with Netflix Eureka** - a critical pattern for building dynamic microservices architectures.

### What You'll Learn
- Understanding the Service Registry pattern
- Setting up Eureka Server
- Registering microservices as Eureka clients
- Discovering and calling services dynamically
- High availability with multiple Eureka instances
- Monitoring service health
- Production best practices

### Why Service Discovery Matters
- ✅ **Dynamic**: Services can scale up/down without config changes
- ✅ **Resilient**: Automatic failover when instances go down
- ✅ **Decoupled**: Services don't need to know each other's locations
- ✅ **Load Balanced**: Automatic distribution across instances
- ✅ **Cloud-Native**: Essential for containerized environments
- ✅ **Self-Healing**: Removes unhealthy instances automatically

## Service Discovery Pattern

### The Problem: Hard-Coded Service Locations

**Traditional Approach:**
```java
// ❌ Hard-coded URLs - inflexible and error-prone
String userServiceUrl = "http://user-service:8081";
String orderServiceUrl = "http://order-service:8082";
String paymentServiceUrl = "http://payment-service:8083";

// What if service moves to different port?
// What if we need to scale to multiple instances?
// What if an instance goes down?
```

### The Solution: Service Registry

**Service Discovery Approach:**
```java
// ✅ Dynamic service lookup
String userServiceUrl = discoveryClient.getServiceUrl("user-service");
String orderServiceUrl = discoveryClient.getServiceUrl("order-service");

// Automatically routes to available instances
// Handles multiple instances
// Removes failed instances
```

### Service Discovery Flow

```
┌─────────────────────────────────────────────────────┐
│              Service Discovery Flow                  │
├─────────────────────────────────────────────────────┤
│                                                     │
│  1. SERVICE REGISTRATION                            │
│  ┌────────────┐                  ┌──────────────┐  │
│  │  Service A │  Register        │              │  │
│  │  Instance1 │ ───────────────> │    Eureka    │  │
│  └────────────┘                  │    Server    │  │
│                                  │   (Registry) │  │
│  ┌────────────┐                  │              │  │
│  │  Service A │  Register        │              │  │
│  │  Instance2 │ ───────────────> │              │  │
│  └────────────┘                  └──────────────┘  │
│                                                     │
│  2. SERVICE DISCOVERY                               │
│  ┌────────────┐                  ┌──────────────┐  │
│  │  Service B │  Discover        │              │  │
│  │            │ <─────────────── │    Eureka    │  │
│  │            │  "Service A"     │    Server    │  │
│  └────────────┘                  └──────────────┘  │
│       │                                             │
│       │  3. DIRECT CALL                             │
│       │  ┌────────────┐                             │
│       └─>│  Service A │                             │
│          │  Instance1 │                             │
│          └────────────┘                             │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### Client-Side vs Server-Side Discovery

**Client-Side Discovery (Eureka):**
- Client queries registry
- Client chooses instance
- Client makes direct call
- Example: Netflix Eureka

**Server-Side Discovery:**
- Client calls load balancer
- Load balancer queries registry
- Load balancer forwards request
- Example: AWS ELB, Nginx

## Netflix Eureka Overview

### What is Eureka?

**Netflix Eureka** is a REST-based service registry developed by Netflix for resilient mid-tier load balancing and failover.

### Key Components

1. **Eureka Server**: Service registry
2. **Eureka Client**: Services that register with Eureka
3. **Service Registry**: Database of available services
4. **Service Discovery**: Mechanism to find services

### Eureka Features

- **Self-Preservation**: Protects against network partitions
- **Replication**: Multiple Eureka servers for high availability
- **REST-Based**: Simple HTTP API
- **Dashboard**: Web UI for monitoring
- **Health Checks**: Monitor service health
- **Metadata**: Store custom service metadata

## Setting Up Eureka Server

### 1. Create Eureka Server Project

**pom.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>eureka-server</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
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

### 2. Enable Eureka Server

**EurekaServerApplication.java:**
```java
package com.example.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer  // Enable Eureka Server
public class EurekaServerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

### 3. Configure Eureka Server

**application.yml:**
```yaml
server:
  port: 8761  # Default Eureka port

spring:
  application:
    name: eureka-server

eureka:
  instance:
    hostname: localhost
  
  client:
    # Don't register itself as a client
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
  
  server:
    # Disable self-preservation for development
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 3000
```

### 4. Run Eureka Server

```bash
mvn spring-boot:run

# Access Eureka Dashboard
# http://localhost:8761
```

## Registering Services with Eureka

### 1. Add Eureka Client Dependency

**pom.xml:**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
</dependencies>
```

### 2. Enable Eureka Client

**UserServiceApplication.java:**
```java
package com.example.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient  // Enable service registration
public class UserServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

### 3. Configure Service Registration

**application.yml:**
```yaml
server:
  port: 8081

spring:
  application:
    name: user-service  # Service name in Eureka

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    
    # Fetch registry from Eureka Server
    fetch-registry: true
    
    # Register this service with Eureka
    register-with-eureka: true
  
  instance:
    # Prefer IP address over hostname
    prefer-ip-address: true
    
    # Instance ID format
    instance-id: ${spring.application.name}:${random.value}
    
    # Heartbeat interval (default: 30s)
    lease-renewal-interval-in-seconds: 10
    
    # Time Eureka waits before removing instance (default: 90s)
    lease-expiration-duration-in-seconds: 30
    
    # Metadata for this instance
    metadata-map:
      version: 1.0.0
      environment: dev
```

### 4. Create REST Controller

**UserController.java:**
```java
package com.example.userservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Value("${server.port}")
    private String port;

    @GetMapping
    public List<User> getUsers() {
        return List.of(
            new User(1L, "John Doe", "john@example.com"),
            new User(2L, "Jane Smith", "jane@example.com")
        );
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return new User(id, "User " + id, "user" + id + "@example.com");
    }

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "service", "user-service",
            "port", port
        );
    }
}
```

## Service Discovery Client

### 1. Using RestTemplate with Load Balancing

**OrderServiceApplication.java:**
```java
package com.example.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class OrderServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
    
    @Bean
    @LoadBalanced  // Enable client-side load balancing
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

**OrderService.java:**
```java
package com.example.orderservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderService {

    private final RestTemplate restTemplate;

    public OrderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public User getUserById(Long userId) {
        // Use service name instead of hardcoded URL
        String url = "http://user-service/users/" + userId;
        return restTemplate.getForObject(url, User.class);
    }

    public List<User> getAllUsers() {
        String url = "http://user-service/users";
        User[] users = restTemplate.getForArray(url, User[].class);
        return Arrays.asList(users);
    }
}
```

### 2. Using WebClient (Reactive)

**Configuration:**
```java
@Configuration
public class WebClientConfig {
    
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
```

**Usage:**
```java
@Service
public class OrderService {

    private final WebClient.Builder webClientBuilder;

    public OrderService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<User> getUserById(Long userId) {
        return webClientBuilder.build()
                .get()
                .uri("http://user-service/users/" + userId)
                .retrieve()
                .bodyToMono(User.class);
    }

    public Flux<User> getAllUsers() {
        return webClientBuilder.build()
                .get()
                .uri("http://user-service/users")
                .retrieve()
                .bodyToFlux(User.class);
    }
}
```

### 3. Using DiscoveryClient (Manual)

**OrderService.java:**
```java
package com.example.orderservice.service;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OrderService {

    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;

    public OrderService(DiscoveryClient discoveryClient, RestTemplate restTemplate) {
        this.discoveryClient = discoveryClient;
        this.restTemplate = new RestTemplate();
    }

    public User getUserById(Long userId) {
        // Get all instances of user-service
        List<ServiceInstance> instances = discoveryClient.getInstances("user-service");
        
        if (instances.isEmpty()) {
            throw new IllegalStateException("No instances of user-service available");
        }
        
        // Choose first instance (or implement custom load balancing)
        ServiceInstance instance = instances.get(0);
        String url = instance.getUri() + "/users/" + userId;
        
        return restTemplate.getForObject(url, User.class);
    }

    public void printAvailableServices() {
        List<String> services = discoveryClient.getServices();
        services.forEach(service -> {
            System.out.println("Service: " + service);
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            instances.forEach(instance -> {
                System.out.println("  Instance: " + instance.getUri());
            });
        });
    }
}
```

### 4. Using Feign Client (Declarative)

**Add Feign Dependency:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

**Enable Feign:**
```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

**Feign Client Interface:**
```java
package com.example.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/users")
    List<User> getAllUsers();

    @GetMapping("/users/{id}")
    User getUserById(@PathVariable("id") Long id);
}
```

**Usage:**
```java
@Service
public class OrderService {

    private final UserClient userClient;

    public OrderService(UserClient userClient) {
        this.userClient = userClient;
    }

    public Order createOrder(Long userId, OrderRequest request) {
        // Automatically discovers and calls user-service
        User user = userClient.getUserById(userId);
        
        // Create order logic
        Order order = new Order();
        order.setUserId(user.getId());
        order.setUserName(user.getName());
        // ... more order logic
        
        return order;
    }
}
```

## Multiple Eureka Instances

### High Availability Setup

**Eureka Server 1 (application-peer1.yml):**
```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server
  profiles:
    active: peer1

eureka:
  instance:
    hostname: eureka-peer1
  
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://eureka-peer2:8762/eureka/
```

**Eureka Server 2 (application-peer2.yml):**
```yaml
server:
  port: 8762

spring:
  application:
    name: eureka-server
  profiles:
    active: peer2

eureka:
  instance:
    hostname: eureka-peer2
  
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://eureka-peer1:8761/eureka/
```

**Client Configuration:**
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka-peer1:8761/eureka/,http://eureka-peer2:8762/eureka/
```

## Self-Preservation Mode

### What is Self-Preservation?

Eureka enters self-preservation mode when it detects that more than 85% of registered instances fail to send heartbeats.

**Purpose:**
- Protect against network partitions
- Prevent mass de-registration during network issues
- Keep service registry stable

**Behavior:**
- Stops evicting instances
- Shows warning in dashboard
- Continues accepting new registrations

**Configuration:**
```yaml
eureka:
  server:
    # Enable/disable self-preservation
    enable-self-preservation: true
    
    # Threshold percentage (default: 0.85)
    renewal-percent-threshold: 0.85
    
    # How often to check for expired leases
    eviction-interval-timer-in-ms: 60000
```

**Production Setting:**
```yaml
# ✅ Enable in production
eureka:
  server:
    enable-self-preservation: true
```

**Development Setting:**
```yaml
# ⚠️ Disable in development for faster testing
eureka:
  server:
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 3000
```

## Health Checks

### Eureka Health Check

**Add Actuator Dependency:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Enable Health Check:**
```yaml
eureka:
  client:
    healthcheck:
      enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info
  
  endpoint:
    health:
      show-details: always
```

### Custom Health Indicator

**DatabaseHealthIndicator.java:**
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1000)) {
                return Health.up()
                        .withDetail("database", "Available")
                        .build();
            }
        } catch (SQLException e) {
            return Health.down()
                    .withDetail("database", "Unavailable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
        return Health.down().build();
    }
}
```

## Eureka Dashboard

### Accessing Dashboard

```
http://localhost:8761
```

### Dashboard Information

**General Info:**
- Environment
- Data center
- Current time

**Instances Currently Registered:**
- Application name
- AMI ID
- Availability zones
- Status (UP, DOWN, OUT_OF_SERVICE)
- Number of instances

**General Information:**
- Renews threshold
- Renews (last minute)
- Self-preservation mode status

**Instance Info:**
- Instance ID
- Status
- Homepage URL
- Health check URL
- Status page URL
- Metadata

## Best Practices

### 1. Use Meaningful Service Names

```yaml
# ✅ Clear, descriptive names
spring:
  application:
    name: user-service

# ❌ Vague names
spring:
  application:
    name: service1
```

### 2. Configure Heartbeat Intervals

```yaml
# ✅ Production settings
eureka:
  instance:
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90

# ⚠️ Development settings (faster detection)
eureka:
  instance:
    lease-renewal-interval-in-seconds: 5
    lease-expiration-duration-in-seconds: 10
```

### 3. Use IP Address in Containers

```yaml
# ✅ Use IP in containerized environments
eureka:
  instance:
    prefer-ip-address: true
    ip-address: ${MY_POD_IP}
```

### 4. Enable Health Checks

```yaml
# ✅ Always enable health checks
eureka:
  client:
    healthcheck:
      enabled: true
```

### 5. Secure Eureka Server

**Add Security Dependency:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**Security Configuration:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .httpBasic();
        
        return http.build();
    }
}
```

**Client Configuration:**
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://username:password@localhost:8761/eureka/
```

### 6. Use Zones for Availability

```yaml
eureka:
  instance:
    metadata-map:
      zone: us-east-1a
  
  client:
    availability-zones:
      region1: zone1,zone2
    region: region1
    prefer-same-zone-eureka: true
```

## Complete Example

### Project Structure

```
microservices-demo/
├── eureka-server/
│   ├── pom.xml
│   └── src/main/
│       ├── java/
│       │   └── EurekaServerApplication.java
│       └── resources/
│           └── application.yml
├── user-service/
│   ├── pom.xml
│   └── src/main/
│       ├── java/
│       │   ├── UserServiceApplication.java
│       │   ├── controller/
│       │   │   └── UserController.java
│       │   └── model/
│       │       └── User.java
│       └── resources/
│           └── application.yml
└── order-service/
    ├── pom.xml
    └── src/main/
        ├── java/
        │   ├── OrderServiceApplication.java
        │   ├── controller/
        │   │   └── OrderController.java
        │   ├── service/
        │   │   └── OrderService.java
        │   └── client/
        │       └── UserClient.java
        └── resources/
            └── application.yml
```

### Running the Example

```bash
# 1. Start Eureka Server
cd eureka-server
mvn spring-boot:run

# 2. Start User Service (multiple instances)
cd user-service
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8082

# 3. Start Order Service
cd order-service
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8083

# 4. Access Eureka Dashboard
# http://localhost:8761

# 5. Test service discovery
curl http://localhost:8083/orders/1
```

## Exercises

### Exercise 1: Basic Eureka Setup
1. Create Eureka Server
2. Create two services (user-service, product-service)
3. Register both services with Eureka
4. Verify registration in dashboard

### Exercise 2: Service Communication
1. Make order-service call user-service
2. Use RestTemplate with @LoadBalanced
3. Test with multiple user-service instances
4. Verify load balancing

### Exercise 3: Feign Client
1. Add Feign dependency to order-service
2. Create Feign client for user-service
3. Replace RestTemplate calls with Feign
4. Test service communication

### Exercise 4: High Availability
1. Set up two Eureka servers
2. Configure peer-to-peer replication
3. Test failover scenario
4. Verify service discovery works with one server down

### Exercise 5: Custom Metadata
1. Add custom metadata to service registration
2. Retrieve metadata in client
3. Implement custom routing based on metadata
4. Add versioning metadata

## Key Takeaways

1. **Service Discovery** eliminates hard-coded service locations
2. **Eureka Server** acts as service registry
3. **Eureka Client** registers and discovers services
4. **@LoadBalanced** enables client-side load balancing
5. **Self-Preservation** protects against network partitions
6. **Health Checks** ensure only healthy instances receive traffic
7. **Multiple Instances** provide high availability
8. **Feign** simplifies service-to-service communication
9. **DiscoveryClient** provides programmatic service discovery
10. **Zones** enable geographic distribution

## Resources

### Official Documentation
- [Spring Cloud Netflix](https://spring.io/projects/spring-cloud-netflix)
- [Eureka Wiki](https://github.com/Netflix/eureka/wiki)
- [Spring Cloud Discovery](https://spring.io/guides/gs/service-registration-and-discovery/)

### Tutorials
- [Eureka Service Discovery](https://www.baeldung.com/spring-cloud-netflix-eureka)
- [Microservices with Eureka](https://spring.io/blog/2015/07/14/microservices-with-spring)

### Tools
- [Eureka Dashboard](http://localhost:8761)
- [Spring Cloud CLI](https://cloud.spring.io/spring-cloud-cli/)

---
[<< Previous: Day 12](../Day12_Microservices_Intro/README.md) | [Back to Main](../README.md) | [Next: Day 14 >>](../Day14_API_Gateway/README.md)
