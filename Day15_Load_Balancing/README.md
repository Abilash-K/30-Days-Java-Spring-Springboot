# Day 15: Load Balancing and Client-Side Load Balancing

## 📋 Table of Contents
- [Introduction](#introduction)
- [Load Balancing Fundamentals](#load-balancing-fundamentals)
- [Load Balancing Strategies](#load-balancing-strategies)
- [Spring Cloud LoadBalancer](#spring-cloud-loadbalancer)
- [Client-Side Load Balancing](#client-side-load-balancing)
- [RestTemplate with Load Balancing](#resttemplate-with-load-balancing)
- [WebClient with Load Balancing](#webclient-with-load-balancing)
- [Feign Client with Load Balancing](#feign-client-with-load-balancing)
- [Custom Load Balancer](#custom-load-balancer)
- [Health-Based Load Balancing](#health-based-load-balancing)
- [Complete Example](#complete-example)
- [Best Practices](#best-practices)
- [Exercises](#exercises)
- [Key Takeaways](#key-takeaways)
- [Resources](#resources)

## Introduction

Welcome to Day 15! Today we'll explore **Load Balancing** - a critical pattern for distributing traffic across multiple service instances to achieve scalability, high availability, and reliability.

### What You'll Learn
- Understanding load balancing concepts
- Different load balancing strategies
- Client-side vs server-side load balancing
- Spring Cloud LoadBalancer configuration
- Using RestTemplate with load balancing
- Using WebClient for reactive load balancing
- Implementing custom load balancers
- Health-aware load balancing

### Why Load Balancing Matters
- ✅ **Scalability**: Handle more requests by adding instances
- ✅ **High Availability**: Failover when instances go down
- ✅ **Performance**: Distribute load evenly across instances
- ✅ **Reliability**: No single point of failure
- ✅ **Resource Optimization**: Efficient use of server resources
- ✅ **Zero Downtime**: Rolling deployments without interruption

## Load Balancing Fundamentals

### Server-Side Load Balancing (Traditional)

```
┌──────────┐                    ┌─────────────────┐
│  Client  │───────────────────>│  Load Balancer  │
│          │                    │   (Hardware/    │
└──────────┘                    │    Software)    │
                                └────────┬────────┘
                                         │
                        ┌────────────────┼────────────────┐
                        │                │                │
                        ▼                ▼                ▼
                ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
                │   Service    │ │   Service    │ │   Service    │
                │  Instance 1  │ │  Instance 2  │ │  Instance 3  │
                └──────────────┘ └──────────────┘ └──────────────┘

Characteristics:
• Centralized load balancer
• Client is unaware of service instances
• Examples: Nginx, HAProxy, AWS ELB
```

### Client-Side Load Balancing (Microservices)

```
┌──────────┐
│  Client  │
│  with    │
│  Load    │
│ Balancer │
└────┬─────┘
     │
     │  ┌─────────────┐
     │  │   Service   │
     │  │  Registry   │
     │  │  (Eureka)   │
     │  └─────────────┘
     │         │
     │    Get instances
     │         │
     └─────────┼─────────────────────────────────┐
               │                                 │
               ▼                                 ▼
       ┌──────────────┐                 ┌──────────────┐
       │   Service    │                 │   Service    │
       │  Instance 1  │                 │  Instance 2  │
       └──────────────┘                 └──────────────┘

Characteristics:
• Client chooses which instance to call
• Aware of all available instances
• Intelligent routing decisions
• No central bottleneck
```

### Comparison

| Aspect | Server-Side | Client-Side |
|--------|-------------|-------------|
| **Location** | Separate component | Embedded in client |
| **Network Hops** | +1 hop | Direct |
| **Single Point of Failure** | Yes | No |
| **Latency** | Higher | Lower |
| **Complexity** | Simple client | Smart client |
| **Service Discovery** | Not required | Required |
| **Best For** | Traditional apps | Microservices |

## Load Balancing Strategies

### 1. Round Robin

**How it works:** Distributes requests sequentially across instances.

```
Request 1 → Instance 1
Request 2 → Instance 2
Request 3 → Instance 3
Request 4 → Instance 1  (cycles back)
Request 5 → Instance 2
```

**Pros:**
- Simple and fair distribution
- Works well when instances are equal

**Cons:**
- Doesn't consider instance load
- No session affinity

**Use Case:** Stateless services with similar capacity

### 2. Weighted Round Robin

**How it works:** Assigns weights to instances based on capacity.

```
Instance 1 (weight=3): ███
Instance 2 (weight=1): █
Instance 3 (weight=2): ██

Request 1 → Instance 1
Request 2 → Instance 1
Request 3 → Instance 1
Request 4 → Instance 2
Request 5 → Instance 3
Request 6 → Instance 3
```

**Use Case:** Instances with different capacities

### 3. Random

**How it works:** Randomly selects an instance.

```
Request 1 → Instance 2 (random)
Request 2 → Instance 1 (random)
Request 3 → Instance 2 (random)
Request 4 → Instance 3 (random)
```

**Pros:**
- Simple implementation
- Good distribution over time

**Cons:**
- No predictability
- Might have uneven distribution short-term

### 4. Least Connections

**How it works:** Routes to instance with fewest active connections.

```
Instance 1: 5 connections
Instance 2: 2 connections  ← Routes here
Instance 3: 8 connections
```

**Use Case:** Long-lived connections, WebSocket

### 5. Response Time / Weighted Response Time

**How it works:** Routes to instance with fastest response time.

```
Instance 1: 100ms avg
Instance 2: 50ms avg   ← Routes here (fastest)
Instance 3: 150ms avg
```

**Use Case:** Performance-critical applications

### 6. Consistent Hashing

**How it works:** Uses hash of request attribute (user ID, session ID) to determine instance.

```
Hash(user123) → Instance 2
Hash(user456) → Instance 1
Hash(user123) → Instance 2  (same user, same instance)
```

**Use Case:** Session affinity, distributed caching

### 7. Zone-Aware

**How it works:** Prefers instances in same availability zone/region.

```
Client in Zone A:
  First try: Instances in Zone A
  Fallback:  Instances in other zones
```

**Use Case:** Multi-zone deployments, reducing latency

## Spring Cloud LoadBalancer

### 1. Setup and Dependencies

**pom.xml**
```xml
<dependencies>
    <!-- Spring Cloud LoadBalancer -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
    
    <!-- Eureka Client for Service Discovery -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    
    <!-- Spring Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- WebFlux for WebClient -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
</dependencies>
```

**Note:** Spring Cloud LoadBalancer replaced Netflix Ribbon (deprecated).

### 2. Configuration

**application.yml**
```yaml
spring:
  application:
    name: order-service
  cloud:
    loadbalancer:
      # Cache configurations
      cache:
        enabled: true
        ttl: 35s  # Cache instance list for 35 seconds
        capacity: 256
        
      # Health check configurations
      health-check:
        initial-delay: 0
        interval: 25s
        
      # Retry configurations
      retry:
        enabled: true
        max-retries-on-same-service-instance: 1
        max-retries-on-next-service-instance: 2
        
      # Zone preference
      zone: zone1

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    metadata-map:
      zone: zone1  # Instance zone

# Enable debug logging
logging:
  level:
    org.springframework.cloud.loadbalancer: DEBUG
```

### 3. Basic Usage

**LoadBalancerApplication.java**
```java
package com.example.loadbalancer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LoadBalancerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(LoadBalancerApplication.class, args);
    }
    
    /**
     * RestTemplate with Load Balancing
     */
    @Bean
    @LoadBalanced  // Enable load balancing
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    /**
     * WebClient with Load Balancing
     */
    @Bean
    @LoadBalanced  // Enable load balancing
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
```

## Client-Side Load Balancing

### Architecture

```
┌─────────────────────────────────────────────────┐
│            Client Service                        │
│                                                  │
│  ┌─────────────────────────────────────────┐   │
│  │   @LoadBalanced RestTemplate/WebClient  │   │
│  └──────────────────┬──────────────────────┘   │
│                     │                            │
│  ┌──────────────────▼──────────────────────┐   │
│  │    Spring Cloud LoadBalancer            │   │
│  │    • Get instances from registry        │   │
│  │    • Apply load balancing strategy      │   │
│  │    • Select instance                    │   │
│  └──────────────────┬──────────────────────┘   │
└────────────────────┼─────────────────────────┘
                      │
       ┌──────────────┼──────────────┐
       │              │              │
       ▼              ▼              ▼
  Instance 1     Instance 2     Instance 3
```

### Service Discovery Integration

**ServiceDiscoveryClient.java**
```java
package com.example.loadbalancer.client;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceDiscoveryClient {
    
    private final DiscoveryClient discoveryClient;
    
    public ServiceDiscoveryClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }
    
    /**
     * Get all instances of a service
     */
    public List<ServiceInstance> getInstances(String serviceId) {
        return discoveryClient.getInstances(serviceId);
    }
    
    /**
     * Get all registered services
     */
    public List<String> getServices() {
        return discoveryClient.getServices();
    }
    
    /**
     * Display instance information
     */
    public void displayInstanceInfo(String serviceId) {
        List<ServiceInstance> instances = getInstances(serviceId);
        
        System.out.println("Service: " + serviceId);
        System.out.println("Total Instances: " + instances.size());
        
        for (ServiceInstance instance : instances) {
            System.out.println("  - " + instance.getUri());
            System.out.println("    Host: " + instance.getHost());
            System.out.println("    Port: " + instance.getPort());
            System.out.println("    Metadata: " + instance.getMetadata());
        }
    }
}
```

## RestTemplate with Load Balancing

### 1. Basic Usage

**UserService.java**
```java
package com.example.loadbalancer.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserService {
    
    private final RestTemplate restTemplate;
    
    public UserService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Call user-service with load balancing
     * URL format: http://service-name/path
     */
    public String getUserById(Long userId) {
        String url = "http://user-service/api/users/" + userId;
        return restTemplate.getForObject(url, String.class);
    }
    
    /**
     * Get all users
     */
    public String[] getAllUsers() {
        String url = "http://user-service/api/users";
        return restTemplate.getForObject(url, String[].class);
    }
    
    /**
     * Create user with POST
     */
    public String createUser(UserDTO userDTO) {
        String url = "http://user-service/api/users";
        return restTemplate.postForObject(url, userDTO, String.class);
    }
    
    /**
     * Update user with PUT
     */
    public void updateUser(Long userId, UserDTO userDTO) {
        String url = "http://user-service/api/users/" + userId;
        restTemplate.put(url, userDTO);
    }
    
    /**
     * Delete user
     */
    public void deleteUser(Long userId) {
        String url = "http://user-service/api/users/" + userId;
        restTemplate.delete(url);
    }
}
```

### 2. With Response Entity

**ProductService.java**
```java
package com.example.loadbalancer.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
public class ProductService {
    
    private final RestTemplate restTemplate;
    
    public ProductService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * GET with ResponseEntity for full response access
     */
    public ProductDTO getProduct(Long productId) {
        String url = "http://product-service/api/products/" + productId;
        
        ResponseEntity<ProductDTO> response = 
                restTemplate.getForEntity(url, ProductDTO.class);
        
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
        
        return response.getBody();
    }
    
    /**
     * POST with headers
     */
    public ProductDTO createProduct(ProductDTO product) {
        String url = "http://product-service/api/products";
        
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Request-Source", "order-service");
        
        // Create request entity
        HttpEntity<ProductDTO> request = new HttpEntity<>(product, headers);
        
        // Make request
        ResponseEntity<ProductDTO> response = 
                restTemplate.postForEntity(url, request, ProductDTO.class);
        
        return response.getBody();
    }
    
    /**
     * Exchange method for full control
     */
    public ProductDTO updateProduct(Long productId, ProductDTO product) {
        String url = "http://product-service/api/products/" + productId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProductDTO> request = new HttpEntity<>(product, headers);
        
        ResponseEntity<ProductDTO> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                request,
                ProductDTO.class
        );
        
        return response.getBody();
    }
}
```

### 3. Error Handling

**RestTemplateWithErrorHandling.java**
```java
package com.example.loadbalancer.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderService {
    
    private final RestTemplate restTemplate;
    
    public OrderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public OrderDTO getOrder(Long orderId) {
        String url = "http://order-service/api/orders/" + orderId;
        
        try {
            ResponseEntity<OrderDTO> response = 
                    restTemplate.getForEntity(url, OrderDTO.class);
            
            return response.getBody();
            
        } catch (HttpClientErrorException e) {
            // 4xx errors
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new OrderNotFoundException("Order not found: " + orderId);
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new InvalidOrderException("Invalid order ID: " + orderId);
            }
            throw e;
            
        } catch (HttpServerErrorException e) {
            // 5xx errors
            throw new ServiceUnavailableException(
                    "Order service is temporarily unavailable");
        }
    }
}
```

## WebClient with Load Balancing

### 1. Basic Usage (Reactive)

**ReactiveUserService.java**
```java
package com.example.loadbalancer.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ReactiveUserService {
    
    private final WebClient.Builder webClientBuilder;
    
    public ReactiveUserService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }
    
    /**
     * GET single user (Mono)
     */
    public Mono<UserDTO> getUserById(Long userId) {
        return webClientBuilder.build()
                .get()
                .uri("http://user-service/api/users/{id}", userId)
                .retrieve()
                .bodyToMono(UserDTO.class);
    }
    
    /**
     * GET multiple users (Flux)
     */
    public Flux<UserDTO> getAllUsers() {
        return webClientBuilder.build()
                .get()
                .uri("http://user-service/api/users")
                .retrieve()
                .bodyToFlux(UserDTO.class);
    }
    
    /**
     * POST create user
     */
    public Mono<UserDTO> createUser(UserDTO user) {
        return webClientBuilder.build()
                .post()
                .uri("http://user-service/api/users")
                .bodyValue(user)
                .retrieve()
                .bodyToMono(UserDTO.class);
    }
    
    /**
     * PUT update user
     */
    public Mono<UserDTO> updateUser(Long userId, UserDTO user) {
        return webClientBuilder.build()
                .put()
                .uri("http://user-service/api/users/{id}", userId)
                .bodyValue(user)
                .retrieve()
                .bodyToMono(UserDTO.class);
    }
    
    /**
     * DELETE user
     */
    public Mono<Void> deleteUser(Long userId) {
        return webClientBuilder.build()
                .delete()
                .uri("http://user-service/api/users/{id}", userId)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
```

### 2. With Headers and Error Handling

**ReactiveProductService.java**
```java
package com.example.loadbalancer.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class ReactiveProductService {
    
    private final WebClient webClient;
    
    public ReactiveProductService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://product-service")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, 
                              MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    
    /**
     * GET with custom headers and error handling
     */
    public Mono<ProductDTO> getProduct(Long productId) {
        return webClient.get()
                .uri("/api/products/{id}", productId)
                .header("X-Request-Source", "order-service")
                .retrieve()
                .onStatus(
                        HttpStatus.NOT_FOUND::equals,
                        response -> Mono.error(
                                new ProductNotFoundException("Product not found"))
                )
                .onStatus(
                        HttpStatus::is5xxServerError,
                        response -> Mono.error(
                                new ServiceUnavailableException("Service down"))
                )
                .bodyToMono(ProductDTO.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .timeout(Duration.ofSeconds(5));
    }
    
    /**
     * POST with timeout and retry
     */
    public Mono<ProductDTO> createProduct(ProductDTO product) {
        return webClient.post()
                .uri("/api/products")
                .bodyValue(product)
                .retrieve()
                .bodyToMono(ProductDTO.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable)
                )
                .timeout(Duration.ofSeconds(10))
                .doOnError(error -> 
                        System.err.println("Error creating product: " + error.getMessage())
                );
    }
}
```

### 3. Parallel Requests

**AggregationService.java**
```java
package com.example.loadbalancer.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AggregationService {
    
    private final WebClient.Builder webClientBuilder;
    
    public AggregationService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }
    
    /**
     * Aggregate data from multiple services in parallel
     */
    public Mono<OrderDetailsDTO> getOrderDetails(Long orderId) {
        WebClient webClient = webClientBuilder.build();
        
        // Fetch order
        Mono<OrderDTO> orderMono = webClient.get()
                .uri("http://order-service/api/orders/{id}", orderId)
                .retrieve()
                .bodyToMono(OrderDTO.class);
        
        // Fetch user (parallel)
        Mono<UserDTO> userMono = orderMono.flatMap(order ->
                webClient.get()
                        .uri("http://user-service/api/users/{id}", order.getUserId())
                        .retrieve()
                        .bodyToMono(UserDTO.class)
        );
        
        // Fetch product (parallel)
        Mono<ProductDTO> productMono = orderMono.flatMap(order ->
                webClient.get()
                        .uri("http://product-service/api/products/{id}", 
                             order.getProductId())
                        .retrieve()
                        .bodyToMono(ProductDTO.class)
        );
        
        // Combine results
        return Mono.zip(orderMono, userMono, productMono)
                .map(tuple -> new OrderDetailsDTO(
                        tuple.getT1(),  // order
                        tuple.getT2(),  // user
                        tuple.getT3()   // product
                ));
    }
}
```

## Feign Client with Load Balancing

### 1. Setup

**pom.xml**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

**Application.java**
```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients  // Enable Feign
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 2. Feign Client Interface

**UserFeignClient.java**
```java
package com.example.loadbalancer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "user-service",  // Service name in Eureka
        fallback = UserFeignClientFallback.class  // Fallback
)
public interface UserFeignClient {
    
    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
    
    @GetMapping("/api/users")
    List<UserDTO> getAllUsers();
    
    @PostMapping("/api/users")
    UserDTO createUser(@RequestBody UserDTO user);
    
    @PutMapping("/api/users/{id}")
    UserDTO updateUser(@PathVariable("id") Long id, @RequestBody UserDTO user);
    
    @DeleteMapping("/api/users/{id}")
    void deleteUser(@PathVariable("id") Long id);
}
```

### 3. Fallback Implementation

**UserFeignClientFallback.java**
```java
package com.example.loadbalancer.client;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserFeignClientFallback implements UserFeignClient {
    
    @Override
    public UserDTO getUserById(Long id) {
        // Return default user
        return new UserDTO(id, "Unknown User", "unknown@example.com");
    }
    
    @Override
    public List<UserDTO> getAllUsers() {
        return Collections.emptyList();
    }
    
    @Override
    public UserDTO createUser(UserDTO user) {
        throw new ServiceUnavailableException("User service unavailable");
    }
    
    @Override
    public UserDTO updateUser(Long id, UserDTO user) {
        throw new ServiceUnavailableException("User service unavailable");
    }
    
    @Override
    public void deleteUser(Long id) {
        throw new ServiceUnavailableException("User service unavailable");
    }
}
```

## Custom Load Balancer

### 1. Custom Load Balancer Configuration

**CustomLoadBalancerConfiguration.java**
```java
package com.example.loadbalancer.config;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

public class CustomLoadBalancerConfiguration {
    
    /**
     * Custom load balancer using weighted round robin
     */
    @Bean
    public ReactorLoadBalancer<ServiceInstance> weightedLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {
        
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        
        return new WeightedRoundRobinLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(name, 
                        ServiceInstanceListSupplier.class),
                name
        );
    }
}
```

### 2. Weighted Round Robin Implementation

**WeightedRoundRobinLoadBalancer.java**
```java
package com.example.loadbalancer.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WeightedRoundRobinLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    
    private static final Log log = LogFactory.getLog(WeightedRoundRobinLoadBalancer.class);
    
    private final AtomicInteger position = new AtomicInteger(0);
    private final String serviceId;
    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    
    public WeightedRoundRobinLoadBalancer(
            ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
            String serviceId) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
    }
    
    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = 
                serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        
        return supplier.get(request).next()
                .map(serviceInstances -> processInstanceResponse(serviceInstances));
    }
    
    private Response<ServiceInstance> processInstanceResponse(
            List<ServiceInstance> instances) {
        
        if (instances.isEmpty()) {
            log.warn("No servers available for service: " + serviceId);
            return new EmptyResponse();
        }
        
        // Create weighted list based on instance metadata
        int totalWeight = 0;
        for (ServiceInstance instance : instances) {
            int weight = getWeight(instance);
            totalWeight += weight;
        }
        
        // Select instance based on position and weights
        int pos = position.incrementAndGet() % totalWeight;
        
        int currentWeight = 0;
        for (ServiceInstance instance : instances) {
            currentWeight += getWeight(instance);
            if (pos < currentWeight) {
                log.info("Selected instance: " + instance.getUri());
                return new DefaultResponse(instance);
            }
        }
        
        // Fallback to first instance
        return new DefaultResponse(instances.get(0));
    }
    
    private int getWeight(ServiceInstance instance) {
        String weightStr = instance.getMetadata().get("weight");
        try {
            return weightStr != null ? Integer.parseInt(weightStr) : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
```

### 3. Using Custom Load Balancer

**Application.java**
```java
@SpringBootApplication
@EnableDiscoveryClient
@LoadBalancerClient(name = "user-service", 
                    configuration = CustomLoadBalancerConfiguration.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**application.yml (Service instances with weights)**
```yaml
eureka:
  instance:
    metadata-map:
      weight: 3  # This instance gets 3x more traffic
```

## Health-Based Load Balancing

### 1. Health Check Load Balancer

**HealthCheckServiceInstanceListSupplier.java**
```java
package com.example.loadbalancer.config;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

public class HealthCheckServiceInstanceListSupplier 
        implements ServiceInstanceListSupplier {
    
    private final ServiceInstanceListSupplier delegate;
    private final HealthCheckService healthCheckService;
    
    public HealthCheckServiceInstanceListSupplier(
            ServiceInstanceListSupplier delegate,
            HealthCheckService healthCheckService) {
        this.delegate = delegate;
        this.healthCheckService = healthCheckService;
    }
    
    @Override
    public String getServiceId() {
        return delegate.getServiceId();
    }
    
    @Override
    public Flux<List<ServiceInstance>> get() {
        return delegate.get().map(this::filteredByHealth);
    }
    
    private List<ServiceInstance> filteredByHealth(List<ServiceInstance> instances) {
        return instances.stream()
                .filter(healthCheckService::isHealthy)
                .collect(Collectors.toList());
    }
}
```

### 2. Health Check Service

**HealthCheckService.java**
```java
package com.example.loadbalancer.config;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HealthCheckService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, Boolean> healthCache = new ConcurrentHashMap<>();
    
    public boolean isHealthy(ServiceInstance instance) {
        String key = instance.getUri().toString();
        
        // Check cache first
        if (healthCache.containsKey(key)) {
            return healthCache.get(key);
        }
        
        // Perform health check
        boolean healthy = checkHealth(instance);
        healthCache.put(key, healthy);
        
        return healthy;
    }
    
    private boolean checkHealth(ServiceInstance instance) {
        try {
            String healthUrl = instance.getUri() + "/actuator/health";
            Map<String, Object> health = 
                    restTemplate.getForObject(healthUrl, Map.class);
            
            return "UP".equals(health.get("status"));
        } catch (Exception e) {
            return false;
        }
    }
    
    public void invalidateCache(String uri) {
        healthCache.remove(uri);
    }
}
```

## Complete Example

### Project Structure
```
order-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/order/
│   │   │       ├── OrderServiceApplication.java
│   │   │       ├── config/
│   │   │       │   ├── LoadBalancerConfig.java
│   │   │       │   └── CustomLoadBalancerConfiguration.java
│   │   │       ├── controller/
│   │   │       │   └── OrderController.java
│   │   │       ├── service/
│   │   │       │   ├── OrderService.java
│   │   │       │   ├── UserService.java
│   │   │       │   └── ProductService.java
│   │   │       ├── client/
│   │   │       │   └── UserFeignClient.java
│   │   │       └── dto/
│   │   │           ├── OrderDTO.java
│   │   │           ├── UserDTO.java
│   │   │           └── ProductDTO.java
│   │   └── resources/
│   │       └── application.yml
└── pom.xml
```

### Complete Application Configuration

**application.yml**
```yaml
server:
  port: 8083

spring:
  application:
    name: order-service
    
  cloud:
    loadbalancer:
      cache:
        enabled: true
        ttl: 35s
      health-check:
        interval: 25s
      retry:
        enabled: true
        max-retries-on-same-service-instance: 1
        max-retries-on-next-service-instance: 2

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    metadata-map:
      zone: zone1
      weight: 1

feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
        loggerLevel: basic
  circuitbreaker:
    enabled: true

# Logging
logging:
  level:
    com.example.order: DEBUG
    org.springframework.cloud.loadbalancer: DEBUG
```

### Testing the Load Balancer

**LoadBalancerTest.java**
```java
package com.example.order;

import com.example.order.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LoadBalancerTest {
    
    @Autowired
    private UserService userService;
    
    @Test
    public void testLoadBalancing() {
        // Make multiple requests
        for (int i = 0; i < 10; i++) {
            String user = userService.getUserById(1L);
            System.out.println("Request " + i + ": " + user);
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
```

## Best Practices

### 1. Configuration Best Practices

**✅ DO:**
- Enable caching to reduce discovery calls
- Configure appropriate timeout values
- Use health checks to filter unhealthy instances
- Set up retry logic for transient failures
- Enable zone-aware load balancing in multi-zone deployments

**❌ DON'T:**
- Don't set cache TTL too high (stale instances)
- Don't set timeout too low (premature failures)
- Don't ignore health check failures
- Don't use load balancing for batch operations

### 2. Service Instance Best Practices

**✅ DO:**
- Register instances with meaningful metadata
- Implement health check endpoints
- Use consistent naming conventions
- Configure appropriate lease renewal intervals
- Deregister instances gracefully on shutdown

**❌ DON'T:**
- Don't register unhealthy instances
- Don't use hardcoded instance metadata
- Don't forget to handle instance failures

### 3. Strategy Selection

**Use Round Robin when:**
- All instances have similar capacity
- Requests have similar complexity
- No session affinity required

**Use Weighted Round Robin when:**
- Instances have different capacities
- Some instances are more powerful
- Gradual traffic shift needed (canary deployments)

**Use Least Connections when:**
- Long-lived connections
- Variable request duration
- WebSocket connections

**Use Random when:**
- Simple distribution needed
- No specific requirements

### 4. Testing Best Practices

**✅ DO:**
- Test with multiple service instances
- Simulate instance failures
- Test failover scenarios
- Monitor load distribution
- Test with different load balancing strategies

**❌ DON'T:**
- Don't test with single instance only
- Don't ignore edge cases
- Don't skip load testing

## Exercises

### Exercise 1: Basic Load Balancing
**Task:** Set up a service with Spring Cloud LoadBalancer that calls another service using RestTemplate.

**Requirements:**
1. Create two services: `client-service` and `provider-service`
2. Register both with Eureka
3. Configure load balancer in client-service
4. Create @LoadBalanced RestTemplate bean
5. Make calls to provider-service from client-service
6. Start 3 instances of provider-service on different ports
7. Verify round-robin distribution

### Exercise 2: WebClient Load Balancing
**Task:** Convert Exercise 1 to use WebClient for reactive load balancing.

**Requirements:**
1. Replace RestTemplate with WebClient
2. Use Mono/Flux for reactive responses
3. Implement error handling with retry
4. Add timeout configuration
5. Test with parallel requests

### Exercise 3: Custom Load Balancer
**Task:** Implement a custom weighted round-robin load balancer.

**Requirements:**
1. Create WeightedRoundRobinLoadBalancer class
2. Read weight from instance metadata
3. Distribute traffic based on weights (e.g., 70/30 split)
4. Test with weighted instances
5. Log selected instances

### Exercise 4: Health-Aware Load Balancing
**Task:** Implement health-check based load balancing that excludes unhealthy instances.

**Requirements:**
1. Create HealthCheckService
2. Implement health check endpoint in provider service
3. Filter instances based on health status
4. Cache health check results (TTL 30s)
5. Test by stopping one instance

### Exercise 5: Feign Client with Fallback
**Task:** Create a Feign client with load balancing and fallback.

**Requirements:**
1. Define Feign client interface
2. Enable Feign in application
3. Implement fallback class
4. Configure timeout and retry
5. Test fallback behavior when service is down

## Key Takeaways

1. **Load Balancing is Essential** for microservices
   - Enables horizontal scaling
   - Provides high availability
   - Improves performance

2. **Client-Side Load Balancing** is preferred in microservices
   - No single point of failure
   - Lower latency
   - Integrates with service discovery

3. **Spring Cloud LoadBalancer** replaced Netflix Ribbon
   - Reactive architecture
   - Better performance
   - Active development

4. **Multiple Strategies** available
   - Round Robin (default)
   - Weighted Round Robin
   - Random
   - Zone-Aware
   - Custom strategies

5. **RestTemplate and WebClient** both support load balancing
   - RestTemplate for synchronous calls
   - WebClient for reactive/async calls
   - Just add @LoadBalanced annotation

6. **Health Checks** improve reliability
   - Exclude unhealthy instances
   - Automatic failover
   - Faster recovery

7. **Configuration is Important**
   - Cache to reduce discovery calls
   - Appropriate timeouts
   - Retry logic
   - Zone awareness

## Resources

### Official Documentation
- [Spring Cloud LoadBalancer](https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#spring-cloud-loadbalancer)
- [Spring Cloud Netflix](https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/)
- [Spring WebClient](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-client)

### Tutorials
- [Spring Cloud LoadBalancer Tutorial](https://spring.io/guides/gs/spring-cloud-loadbalancer/)
- [Client-Side Load Balancing](https://www.baeldung.com/spring-cloud-load-balancer)

### Books
- *Spring Microservices in Action* by John Carnell
- *Cloud Native Java* by Josh Long and Kenny Bastani

### Videos
- [Spring Cloud LoadBalancer Deep Dive](https://www.youtube.com/watch?v=OZfCUx3cGPU)
- [Microservices Load Balancing Patterns](https://www.youtube.com/watch?v=RqfaTIWc3LQ)

---

**Next Steps:**
- Complete the exercises
- Implement custom load balancing strategies
- Test with different scenarios
- Monitor load distribution
- Prepare for Day 16: Circuit Breaker

---
[<< Previous: Day 14](../Day14_API_Gateway/README.md) | [Back to Main](../README.md) | [Next: Day 16 >>](../Day16_Circuit_Breaker/README.md)
