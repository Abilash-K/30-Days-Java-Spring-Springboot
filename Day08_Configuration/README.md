# Day 08: Spring Boot Configuration Management

## 📋 Table of Contents
- [Introduction](#introduction)
- [Configuration Files](#configuration-files)
- [Spring Profiles](#spring-profiles)
- [@ConfigurationProperties](#configurationproperties)
- [Environment Variables](#environment-variables)
- [Externalized Configuration](#externalized-configuration)
- [Configuration Priority](#configuration-priority)
- [Property Placeholders](#property-placeholders)
- [Type-Safe Configuration](#type-safe-configuration)
- [Profile-Specific Properties](#profile-specific-properties)
- [Spring Cloud Config](#spring-cloud-config)
- [Best Practices](#best-practices)
- [Complete Example](#complete-example)
- [Exercises](#exercises)
- [Key Takeaways](#key-takeaways)
- [Resources](#resources)

## Introduction

Welcome to Day 8! Today we'll master **Configuration Management** in Spring Boot - a crucial skill for building applications that work across different environments (development, testing, production).

### What You'll Learn
- Different ways to configure Spring Boot applications
- Using profiles for environment-specific configurations
- Type-safe configuration properties
- Externalized configuration strategies
- Cloud-native configuration with Spring Cloud Config

### Why Configuration Management Matters
- ✅ Separate configuration from code
- ✅ Same code works in different environments
- ✅ Easy to change without recompilation
- ✅ Secure sensitive information
- ✅ Enable feature toggles

## Configuration Files

### application.properties vs application.yml

**application.properties:**
```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=root
spring.datasource.password=secret
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Logging
logging.level.root=INFO
logging.level.com.example.myapp=DEBUG
logging.file.name=logs/application.log
```

**application.yml (Recommended):**
```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: secret
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQL8Dialect

logging:
  level:
    root: INFO
    com.example.myapp: DEBUG
  file:
    name: logs/application.log
```

**YAML Advantages:**
- More readable and hierarchical
- Supports lists and complex structures
- Less repetitive
- Better for configuration files

### Common Configuration Properties

```yaml
# Application name
spring:
  application:
    name: my-application

# Server Configuration
server:
  port: 8080
  address: localhost
  compression:
    enabled: true
  http2:
    enabled: true
  error:
    include-message: always
    include-binding-errors: always

# Data Source
spring:
  datasource:
    url: ${DB_URL:jdbc:h2:mem:testdb}
    username: ${DB_USERNAME:sa}
    password: ${DB_PASSWORD:}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000

# Jackson (JSON)
spring:
  jackson:
    serialization:
      write-dates-as-timestamps: false
    default-property-inclusion: non_null
    date-format: yyyy-MM-dd HH:mm:ss

# Multipart file upload
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 10MB

# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

## Spring Profiles

### What are Profiles?

Profiles provide a way to segregate parts of your application configuration and make it available only in certain environments.

### Profile-Specific Configuration Files

```
src/main/resources/
├── application.yml           # Common properties
├── application-dev.yml       # Development
├── application-test.yml      # Testing
├── application-prod.yml      # Production
└── application-local.yml     # Local development
```

**application.yml (default):**
```yaml
spring:
  application:
    name: my-app

app:
  name: My Application
  version: 1.0.0
```

**application-dev.yml:**
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:devdb
    username: sa
    password:
  h2:
    console:
      enabled: true

logging:
  level:
    root: DEBUG
    com.example: TRACE

app:
  feature:
    email-enabled: false
    debug-mode: true
```

**application-test.yml:**
```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password:

logging:
  level:
    root: INFO
    com.example: DEBUG

app:
  feature:
    email-enabled: false
    debug-mode: true
```

**application-prod.yml:**
```yaml
server:
  port: 80

spring:
  datasource:
    url: jdbc:mysql://prod-db:3306/proddb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20

logging:
  level:
    root: WARN
    com.example: INFO
  file:
    name: /var/log/myapp.log

app:
  feature:
    email-enabled: true
    debug-mode: false
```

### Activating Profiles

**1. application.yml:**
```yaml
spring:
  profiles:
    active: dev
```

**2. Command Line:**
```bash
java -jar myapp.jar --spring.profiles.active=prod
```

**3. Environment Variable:**
```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar myapp.jar
```

**4. IDE (IntelliJ IDEA):**
```
Run/Debug Configurations → VM Options:
-Dspring.profiles.active=dev
```

**5. Maven:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**6. Gradle:**
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Multiple Profiles

```bash
# Activate multiple profiles
java -jar myapp.jar --spring.profiles.active=dev,debug,local
```

```yaml
spring:
  profiles:
    active: dev, debug
```

### Profile-Specific Beans

```java
@Configuration
public class DataSourceConfig {
    
    @Bean
    @Profile("dev")
    public DataSource devDataSource() {
        return new HikariDataSource(devConfig());
    }
    
    @Bean
    @Profile("prod")
    public DataSource prodDataSource() {
        return new HikariDataSource(prodConfig());
    }
    
    @Bean
    @Profile({"dev", "test"})
    public DataSource testDataSource() {
        return new HikariDataSource(testConfig());
    }
    
    @Bean
    @Profile("!prod") // Not prod
    public DataSource nonProdDataSource() {
        return new HikariDataSource(devConfig());
    }
}
```

```java
@Service
@Profile("dev")
public class MockEmailService implements EmailService {
    @Override
    public void send(String to, String message) {
        System.out.println("Mock email sent to: " + to);
    }
}

@Service
@Profile("prod")
public class RealEmailService implements EmailService {
    @Override
    public void send(String to, String message) {
        // Real email sending logic
    }
}
```

### Programmatic Profile Check

```java
@Component
public class ProfileChecker {
    
    @Autowired
    private Environment environment;
    
    public void checkProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        String[] defaultProfiles = environment.getDefaultProfiles();
        
        System.out.println("Active profiles: " + Arrays.toString(activeProfiles));
        
        if (environment.acceptsProfiles(Profiles.of("dev"))) {
            System.out.println("Development mode");
        }
        
        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            System.out.println("Production mode");
        }
    }
}
```

## @ConfigurationProperties

### Type-Safe Configuration

Instead of using `@Value` annotations everywhere, use `@ConfigurationProperties` for type-safe, organized configuration.

**application.yml:**
```yaml
app:
  name: My Application
  version: 1.0.0
  server:
    ip: 192.168.1.1
    port: 9000
  security:
    enabled: true
    token-expiration: 86400
    allowed-origins:
      - http://localhost:3000
      - http://localhost:4200
  features:
    email: true
    sms: false
    push-notification: true
  limits:
    max-upload-size: 10485760
    max-connections: 100
  admin:
    username: admin
    email: admin@example.com
```

**Configuration Properties Class:**
```java
@Configuration
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {
    
    @NotBlank
    private String name;
    
    @NotBlank
    private String version;
    
    private Server server = new Server();
    private Security security = new Security();
    private Features features = new Features();
    private Limits limits = new Limits();
    private Admin admin = new Admin();
    
    // Getters and Setters
    
    @Data
    public static class Server {
        @NotBlank
        private String ip;
        
        @Min(1)
        @Max(65535)
        private int port;
    }
    
    @Data
    public static class Security {
        private boolean enabled;
        
        @Positive
        private long tokenExpiration;
        
        private List<String> allowedOrigins = new ArrayList<>();
    }
    
    @Data
    public static class Features {
        private boolean email;
        private boolean sms;
        private boolean pushNotification;
    }
    
    @Data
    public static class Limits {
        @Positive
        private long maxUploadSize;
        
        @Positive
        private int maxConnections;
    }
    
    @Data
    public static class Admin {
        @NotBlank
        private String username;
        
        @Email
        private String email;
    }
}
```

**Enable Configuration Properties:**
```java
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**Using Configuration Properties:**
```java
@Service
public class AppService {
    private final AppProperties appProperties;
    
    public AppService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }
    
    public void printConfig() {
        System.out.println("App Name: " + appProperties.getName());
        System.out.println("Version: " + appProperties.getVersion());
        System.out.println("Server IP: " + appProperties.getServer().getIp());
        System.out.println("Server Port: " + appProperties.getServer().getPort());
        System.out.println("Security Enabled: " + appProperties.getSecurity().isEnabled());
        System.out.println("Email Feature: " + appProperties.getFeatures().isEmail());
    }
}
```

### Using @Value (Simpler Cases)

```java
@Component
public class ConfigComponent {
    
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.version}")
    private String version;
    
    @Value("${app.server.port:8080}") // Default value
    private int serverPort;
    
    @Value("${app.security.enabled:false}")
    private boolean securityEnabled;
    
    @Value("#{${app.limits.max-connections}}")
    private int maxConnections;
    
    // SpEL expression
    @Value("#{${app.limits.max-upload-size} / 1024}")
    private long maxUploadSizeKB;
    
    @Value("${app.security.allowed-origins}")
    private List<String> allowedOrigins;
}
```

## Environment Variables

### Reading Environment Variables

**application.yml:**
```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:h2:mem:testdb}
    username: ${DATABASE_USERNAME:sa}
    password: ${DATABASE_PASSWORD:}

app:
  api-key: ${API_KEY}
  secret: ${APP_SECRET}
```

**System Properties:**
```bash
# Set environment variables
export DATABASE_URL=jdbc:mysql://localhost:3306/mydb
export DATABASE_USERNAME=root
export DATABASE_PASSWORD=secret
export API_KEY=my-api-key
export APP_SECRET=my-secret

java -jar myapp.jar
```

**Docker:**
```yaml
# docker-compose.yml
services:
  app:
    image: myapp:latest
    environment:
      - DATABASE_URL=jdbc:mysql://db:3306/mydb
      - DATABASE_USERNAME=root
      - DATABASE_PASSWORD=secret
      - SPRING_PROFILES_ACTIVE=prod
```

**Kubernetes:**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  DATABASE_URL: jdbc:mysql://db:3306/mydb
  DATABASE_USERNAME: root
---
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
type: Opaque
stringData:
  DATABASE_PASSWORD: secret
  API_KEY: my-api-key
```

### Programmatic Access

```java
@Component
public class EnvironmentReader {
    
    @Autowired
    private Environment environment;
    
    public void readEnvironment() {
        String dbUrl = environment.getProperty("DATABASE_URL");
        String dbUsername = environment.getProperty("DATABASE_USERNAME");
        
        // With default value
        String apiKey = environment.getProperty("API_KEY", "default-key");
        
        // Required property
        String secret = environment.getRequiredProperty("APP_SECRET");
        
        // With type conversion
        Integer port = environment.getProperty("SERVER_PORT", Integer.class, 8080);
    }
}
```

## Externalized Configuration

### Configuration Priority (Highest to Lowest)

1. Command line arguments
2. SPRING_APPLICATION_JSON properties
3. ServletConfig init parameters
4. ServletContext init parameters
5. JNDI attributes
6. Java System properties (System.getProperties())
7. OS environment variables
8. Profile-specific application properties outside jar (application-{profile}.properties)
9. Profile-specific application properties inside jar
10. Application properties outside jar (application.properties)
11. Application properties inside jar
12. @PropertySource annotations
13. Default properties

### External Configuration Files

```bash
# Load config from custom location
java -jar myapp.jar --spring.config.location=file:/etc/myapp/application.yml

# Additional locations
java -jar myapp.jar --spring.config.additional-location=file:/etc/myapp/

# Multiple locations
java -jar myapp.jar \
  --spring.config.location=\
classpath:/,\
file:/etc/myapp/,\
file:/opt/myapp/config/
```

### @PropertySource

```java
@Configuration
@PropertySource("classpath:custom.properties")
@PropertySource("file:/etc/myapp/external.properties")
public class CustomPropertyConfig {
    
    @Value("${custom.property}")
    private String customProperty;
}
```

**Multiple Property Sources:**
```java
@Configuration
@PropertySources({
    @PropertySource("classpath:app.properties"),
    @PropertySource("classpath:database.properties"),
    @PropertySource("classpath:security.properties")
})
public class MultiplePropertyConfig {
}
```

### Random Values

```yaml
app:
  secret: ${random.value}
  number: ${random.int}
  long-number: ${random.long}
  uuid: ${random.uuid}
  range: ${random.int(10,100)}
```

## Property Placeholders

### Using Placeholders

```yaml
app:
  name: My Application
  description: ${app.name} is awesome
  full-url: http://${app.server.host}:${app.server.port}${app.context-path}
  
  server:
    host: localhost
    port: 8080
  
  context-path: /api
```

### SpEL in Configuration

```yaml
app:
  # Mathematical expressions
  calculated-value: #{10 * 2}
  
  # Conditional
  feature-enabled: #{systemProperties['env'] == 'prod' ? true : false}
```

```java
@Value("#{${app.limits.max-upload-size} / 1024 / 1024}") // Convert to MB
private long maxUploadSizeMB;

@Value("#{'${app.security.allowed-origins}'.split(',')}")
private List<String> allowedOriginsList;
```

## Type-Safe Configuration

### Constructor Binding (Immutable)

```java
@ConfigurationProperties(prefix = "app")
@ConstructorBinding
public class ImmutableAppProperties {
    
    private final String name;
    private final String version;
    private final Server server;
    
    public ImmutableAppProperties(String name, String version, Server server) {
        this.name = name;
        this.version = version;
        this.server = server;
    }
    
    // Only getters, no setters
    
    public static class Server {
        private final String ip;
        private final int port;
        
        public Server(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
        
        // Getters only
    }
}
```

**Enable Constructor Binding:**
```java
@SpringBootApplication
@EnableConfigurationProperties(ImmutableAppProperties.class)
public class Application {
}
```

### Record-Based Configuration (Java 16+)

```java
@ConfigurationProperties(prefix = "app")
public record AppConfig(
    String name,
    String version,
    ServerConfig server,
    SecurityConfig security
) {
    public record ServerConfig(String ip, int port) {}
    public record SecurityConfig(boolean enabled, long tokenExpiration) {}
}
```

## Spring Cloud Config

### Config Server Setup

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

**application.yml (Config Server):**
```yaml
server:
  port: 8888

spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/myorg/config-repo
          search-paths: '{application}'
          default-label: main
        # Or use file system
        native:
          search-locations: file:/config-repo
```

### Config Client Setup

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

**bootstrap.yml (Config Client):**
```yaml
spring:
  application:
    name: my-service
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
      retry:
        max-attempts: 6
```

### Refresh Configuration

```java
@RestController
@RefreshScope
public class DynamicConfigController {
    
    @Value("${app.message}")
    private String message;
    
    @GetMapping("/message")
    public String getMessage() {
        return message;
    }
}
```

**Trigger Refresh:**
```bash
curl -X POST http://localhost:8080/actuator/refresh
```

## Best Practices

### 1. Use YAML over Properties

```yaml
# Better readability
server:
  port: 8080
  compression:
    enabled: true
```

### 2. Use @ConfigurationProperties

```java
// Good - Type-safe
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private String name;
    private int port;
}

// Avoid - Error-prone
@Value("${app.name}")
private String name;
@Value("${app.port}")
private int port;
```

### 3. Profile-Specific Configuration

```
application.yml          # Common
application-dev.yml      # Development
application-prod.yml     # Production
```

### 4. Never Commit Secrets

```yaml
# Bad - Hardcoded
spring:
  datasource:
    password: mypassword

# Good - Environment variable
spring:
  datasource:
    password: ${DB_PASSWORD}
```

### 5. Provide Default Values

```yaml
app:
  timeout: ${APP_TIMEOUT:30000}
  retry-attempts: ${RETRY_ATTEMPTS:3}
```

### 6. Validate Configuration

```java
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {
    @NotBlank
    private String name;
    
    @Min(1)
    @Max(65535)
    private int port;
}
```

### 7. Document Configuration

```yaml
# Application Configuration
app:
  # Application display name
  name: My Application
  
  # Server configuration
  server:
    # Server IP address
    ip: localhost
    # Server port (1-65535)
    port: 8080
```

## Complete Example

### Multi-Environment Application

**application.yml:**
```yaml
spring:
  application:
    name: e-commerce-app
  profiles:
    active: ${ACTIVE_PROFILE:dev}

app:
  name: E-Commerce Platform
  version: 2.0.0
```

**application-dev.yml:**
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:devdb
    username: sa
    password:
  h2:
    console:
      enabled: true
  jpa:
    show-sql: true

app:
  features:
    email: false
    payment: false
  external-services:
    payment-api: http://localhost:9000
```

**application-prod.yml:**
```yaml
server:
  port: 80

spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
  jpa:
    show-sql: false

app:
  features:
    email: true
    payment: true
  external-services:
    payment-api: ${PAYMENT_API_URL}
```

**Configuration Properties:**
```java
@Configuration
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {
    
    @NotBlank
    private String name;
    
    @NotBlank
    private String version;
    
    private Features features = new Features();
    private ExternalServices externalServices = new ExternalServices();
    
    @Data
    public static class Features {
        private boolean email;
        private boolean payment;
    }
    
    @Data
    public static class ExternalServices {
        @NotBlank
        private String paymentApi;
    }
    
    // Getters and Setters
}
```

## Exercises

### Exercise 1: Basic Configuration
Create an application with:
- Different configurations for dev, test, and prod
- Database settings for each environment
- Logging levels per environment

### Exercise 2: Type-Safe Configuration
Create a `@ConfigurationProperties` class for:
- Application metadata (name, version, description)
- Server settings (host, port, timeout)
- Security settings (enabled, token expiration)
- Feature flags (email, sms, notifications)

### Exercise 3: External Configuration
Configure your app to:
- Read database credentials from environment variables
- Support custom configuration file location
- Override properties via command line

### Exercise 4: Profile-Specific Beans
Create:
- Mock service for dev profile
- Real service for prod profile
- Test with different profiles

### Exercise 5: Spring Cloud Config
Set up:
- Config Server with Git backend
- Config Client that reads from server
- Refresh configuration without restart

## Key Takeaways

✅ Use **YAML** for better readability  
✅ Leverage **Spring Profiles** for environment-specific configs  
✅ Use **@ConfigurationProperties** for type-safe configuration  
✅ Never commit **secrets** to version control  
✅ Use **environment variables** for sensitive data  
✅ Provide **default values** for optional properties  
✅ **Validate** configuration with @Validated  
✅ Understand **configuration priority**  
✅ Use **Spring Cloud Config** for centralized configuration  
✅ Document your **configuration properties**  
✅ Use **@RefreshScope** for dynamic configuration  
✅ Externalize configuration for **12-factor apps**  

## Resources

### Official Documentation
- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Spring Boot Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [Spring Cloud Config](https://spring.io/projects/spring-cloud-config)

### Articles
- [Baeldung: Properties with Spring](https://www.baeldung.com/properties-with-spring)
- [DZone: Spring Boot Configuration](https://dzone.com/articles/spring-boot-configuration-best-practices)

### Books
- "Spring Boot in Action" by Craig Walls
- "Cloud Native Java" by Josh Long

## Next Steps

Tomorrow, we'll explore **Spring Boot Actuator and Monitoring** to gain insights into our running applications!

---
[<< Previous: Day 07](../Day07_Exception_Validation/README.md) | [Back to Main](../README.md) | [Next: Day 09 >>](../Day09_Actuator_Monitoring/README.md)
