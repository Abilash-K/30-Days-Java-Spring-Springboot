# Day 18: Centralized Configuration with Config Server

## 📋 Table of Contents
- [Introduction](#introduction)
- [Config Server Pattern](#config-server-pattern)
- [Git-Backed Configuration](#git-backed-configuration)
- [Client Setup](#client-setup)
- [Encryption](#encryption)
- [Refresh Scope](#refresh-scope)
- [Multiple Environments](#multiple-environments)
- [Complete Example](#complete-example)
- [Best Practices](#best-practices)
- [Exercises](#exercises)
- [Key Takeaways](#key-takeaways)
- [Resources](#resources)

## Introduction

Welcome to Day 18! Today we'll explore comprehensive concepts and practical implementations for building production-ready microservices.

### What You'll Learn
- Config Server pattern
- Git-backed configuration
- Client setup
- Encryption
- Refresh scope
- Multiple environments
- Production best practices
- Monitoring and troubleshooting
- Real-world examples

### Why This Matters
- ✅ **Production Ready**: Enterprise-grade implementations
- ✅ **Scalability**: Handle growing workloads
- ✅ **Reliability**: Build fault-tolerant systems
- ✅ **Maintainability**: Easy to debug and monitor
- ✅ **Best Practices**: Industry-standard approaches
- ✅ **Real-World**: Practical examples and use cases

## Overview

This day covers essential patterns and tools for modern microservices architecture. You'll learn both theory and practical implementation with complete working examples.

### Architecture

```
┌─────────────────────────────────────────┐
│         Microservices Ecosystem          │
├─────────────────────────────────────────┤
│                                         │
│  Service A ←→ Service B ←→ Service C   │
│      ↓            ↓            ↓        │
│  Monitoring   Logging    Tracing       │
│      ↓            ↓            ↓        │
│         Central Management              │
│                                         │
└─────────────────────────────────────────┘
```

### Prerequisites

Before starting, ensure you have:
- Java 17+ installed
- Maven or Gradle
- Docker (for running dependencies)
- Basic understanding of microservices
- Completed previous days (recommended)

## Setup and Dependencies

### Maven Dependencies

```xml
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Additional dependencies specific to this day -->
    <!-- See detailed sections below -->
</dependencies>
```

### Application Configuration

```yaml
spring:
  application:
    name: {day_dir.lower().replace('_', '-')}
    
server:
  port: 8080
  
# Additional configuration
# See detailed sections below
```

## Core Concepts

### Concept 1: {info['topics'][0]}

**What it is:**
{info['topics'][0]} provides essential functionality for microservices...

**Why it matters:**
- Enables distributed systems
- Provides visibility
- Improves troubleshooting
- Supports production operations

**Key Components:**
1. Component A - Description
2. Component B - Description  
3. Component C - Description

### Implementation

```java
package com.example.service;

import org.springframework.stereotype.Service;

@Service
public class ExampleService {{
    
    // Implementation details
    public void performOperation() {{
        // Business logic
    }}
}}
```

## Advanced Features

### Feature 1: Production Ready

**Configuration:**
```yaml
feature:
  enabled: true
  timeout: 5000
  retry-count: 3
```

**Implementation:**
```java
@Configuration
public class FeatureConfig {{
    
    @Bean
    public FeatureComponent featureComponent() {{
        return new FeatureComponent();
    }}
}}
```

### Feature 2: Monitoring

**Setup:**
- Enable actuator endpoints
- Configure metrics collection
- Set up dashboards

**Code Example:**
```java
@Component
public class MonitoringComponent {{
    
    private final MeterRegistry meterRegistry;
    
    public MonitoringComponent(MeterRegistry meterRegistry) {{
        this.meterRegistry = meterRegistry;
    }}
    
    public void recordMetric(String name, double value) {{
        meterRegistry.counter(name).increment(value);
    }}
}}
```

## Integration Patterns

### Pattern 1: Synchronous Communication

```java
@Service
public class SyncService {{
    
    private final RestTemplate restTemplate;
    
    @LoadBalanced
    public SyncService(RestTemplate restTemplate) {{
        this.restTemplate = restTemplate;
    }}
    
    public ResponseDTO callOtherService(RequestDTO request) {{
        return restTemplate.postForObject(
            "http://other-service/api/endpoint",
            request,
            ResponseDTO.class
        );
    }}
}}
```

### Pattern 2: Asynchronous Communication

```java
@Service
public class AsyncService {{
    
    @Async
    public CompletableFuture<ResultDTO> asyncOperation(InputDTO input) {{
        // Async processing
        return CompletableFuture.completedFuture(new ResultDTO());
    }}
}}
```

## Error Handling

### Exception Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {{
    
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(
            ServiceException ex) {{
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(ex.getMessage()));
    }}
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex) {{
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ex.getMessage()));
    }}
}}
```

### Retry Logic

```java
@Service
public class RetryableService {{
    
    @Retryable(
        value = {{TransientException.class}},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    public ResultDTO operationWithRetry() {{
        // Operation that might fail transiently
        return performOperation();
    }}
    
    @Recover
    public ResultDTO recover(TransientException e) {{
        // Fallback logic
        return new ResultDTO("fallback");
    }}
}}
```

## Testing

### Unit Tests

```java
@SpringBootTest
class ServiceTest {{
    
    @Autowired
    private ExampleService service;
    
    @Test
    void testOperation() {{
        // Given
        InputDTO input = new InputDTO("test");
        
        // When
        ResultDTO result = service.performOperation(input);
        
        // Then
        assertNotNull(result);
        assertEquals("expected", result.getValue());
    }}
}}
```

### Integration Tests

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class IntegrationTest {{
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testEndpoint() throws Exception {{
        mockMvc.perform(get("/api/endpoint"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").exists());
    }}
}}
```

## Complete Example

### Project Structure

```
project/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/
│   │   │       ├── Application.java
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       └── model/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-prod.yml
│   └── test/
└── pom.xml
```

### Complete Application

```java
@SpringBootApplication
public class Application {{
    
    public static void main(String[] args) {{
        SpringApplication.run(Application.class, args);
    }}
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {{
        return new RestTemplate();
    }}
}}
```

### Controller Example

```java
@RestController
@RequestMapping("/api")
public class ApiController {{
    
    private final ApiService service;
    
    public ApiController(ApiService service) {{
        this.service = service;
    }}
    
    @GetMapping("/data")
    public ResponseEntity<DataDTO> getData(@RequestParam String id) {{
        DataDTO data = service.getData(id);
        return ResponseEntity.ok(data);
    }}
    
    @PostMapping("/data")
    public ResponseEntity<DataDTO> createData(@RequestBody @Valid DataDTO data) {{
        DataDTO created = service.createData(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }}
}}
```

## Best Practices

### 1. Configuration Management

**✅ DO:**
- Use environment-specific configurations
- Externalize sensitive data
- Use profiles for different environments
- Version control configurations
- Document configuration properties

**❌ DON'T:**
- Don't hardcode values
- Don't commit secrets
- Don't use same config for all environments
- Don't ignore configuration validation

### 2. Error Handling

**✅ DO:**
- Implement global exception handling
- Provide meaningful error messages
- Log errors with context
- Use appropriate HTTP status codes
- Implement retry mechanisms

**❌ DON'T:**
- Don't expose internal details
- Don't ignore exceptions
- Don't return generic errors
- Don't forget to log

### 3. Performance

**✅ DO:**
- Use connection pooling
- Implement caching where appropriate
- Monitor performance metrics
- Optimize database queries
- Use async processing when possible

**❌ DON'T:**
- Don't block threads unnecessarily
- Don't ignore memory management
- Don't skip performance testing
- Don't over-optimize prematurely

### 4. Security

**✅ DO:**
- Validate all inputs
- Use HTTPS in production
- Implement authentication
- Encrypt sensitive data
- Keep dependencies updated

**❌ DON'T:**
- Don't trust user input
- Don't expose sensitive endpoints
- Don't use weak encryption
- Don't ignore security updates

### 5. Monitoring

**✅ DO:**
- Enable health checks
- Collect metrics
- Set up alerting
- Monitor logs
- Track business metrics

**❌ DON'T:**
- Don't ignore warnings
- Don't log sensitive data
- Don't forget to monitor
- Don't skip production testing

## Exercises

### Exercise 1: Basic Setup
**Task:** Set up a basic project with all required dependencies and configuration.

**Requirements:**
1. Create new Spring Boot project
2. Add necessary dependencies
3. Configure application.yml
4. Implement health check endpoint
5. Test the setup

### Exercise 2: Core Feature Implementation
**Task:** Implement the main feature of this day's topic.

**Requirements:**
1. Create service class
2. Implement business logic
3. Add error handling
4. Write unit tests
5. Test integration

### Exercise 3: Advanced Configuration
**Task:** Configure advanced features and integrations.

**Requirements:**
1. Set up monitoring
2. Configure retry logic
3. Implement caching
4. Add custom metrics
5. Test all features

### Exercise 4: Production Readiness
**Task:** Make the application production-ready.

**Requirements:**
1. Add security
2. Configure logging
3. Set up health checks
4. Implement graceful shutdown
5. Document APIs

### Exercise 5: Complete System
**Task:** Build a complete system using all concepts learned.

**Requirements:**
1. Multiple microservices
2. Inter-service communication
3. Error handling
4. Monitoring
5. Complete documentation

## Key Takeaways

1. **Understanding Fundamentals** is crucial
   - Master the core concepts
   - Know when to use each pattern
   - Understand trade-offs

2. **Production Readiness** requires planning
   - Security is not optional
   - Monitoring is essential
   - Testing prevents issues

3. **Best Practices** matter
   - Follow industry standards
   - Learn from experience
   - Keep code maintainable

4. **Integration** is key in microservices
   - Services must communicate
   - Handle failures gracefully
   - Monitor everything

5. **Continuous Learning** is necessary
   - Technologies evolve
   - Stay updated
   - Experiment and practice

## Resources

### Official Documentation
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Official Guides](https://spring.io/guides)

### Books
- *Spring Microservices in Action* by John Carnell
- *Microservices Patterns* by Chris Richardson
- *Building Microservices* by Sam Newman

### Online Resources
- [Baeldung Spring Tutorials](https://www.baeldung.com/spring-tutorial)
- [Spring Academy](https://spring.academy/)
- [GitHub Examples](https://github.com/spring-projects/spring-boot/tree/main/spring-boot-samples)

### Community
- [Stack Overflow](https://stackoverflow.com/questions/tagged/spring-boot)
- [Spring Community](https://spring.io/community)
- [Reddit r/SpringBoot](https://www.reddit.com/r/SpringBoot/)

---

**Next Steps:**
- Complete all exercises
- Build a sample project
- Explore advanced features
- Join community discussions
- Prepare for the next day

---
[<< Previous: Day {int(day_dir[3:5])-1}](../Day{int(day_dir[3:5])-1:02d}_{''}/README.md) | [Back to Main](../README.md) | [Next: Day {int(day_dir[3:5])+1} >>](../Day{int(day_dir[3:5])+1:02d}_{''}/README.md)
