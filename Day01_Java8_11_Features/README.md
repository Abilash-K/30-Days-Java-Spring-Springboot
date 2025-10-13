# Day 01: Introduction to Java 8 & 11 Features

## 📋 Table of Contents
- [Introduction](#introduction)
- [Java 8 Features](#java-8-features)
- [Java 11 Features](#java-11-features)
- [Code Examples](#code-examples)
- [Exercises](#exercises)
- [Resources](#resources)

## Introduction

Welcome to Day 1 of the 30 Days Spring Boot & Microservices challenge! Today, we'll explore the powerful features introduced in Java 8 and Java 11, which form the foundation for modern Spring Boot development.

### Why Java 8 & 11?

- **Java 8**: Revolutionary release with Lambda expressions and Stream API
- **Java 11**: LTS (Long Term Support) version with performance improvements and new APIs
- Essential for modern Spring Boot and microservices development

## Java 8 Features

### 1. Lambda Expressions

Lambda expressions enable functional programming in Java, making code more concise and readable.

**Syntax:**
```java
(parameters) -> expression
(parameters) -> { statements; }
```

**Example:**
```java
// Before Java 8
List<String> names = Arrays.asList("John", "Jane", "Jack");
Collections.sort(names, new Comparator<String>() {
    @Override
    public int compare(String s1, String s2) {
        return s1.compareTo(s2);
    }
});

// With Java 8 Lambda
Collections.sort(names, (s1, s2) -> s1.compareTo(s2));
// Even simpler
Collections.sort(names, String::compareTo);
```

### 2. Stream API

Streams provide a declarative way to process collections of data.

**Key Operations:**
- **Intermediate**: filter, map, sorted, distinct
- **Terminal**: forEach, collect, reduce, count

**Example:**
```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// Filter even numbers and calculate sum
int sum = numbers.stream()
    .filter(n -> n % 2 == 0)
    .mapToInt(Integer::intValue)
    .sum();
// Result: 30
```

### 3. Optional Class

Optional is a container that may or may not contain a non-null value.

**Benefits:**
- Prevents NullPointerException
- Makes null handling explicit
- Better API design

**Example:**
```java
// Bad practice
public String getUserName(Long userId) {
    User user = userRepository.findById(userId);
    if (user != null) {
        return user.getName();
    }
    return "Unknown";
}

// Good practice with Optional
public String getUserName(Long userId) {
    return userRepository.findById(userId)
        .map(User::getName)
        .orElse("Unknown");
}
```

### 4. Functional Interfaces

An interface with exactly one abstract method.

**Common Functional Interfaces:**
- `Predicate<T>`: Tests a condition, returns boolean
- `Function<T, R>`: Transforms T to R
- `Consumer<T>`: Accepts T, returns void
- `Supplier<T>`: Provides T

**Example:**
```java
// Predicate
Predicate<Integer> isEven = num -> num % 2 == 0;
System.out.println(isEven.test(4)); // true

// Function
Function<String, Integer> stringLength = String::length;
System.out.println(stringLength.apply("Hello")); // 5

// Consumer
Consumer<String> printer = System.out::println;
printer.accept("Hello World");

// Supplier
Supplier<Double> randomSupplier = Math::random;
System.out.println(randomSupplier.get());
```

### 5. Method References

Shorthand notation of lambda expressions to call methods.

**Types:**
1. Reference to a static method: `ClassName::methodName`
2. Reference to an instance method: `instance::methodName`
3. Reference to an instance method of arbitrary object: `ClassName::methodName`
4. Reference to a constructor: `ClassName::new`

**Example:**
```java
List<String> names = Arrays.asList("John", "Jane", "Jack");

// Lambda expression
names.forEach(name -> System.out.println(name));

// Method reference
names.forEach(System.out::println);
```

### 6. Default Methods in Interfaces

Interfaces can now have method implementations.

**Example:**
```java
public interface Vehicle {
    void start();
    
    default void stop() {
        System.out.println("Vehicle stopped");
    }
    
    static int getWheelCount() {
        return 4;
    }
}
```

### 7. Date and Time API (java.time)

New comprehensive date and time API replacing Date and Calendar.

**Key Classes:**
- `LocalDate`: Date without time
- `LocalTime`: Time without date
- `LocalDateTime`: Date with time
- `ZonedDateTime`: Date and time with timezone
- `Duration`: Time-based amount
- `Period`: Date-based amount

**Example:**
```java
// Current date and time
LocalDate today = LocalDate.now();
LocalTime now = LocalTime.now();
LocalDateTime dateTime = LocalDateTime.now();

// Create specific date
LocalDate birthday = LocalDate.of(1990, Month.JANUARY, 15);

// Date operations
LocalDate nextWeek = today.plusWeeks(1);
LocalDate lastMonth = today.minusMonths(1);

// Formatting
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
String formattedDate = today.format(formatter);
```

### 8. CompletableFuture

Asynchronous programming support.

**Example:**
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    // Simulate long-running task
    return "Hello from Future";
});

future.thenApply(String::toUpperCase)
      .thenAccept(System.out::println);
```

## Java 11 Features

### 1. Local-Variable Type Inference (var)

Use `var` to declare local variables without explicit type.

**Example:**
```java
// Before Java 11
String message = "Hello World";
List<String> names = new ArrayList<>();

// With Java 11
var message = "Hello World";
var names = new ArrayList<String>();

// In lambda parameters (Java 11)
BiFunction<Integer, Integer, Integer> add = (var a, var b) -> a + b;
```

### 2. New String Methods

**Example:**
```java
String str = "  Hello World  ";

// isBlank() - checks if string is empty or contains only whitespace
System.out.println("   ".isBlank()); // true

// lines() - returns stream of lines
String multiline = "Line1\nLine2\nLine3";
multiline.lines().forEach(System.out::println);

// strip(), stripLeading(), stripTrailing()
System.out.println(str.strip()); // "Hello World"
System.out.println(str.stripLeading()); // "Hello World  "
System.out.println(str.stripTrailing()); // "  Hello World"

// repeat()
System.out.println("abc".repeat(3)); // "abcabcabc"
```

### 3. Collection to Array

Easier conversion from Collection to typed array.

**Example:**
```java
List<String> names = Arrays.asList("John", "Jane", "Jack");

// Before Java 11
String[] array = names.toArray(new String[names.size()]);

// With Java 11
String[] array = names.toArray(String[]::new);
```

### 4. Files Methods

New methods for reading and writing files.

**Example:**
```java
// Write string to file
Path path = Files.writeString(
    Path.of("example.txt"), 
    "Hello World"
);

// Read string from file
String content = Files.readString(path);
```

### 5. HTTP Client API

Standardized HTTP client (replaces HttpURLConnection).

**Example:**
```java
HttpClient client = HttpClient.newHttpClient();

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.example.com/data"))
    .GET()
    .build();

HttpResponse<String> response = client.send(
    request, 
    HttpResponse.BodyHandlers.ofString()
);

System.out.println(response.body());
```

### 6. Running Java Files Directly

Execute Java source files without explicit compilation.

**Example:**
```bash
# Java 11+
java HelloWorld.java

# No need for javac anymore for single-file programs
```

### 7. Predicate.not()

Negate a predicate method reference.

**Example:**
```java
List<String> names = Arrays.asList("John", "", "Jane", "  ", "Jack");

// Before Java 11
names.stream()
    .filter(s -> !s.isBlank())
    .collect(Collectors.toList());

// With Java 11
names.stream()
    .filter(Predicate.not(String::isBlank))
    .collect(Collectors.toList());
```

## Code Examples

See the [examples](./examples) directory for complete working code:
- `LambdaExamples.java` - Lambda expressions demonstrations
- `StreamAPIExamples.java` - Stream operations
- `OptionalExamples.java` - Optional usage patterns
- `DateTimeExamples.java` - Date and Time API
- `Java11Features.java` - Java 11 specific features

## Exercises

### Exercise 1: Lambda Expressions
Create a program that filters and processes a list of employees:
- Filter employees with salary > 50000
- Sort by name
- Print names in uppercase

### Exercise 2: Stream API
Given a list of integers, use streams to:
- Find the sum of all even numbers
- Get the maximum value
- Count numbers greater than 10
- Create a list of squares of all numbers

### Exercise 3: Optional
Create a service that:
- Finds a user by ID (may not exist)
- Returns user's email in uppercase
- Returns "NO EMAIL" if user not found or has no email

### Exercise 4: Date and Time
Create a program to:
- Calculate age from date of birth
- Find the number of days until next birthday
- Display all dates of upcoming Fridays in the next 30 days

### Exercise 5: CompletableFuture
Create an asynchronous service that:
- Fetches user data
- Fetches user orders concurrently
- Combines both results
- Handles errors appropriately

## Solutions

Solutions to exercises are available in the [solutions](./solutions) directory.

## Key Takeaways

✅ Lambda expressions simplify code and enable functional programming  
✅ Stream API provides powerful data processing capabilities  
✅ Optional prevents NullPointerException  
✅ New Date and Time API is thread-safe and immutable  
✅ Java 11 introduces convenient String and File methods  
✅ CompletableFuture enables reactive programming  

## Resources

### Official Documentation
- [Java 8 Documentation](https://docs.oracle.com/javase/8/docs/)
- [Java 11 Documentation](https://docs.oracle.com/en/java/javase/11/)

### Tutorials
- [Java 8 Stream API Tutorial](https://www.oracle.com/technical-resources/articles/java/ma14-java-se-8-streams.html)
- [Java Optional Guide](https://www.oracle.com/technical-resources/articles/java/java8-optional.html)

### Books
- "Java 8 in Action" by Raoul-Gabriel Urma
- "Modern Java in Action" by Raoul-Gabriel Urma

## Next Steps

Tomorrow, we'll dive into **Setting Up Spring Boot Project** where we'll use these Java features to build our first Spring Boot application!

---

[<< Back to Main](../README.md) | [Next: Day 02 >>](../Day02_SpringBoot_Setup/README.md)
