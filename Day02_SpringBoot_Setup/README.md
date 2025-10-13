# Day 02: Setting Up Spring Boot Project

## 📋 Table of Contents
- [Introduction](#introduction)
- [What is Spring Boot?](#what-is-spring-boot)
- [Setting Up Development Environment](#setting-up-development-environment)
- [Creating Your First Spring Boot Project](#creating-your-first-spring-boot-project)
- [Project Structure](#project-structure)
- [Running the Application](#running-the-application)
- [Exercises](#exercises)
- [Resources](#resources)

## Introduction

Welcome to Day 2! Today we'll set up our Spring Boot development environment and create our first Spring Boot application. By the end of this day, you'll have a working Spring Boot project and understand its structure.

## What is Spring Boot?

### Overview
Spring Boot is an opinionated framework built on top of the Spring Framework that simplifies the development of production-ready applications.

### Key Features

**1. Auto-Configuration**
- Automatically configures Spring application based on dependencies
- Reduces boilerplate configuration code
- Convention over configuration approach

**2. Embedded Servers**
- No need for external application servers
- Tomcat, Jetty, or Undertow embedded by default
- Deploy as standalone JAR files

**3. Production-Ready Features**
- Health checks
- Metrics
- Externalized configuration
- Logging

**4. Starter Dependencies**
- Pre-packaged dependency descriptors
- Simplified dependency management
- Example: `spring-boot-starter-web` includes all web dependencies

**5. Spring Boot CLI**
- Command-line interface for rapid prototyping
- Groovy-based development support

### Spring vs Spring Boot

| Feature | Spring Framework | Spring Boot |
|---------|-----------------|-------------|
| Configuration | XML/Java-based, manual | Auto-configuration |
| Deployment | Requires application server | Embedded server |
| Setup Time | Complex, time-consuming | Quick and simple |
| Dependency Management | Manual | Starter POMs |
| Production Features | Need custom setup | Built-in actuator |

## Setting Up Development Environment

### Required Software

#### 1. Java Development Kit (JDK)
```bash
# Download and install JDK 11 or 17
# Check installation
java -version
javac -version
```

**Download from:**
- Oracle JDK: https://www.oracle.com/java/technologies/downloads/
- OpenJDK: https://adoptium.net/

#### 2. Build Tool (Maven or Gradle)

**Maven:**
```bash
# Check Maven installation
mvn -version
```
Download from: https://maven.apache.org/download.cgi

**Gradle:**
```bash
# Check Gradle installation
gradle -version
```
Download from: https://gradle.org/install/

#### 3. IDE (Integrated Development Environment)

**Option 1: IntelliJ IDEA (Recommended)**
- Ultimate Edition: Full Spring Boot support
- Community Edition: Basic Java support
- Download: https://www.jetbrains.com/idea/

**Option 2: Eclipse with Spring Tools**
- Download Spring Tool Suite: https://spring.io/tools
- Or install Spring Tools plugin in Eclipse

**Option 3: Visual Studio Code**
- Install Java Extension Pack
- Install Spring Boot Extension Pack
- Download: https://code.visualstudio.com/

#### 4. Postman (API Testing)
- Download: https://www.postman.com/downloads/

### Environment Variables

**Windows:**
```batch
JAVA_HOME=C:\Program Files\Java\jdk-11
PATH=%JAVA_HOME%\bin;%PATH%
```

**Linux/Mac:**
```bash
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
export PATH=$JAVA_HOME/bin:$PATH
```

## Creating Your First Spring Boot Project

### Method 1: Spring Initializr (Web Interface)

1. **Visit:** https://start.spring.io/

2. **Configure Project:**
   - **Project:** Maven or Gradle
   - **Language:** Java
   - **Spring Boot:** 2.7.x or 3.x
   - **Packaging:** Jar
   - **Java:** 11 or 17

3. **Project Metadata:**
   - **Group:** com.example
   - **Artifact:** demo
   - **Name:** demo
   - **Package name:** com.example.demo
   - **Description:** Demo project for Spring Boot

4. **Dependencies:**
   - Spring Web
   - Spring Boot DevTools (optional)

5. **Click "Generate"** to download ZIP file

6. **Extract and open in IDE**

### Method 2: Spring Initializr (IntelliJ IDEA)

1. **File → New → Project**
2. **Select "Spring Initializr"**
3. **Configure project settings**
4. **Select dependencies**
5. **Click "Finish"**

### Method 3: Spring Boot CLI

```bash
# Install Spring Boot CLI
sdk install springboot

# Create project
spring init --dependencies=web --build=maven --java-version=11 demo

# Navigate to project
cd demo
```

### Method 4: Maven Command Line

```bash
mvn archetype:generate \
  -DgroupId=com.example \
  -DartifactId=demo \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false
```

## Project Structure

### Standard Spring Boot Project Layout

```
demo/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── demo/
│   │   │               ├── DemoApplication.java
│   │   │               ├── controller/
│   │   │               ├── service/
│   │   │               ├── repository/
│   │   │               └── model/
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application.yml
│   │       ├── static/
│   │       └── templates/
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── demo/
│                       └── DemoApplicationTests.java
│
├── target/                    # Build output (Maven)
├── pom.xml                    # Maven configuration
└── README.md
```

### Key Files Explained

#### 1. pom.xml (Maven)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <!-- Parent POM -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.14</version>
        <relativePath/>
    </parent>
    
    <!-- Project Coordinates -->
    <groupId>com.example</groupId>
    <artifactId>demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>demo</name>
    <description>Demo project for Spring Boot</description>
    
    <!-- Java Version -->
    <properties>
        <java.version>11</java.version>
    </properties>
    
    <!-- Dependencies -->
    <dependencies>
        <!-- Spring Boot Web Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Boot Test Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <!-- Build Configuration -->
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

#### 2. Main Application Class
```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

**@SpringBootApplication** is a convenience annotation that combines:
- `@Configuration`: Tags class as source of bean definitions
- `@EnableAutoConfiguration`: Enable Spring Boot's auto-configuration
- `@ComponentScan`: Scan for components in current package and sub-packages

#### 3. application.properties
```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api

# Application Name
spring.application.name=demo

# Logging
logging.level.root=INFO
logging.level.com.example.demo=DEBUG
```

#### 4. application.yml (Alternative to properties)
```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: demo

logging:
  level:
    root: INFO
    com.example.demo: DEBUG
```

## Running the Application

### Method 1: IDE
1. Right-click on `DemoApplication.java`
2. Select "Run 'DemoApplication'"

### Method 2: Maven
```bash
# Clean and compile
mvn clean install

# Run application
mvn spring-boot:run
```

### Method 3: Gradle
```bash
# Build
./gradlew build

# Run
./gradlew bootRun
```

### Method 4: Executable JAR
```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Verifying the Application

**Check Console Output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.7.14)

2024-01-15 10:30:45.123  INFO 12345 --- [main] c.e.demo.DemoApplication    : Starting DemoApplication
2024-01-15 10:30:48.456  INFO 12345 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http)
2024-01-15 10:30:48.789  INFO 12345 --- [main] c.e.demo.DemoApplication    : Started DemoApplication in 4.567 seconds
```

**Test in Browser:**
```
http://localhost:8080
```

## Creating a Simple REST Endpoint

### Controller Example
```java
package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Spring Boot!";
    }
    
    @GetMapping("/")
    public String home() {
        return "Welcome to Spring Boot Application!";
    }
}
```

**Test the endpoint:**
```bash
curl http://localhost:8080/hello
# Output: Hello, Spring Boot!
```

## Common Configuration Properties

### Server Configuration
```properties
server.port=8081                          # Change default port
server.address=127.0.0.1                  # Bind to specific IP
server.servlet.context-path=/myapp        # Add context path
server.compression.enabled=true           # Enable compression
```

### Logging Configuration
```properties
logging.file.name=app.log                 # Log file name
logging.level.root=WARN                   # Root log level
logging.level.org.springframework=INFO    # Spring log level
logging.pattern.console=%d{HH:mm:ss} - %msg%n  # Console pattern
```

### DevTools (Development Only)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

**Features:**
- Automatic restart on code changes
- LiveReload browser integration
- Property defaults for development

## Exercises

### Exercise 1: Create Your First Project
Create a new Spring Boot project with:
- Spring Web dependency
- Java 11
- Maven as build tool
- Group: com.mycompany
- Artifact: myapp

### Exercise 2: Add REST Endpoints
Create a RestController with endpoints:
- GET `/api/status` - Returns "Server is running"
- GET `/api/version` - Returns "v1.0.0"
- GET `/api/time` - Returns current server time

### Exercise 3: Configuration
- Change server port to 9090
- Add a custom context path `/myapi`
- Configure logging level for your package to DEBUG

### Exercise 4: Profile Configuration
Create two profiles:
- `application-dev.properties` - port 8080
- `application-prod.properties` - port 80
Run application with different profiles

### Exercise 5: Build and Run
- Build your application as JAR
- Run it from command line
- Access it from browser
- Test all endpoints

## Key Takeaways

✅ Spring Boot simplifies Spring application development  
✅ Spring Initializr is the fastest way to create projects  
✅ `@SpringBootApplication` combines multiple annotations  
✅ Embedded servers eliminate deployment complexity  
✅ Auto-configuration reduces manual configuration  
✅ Properties files externalize configuration  

## Resources

### Official Documentation
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Initializr](https://start.spring.io/)
- [Spring Guides](https://spring.io/guides)

### Tools
- [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.devtools)
- [Maven](https://maven.apache.org/)
- [Gradle](https://gradle.org/)

### Tutorials
- [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
- [Building an Application with Spring Boot](https://spring.io/guides/gs/spring-boot/)

## Next Steps

Tomorrow, we'll explore **Spring Boot Basics and Annotations** where we'll dive deeper into Spring Boot's core features and commonly used annotations!

---

[<< Previous: Day 01](../Day01_Java8_11_Features/README.md) | [Back to Main](../README.md) | [Next: Day 03 >>](../Day03_SpringBoot_Basics/README.md)
