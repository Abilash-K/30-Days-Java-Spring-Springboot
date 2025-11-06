# Day 13: Service Discovery with Eureka

## 📋 Table of Contents
- [Introduction](#introduction)
- [What is Service Discovery?](#what-is-service-discovery)
- [Service Registry Pattern](#service-registry-pattern)
- [Netflix Eureka Architecture](#netflix-eureka-architecture)
- [Setting Up Eureka Server](#setting-up-eureka-server)
- [Eureka Client Configuration](#eureka-client-configuration)
- [Service Registration](#service-registration)
- [Service Discovery](#service-discovery)
- [High Availability](#high-availability)
- [Health Checks](#health-checks)
- [Security](#security)
- [Eureka Dashboard](#eureka-dashboard)
- [Best Practices](#best-practices)
- [Complete Example](#complete-example)
- [Exercises](#exercises)
- [Key Takeaways](#key-takeaways)
- [Resources](#resources)

## Introduction

Welcome to Day 13! Today we'll explore **Netflix Eureka**, a powerful service discovery solution for microservices architecture.

### What You'll Learn
- Understanding service discovery patterns
- Setting up Eureka Server
- Registering services with Eureka
- Discovering and consuming services
- High availability configuration
- Security best practices

### Why Service Discovery Matters
- ✅ **Dynamic Discovery**: Services find each other automatically
- ✅ **Scalability**: Add/remove service instances easily
- ✅ **Fault Tolerance**: Automatic failover to healthy instances
- ✅ **Load Balancing**: Distribute requests across instances
- ✅ **Cloud-Native**: Essential for containerized environments

## What is Service Discovery?

In microservices, services need to communicate with each other. Service discovery solves the problem of finding network locations of service instances.

### Traditional Approach (Manual Configuration)

```
Service A wants to call Service B

application.yml (Service A):
service-b:
  url: http://192.168.1.10:8081  # ❌ Hardcoded IP
  
Problems:
- Manual updates when IPs change
- No load balancing
- No health checks
- Single point of failure
```

### Service Discovery Approach

```
Service A wants to call Service B

1. Service B registers with Eureka Server
   - Name: "service-b"
   - IP: 192.168.1.10
   - Port: 8081
   - Status: UP

2. Service A asks Eureka "Where is service-b?"
   - Eureka returns: http://192.168.1.10:8081

3. Service A calls Service B using the discovered URL

Benefits:
✅ Dynamic discovery
✅ Automatic load balancing
✅ Health monitoring
✅ Fault tolerance
```

## Service Registry Pattern

### Architecture Overview

```
┌────────────────────────────────────────────────────────────┐
│                    Eureka Server                            │
│                  (Service Registry)                         │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  Registered Services:                                 │ │
│  │  - user-service: [192.168.1.10:8081, 192.168.1.11]  │ │
│  │  - order-service: [192.168.1.12:8082]               │ │
│  │  - product-service: [192.168.1.13:8083]             │ │
│  └──────────────────────────────────────────────────────┘ │
└─────────────────────────┬──────────────────────────────────┘
                          │
         ┌────────────────┼────────────────┐
         │                │                │
    ┌────▼────┐     ┌────▼────┐     ┌────▼────┐
    │  User   │     │ Order   │     │ Product │
    │ Service │     │ Service │     │ Service │
    └────┬────┘     └────┬────┘     └─────────┘
         │               │
         │  Discover &   │
         │  Call         │
         └──────────────►┘
```

### Client-Side Discovery

```
1. Services register with Eureka Server (Service Registry)
2. Clients query Eureka to get service locations
3. Client chooses an instance (client-side load balancing)
4. Client calls the service directly
```

**Pros:**
- Clients have full control
- No extra network hop
- Client-side load balancing

**Cons:**
- Client must implement discovery logic
- Multiple language implementations needed

## Netflix Eureka Architecture

### Components

**1. Eureka Server:**
- Service registry
- Stores service metadata
- Provides REST API for registration and discovery
- Heartbeat receiver

**2. Eureka Client:**
- Registers with Eureka Server
- Sends heartbeats every 30 seconds
- Fetches registry from server
- Caches service locations locally

### Registration and Discovery Flow

```
Step 1: Service Registration
Service Instance → POST /eureka/apps/{app-name} → Eureka Server

Step 2: Heartbeat
Service Instance → PUT /eureka/apps/{app-name}/{instance-id} → Eureka Server
(Every 30 seconds)

Step 3: Service Discovery
Client → GET /eureka/apps → Eureka Server
(Returns list of all services)

Client → GET /eureka/apps/{app-name} → Eureka Server
(Returns specific service instances)

Step 4: Local Caching
Client caches service locations locally
Refreshes cache every 30 seconds
```

## Setting Up Eureka Server

### 1. Create Eureka Server Project

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
    <artifactId>eureka-server</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2022.0.3</spring-cloud.version>
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

```java
package com.example.eureka;

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
  client:
    register-with-eureka: false  # Don't register itself
    fetch-registry: false  # Don't fetch registry
    service-url:
      defaultZone: http://localhost:8761/eureka/
  server:
    enable-self-preservation: false  # Disable in dev (enable in prod)
    eviction-interval-timer-in-ms: 4000  # Cleanup interval
```

### 4. Run Eureka Server

```bash
mvn spring-boot:run
```

Access Eureka Dashboard at: `http://localhost:8761`

## Eureka Client Configuration

### 1. Add Eureka Client Dependency

**Maven (pom.xml):**
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

```java
package com.example.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient  // Enable Eureka Client (optional in newer versions)
public class UserServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

### 3. Configure Eureka Client

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
      defaultZone: http://localhost:8761/eureka/  # Eureka Server URL
    fetch-registry: true  # Fetch other services
    register-with-eureka: true  # Register this service
  instance:
    hostname: localhost
    prefer-ip-address: true  # Use IP address instead of hostname
    lease-renewal-interval-in-seconds: 30  # Heartbeat interval
    lease-expiration-duration-in-seconds: 90  # Eviction time
```

### 4. Multiple Eureka Server URLs

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka1:8761/eureka/,http://eureka2:8762/eureka/
```

## Service Registration

### Automatic Registration

When you start a Eureka client, it automatically registers with the Eureka server.

```
Console Output:
DiscoveryClient_USER-SERVICE: registering service...
DiscoveryClient_USER-SERVICE: registration status: 204
```

### Custom Instance Metadata

```yaml
eureka:
  instance:
    metadata-map:
      zone: us-east-1a
      version: 1.0.0
      team: platform
      custom-key: custom-value
```

### Programmatic Registration Info

```java
package com.example.userservice.config;

import com.netflix.appinfo.InstanceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class EurekaConfig {
    
    @Bean
    public EurekaInstanceConfigBean eurekaInstanceConfig() {
        EurekaInstanceConfigBean config = new EurekaInstanceConfigBean();
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("version", "1.0.0");
        metadata.put("stage", "production");
        config.setMetadataMap(metadata);
        
        return config;
    }
}
```

## Service Discovery

### Using RestTemplate with Load Balancing

```java
package com.example.orderservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    
    @Bean
    @LoadBalanced  // Enable client-side load balancing
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

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
    
    public UserDto getUser(Long userId) {
        // Use service name instead of URL
        String url = "http://user-service/api/users/" + userId;
        return restTemplate.getForObject(url, UserDto.class);
    }
}
```

### Using WebClient (Reactive)

```java
package com.example.orderservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
```

```java
package com.example.orderservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ReactiveOrderService {
    
    private final WebClient.Builder webClientBuilder;
    
    public ReactiveOrderService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }
    
    public Mono<UserDto> getUser(Long userId) {
        return webClientBuilder.build()
                .get()
                .uri("http://user-service/api/users/" + userId)
                .retrieve()
                .bodyToMono(UserDto.class);
    }
}
```

### Using OpenFeign

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

```java
@SpringBootApplication
@EnableFeignClients  // Enable Feign clients
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

```java
package com.example.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")  // Service name in Eureka
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}")
    UserDto getUser(@PathVariable("id") Long id);
}
```

```java
package com.example.orderservice.service;

import com.example.orderservice.client.UserServiceClient;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceWithFeign {
    
    private final UserServiceClient userServiceClient;
    
    public OrderServiceWithFeign(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }
    
    public UserDto getUser(Long userId) {
        return userServiceClient.getUser(userId);
    }
}
```

### Manual Service Discovery

```java
package com.example.orderservice.service;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ManualDiscoveryService {
    
    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;
    
    public ManualDiscoveryService(DiscoveryClient discoveryClient, RestTemplate restTemplate) {
        this.discoveryClient = discoveryClient;
        this.restTemplate = restTemplate;
    }
    
    public UserDto getUserManually(Long userId) {
        // Get all instances of user-service
        List<ServiceInstance> instances = discoveryClient.getInstances("user-service");
        
        if (instances.isEmpty()) {
            throw new RuntimeException("No instances of user-service available");
        }
        
        // Choose first instance (or implement custom load balancing)
        ServiceInstance instance = instances.get(0);
        String url = instance.getUri() + "/api/users/" + userId;
        
        return restTemplate.getForObject(url, UserDto.class);
    }
    
    public void printAllServices() {
        List<String> services = discoveryClient.getServices();
        
        System.out.println("Discovered services:");
        for (String service : services) {
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            System.out.println("  " + service + ":");
            for (ServiceInstance instance : instances) {
                System.out.println("    - " + instance.getUri());
            }
        }
    }
}
```

## High Availability

### Eureka Server Cluster

#### Server 1 Configuration

**application-peer1.yml:**
```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  instance:
    hostname: peer1
  client:
    register-with-eureka: true  # Register with other peers
    fetch-registry: true  # Fetch from other peers
    service-url:
      defaultZone: http://peer2:8762/eureka/
```

#### Server 2 Configuration

**application-peer2.yml:**
```yaml
server:
  port: 8762

spring:
  application:
    name: eureka-server

eureka:
  instance:
    hostname: peer2
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://peer1:8761/eureka/
```

#### Run Multiple Servers

```bash
# Terminal 1
java -jar eureka-server.jar --spring.profiles.active=peer1

# Terminal 2
java -jar eureka-server.jar --spring.profiles.active=peer2
```

#### Client Configuration for HA

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://peer1:8761/eureka/,http://peer2:8762/eureka/
```

## Health Checks

### Custom Health Indicator

```java
package com.example.userservice.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // Check application health
        boolean isHealthy = checkDatabaseConnection() && checkExternalAPI();
        
        if (isHealthy) {
            return Health.up()
                    .withDetail("database", "connected")
                    .withDetail("api", "available")
                    .build();
        } else {
            return Health.down()
                    .withDetail("error", "Service unhealthy")
                    .build();
        }
    }
    
    private boolean checkDatabaseConnection() {
        // Check database
        return true;
    }
    
    private boolean checkExternalAPI() {
        // Check external API
        return true;
    }
}
```

### Health Check URL

```yaml
eureka:
  instance:
    health-check-url-path: /actuator/health
    status-page-url-path: /actuator/info
```

## Security

### Secure Eureka Server

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

```java
package com.example.eureka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests()
            .anyRequest().authenticated()
            .and()
            .httpBasic();
        
        return http.build();
    }
    
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("eureka")
                .password("password")
                .roles("ADMIN")
                .build();
        
        return new InMemoryUserDetailsManager(user);
    }
}
```

**application.yml:**
```yaml
spring:
  security:
    user:
      name: eureka
      password: password
```

### Client with Security

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka:password@localhost:8761/eureka/
```

## Eureka Dashboard

### Accessing the Dashboard

Navigate to: `http://localhost:8761`

### Dashboard Information

**1. System Status:**
- Environment
- Data center
- Current time
- Uptime
- Lease expiration enabled
- Renews threshold
- Renews (last min)

**2. DS Replicas:**
- List of peer Eureka servers

**3. Instances Currently Registered:**
- Application name
- AMI ID
- Availability Zones
- Status (UP, DOWN, STARTING, OUT_OF_SERVICE)
- Instance count

**4. General Info:**
- Total memory
- Environment
- CPU count

**5. Instance Info:**
- IP address
- Status
- Metadata
- Homepage URL
- Health check URL

## Best Practices

### 1. Use IP Address Instead of Hostname

```yaml
eureka:
  instance:
    prefer-ip-address: true
```

### 2. Configure Appropriate Timeouts

```yaml
eureka:
  instance:
    lease-renewal-interval-in-seconds: 30  # Heartbeat interval
    lease-expiration-duration-in-seconds: 90  # Eviction timeout
```

### 3. Enable Self-Preservation in Production

```yaml
eureka:
  server:
    enable-self-preservation: true  # Prevent mass deregistration
```

### 4. Configure Cache Refresh

```yaml
eureka:
  client:
    registry-fetch-interval-seconds: 30  # How often to fetch registry
```

### 5. Health Check Configuration

```yaml
eureka:
  instance:
    health-check-url-path: /actuator/health
  client:
    healthcheck:
      enabled: true
```

### 6. Use Eureka Zones for Regional Deployment

```yaml
eureka:
  instance:
    metadata-map:
      zone: us-east-1a
  client:
    prefer-same-zone-eureka: true
```

### 7. Implement Proper Shutdown

```java
@PreDestroy
public void onShutdown() {
    eurekaClient.shutdown();
}
```

### 8. Monitor Eureka Server

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

## Complete Example

### Project Structure

```
microservices-ecosystem/
├── eureka-server/
│   ├── pom.xml
│   └── src/main/
│       ├── java/
│       │   └── com/example/eureka/
│       │       └── EurekaServerApplication.java
│       └── resources/
│           └── application.yml
├── user-service/
│   ├── pom.xml
│   └── src/main/
│       ├── java/
│       │   └── com/example/userservice/
│       │       ├── UserServiceApplication.java
│       │       ├── controller/
│       │       ├── service/
│       │       └── model/
│       └── resources/
│           └── application.yml
└── order-service/
    ├── pom.xml
    └── src/main/
        ├── java/
        │   └── com/example/orderservice/
        │       ├── OrderServiceApplication.java
        │       ├── controller/
        │       ├── service/
        │       ├── client/
        │       └── model/
        └── resources/
            └── application.yml
```

### Complete User Service

```java
package com.example.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

```java
package com.example.userservice.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public Map<String, Object> getUser(@PathVariable Long id) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("name", "John Doe");
        user.put("email", "john@example.com");
        return user;
    }
}
```

**application.yml:**
```yaml
server:
  port: 8081

spring:
  application:
    name: user-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

### Complete Order Service with Feign

```java
package com.example.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

```java
package com.example.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}")
    Map<String, Object> getUser(@PathVariable("id") Long id);
}
```

```java
package com.example.orderservice.controller;

import com.example.orderservice.client.UserServiceClient;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private final UserServiceClient userServiceClient;
    
    public OrderController(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }
    
    @GetMapping("/{id}")
    public Map<String, Object> getOrder(@PathVariable Long id) {
        // Get user details from user-service
        Map<String, Object> user = userServiceClient.getUser(1L);
        
        Map<String, Object> order = new HashMap<>();
        order.put("id", id);
        order.put("product", "Laptop");
        order.put("quantity", 1);
        order.put("user", user);
        
        return order;
    }
}
```

**application.yml:**
```yaml
server:
  port: 8082

spring:
  application:
    name: order-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

### Running the Example

```bash
# 1. Start Eureka Server
cd eureka-server
mvn spring-boot:run

# 2. Start User Service
cd user-service
mvn spring-boot:run

# 3. Start Order Service
cd order-service
mvn spring-boot:run

# 4. Access Eureka Dashboard
# http://localhost:8761

# 5. Test Order Service
curl http://localhost:8082/api/orders/1
```

## Exercises

### Exercise 1: Basic Eureka Setup
Create Eureka Server and register two services.

**Requirements:**
1. Create Eureka Server on port 8761
2. Create product-service on port 8083
3. Create cart-service on port 8084
4. Register both services with Eureka
5. Verify registration in Eureka Dashboard

### Exercise 2: Service Communication
Implement service-to-service communication using RestTemplate.

**Requirements:**
1. cart-service calls product-service
2. Use @LoadBalanced RestTemplate
3. Implement GET /api/cart/{id} endpoint
4. Fetch product details from product-service
5. Return combined cart and product data

### Exercise 3: Feign Client
Implement Feign client for service communication.

**Requirements:**
1. Create ProductServiceClient interface
2. Use @FeignClient annotation
3. Implement multiple endpoints (GET, POST)
4. Add error handling with fallback
5. Test with multiple service instances

### Exercise 4: High Availability
Set up Eureka Server cluster with two instances.

**Requirements:**
1. Create two Eureka Server profiles
2. Configure peer-to-peer replication
3. Update client to use both servers
4. Test failover (stop one server)
5. Verify services remain discoverable

### Exercise 5: Secure Eureka
Add security to Eureka Server and clients.

**Requirements:**
1. Add Spring Security to Eureka Server
2. Configure basic authentication
3. Update clients with credentials
4. Test authentication
5. Implement custom security configuration

## Key Takeaways

1. **Service Discovery Eliminates Hardcoded URLs**
   - Services register dynamically
   - Clients discover services automatically
   - Easy to scale and deploy

2. **Eureka Provides Self-Service Registration**
   - Services auto-register on startup
   - Heartbeats maintain registration
   - Auto-deregistration on shutdown

3. **Client-Side Load Balancing**
   - Clients choose service instances
   - No extra network hop
   - Integrated with RestTemplate and Feign

4. **High Availability is Essential**
   - Run multiple Eureka servers
   - Configure peer-to-peer replication
   - Clients can connect to multiple servers

5. **Health Checks Keep Registry Clean**
   - Services send heartbeats
   - Unhealthy instances removed
   - Clients always get healthy instances

6. **Eureka Dashboard Provides Visibility**
   - See all registered services
   - Monitor instance status
   - View metadata and configuration

## Resources

### Official Documentation
- [Spring Cloud Netflix Eureka](https://spring.io/projects/spring-cloud-netflix)
- [Eureka Wiki](https://github.com/Netflix/eureka/wiki)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)

### Tutorials
- [Microservices with Spring Boot and Eureka](https://www.baeldung.com/spring-cloud-netflix-eureka)
- [Service Discovery with Eureka](https://spring.io/guides/gs/service-registration-and-discovery/)
- [Eureka Best Practices](https://www.baeldung.com/eureka-self-preservation-renewal)

### Tools
- [Eureka Dashboard](http://localhost:8761)
- [Spring Cloud CLI](https://spring.io/projects/spring-cloud-cli)
- [Netflix OSS](https://netflix.github.io/)

---
[<< Previous: Day 12](../Day12_Microservices_Intro/README.md) | [Back to Main](../README.md) | [Next: Day 14 >>](../Day14_API_Gateway/README.md)
