package examples;

import java.util.*;
import java.util.stream.*;

/**
 * Comprehensive Stream API Examples
 * Demonstrates various Stream operations in Java 8
 */
public class StreamAPIExamples {

    public static void main(String[] args) {
        System.out.println("=== Stream API Examples ===\n");
        
        creatingStreams();
        intermediateOperations();
        terminalOperations();
        practicalExamples();
    }

    /**
     * Different ways to create streams
     */
    private static void creatingStreams() {
        System.out.println("1. Creating Streams:");
        
        // From collection
        List<String> list = Arrays.asList("A", "B", "C");
        Stream<String> stream1 = list.stream();
        System.out.println("   From list: " + stream1.collect(Collectors.toList()));
        
        // From array
        String[] array = {"X", "Y", "Z"};
        Stream<String> stream2 = Arrays.stream(array);
        System.out.println("   From array: " + stream2.collect(Collectors.toList()));
        
        // Using Stream.of()
        Stream<Integer> stream3 = Stream.of(1, 2, 3, 4, 5);
        System.out.println("   Using Stream.of(): " + stream3.collect(Collectors.toList()));
        
        // Infinite stream with limit
        Stream<Integer> stream4 = Stream.iterate(0, n -> n + 2).limit(5);
        System.out.println("   Infinite stream (limited): " + stream4.collect(Collectors.toList()));
        
        System.out.println();
    }

    /**
     * Intermediate operations (return a stream)
     */
    private static void intermediateOperations() {
        System.out.println("2. Intermediate Operations:");
        
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        // filter() - select elements based on condition
        System.out.println("   Even numbers:");
        numbers.stream()
               .filter(n -> n % 2 == 0)
               .forEach(n -> System.out.print("   " + n + " "));
        System.out.println("\n");
        
        // map() - transform each element
        System.out.println("   Squared numbers:");
        numbers.stream()
               .map(n -> n * n)
               .forEach(n -> System.out.print("   " + n + " "));
        System.out.println("\n");
        
        // sorted() - sort elements
        List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
        System.out.println("   Sorted names:");
        names.stream()
             .sorted()
             .forEach(name -> System.out.println("   - " + name));
        
        // distinct() - remove duplicates
        List<Integer> duplicates = Arrays.asList(1, 2, 2, 3, 3, 3, 4, 4, 5);
        System.out.println("\n   Distinct numbers:");
        duplicates.stream()
                  .distinct()
                  .forEach(n -> System.out.print("   " + n + " "));
        System.out.println("\n");
        
        // limit() and skip()
        System.out.println("   First 3 numbers:");
        numbers.stream()
               .limit(3)
               .forEach(n -> System.out.print("   " + n + " "));
        
        System.out.println("\n\n   Skip first 5, then take 3:");
        numbers.stream()
               .skip(5)
               .limit(3)
               .forEach(n -> System.out.print("   " + n + " "));
        
        System.out.println("\n");
    }

    /**
     * Terminal operations (produce a result or side-effect)
     */
    private static void terminalOperations() {
        System.out.println("3. Terminal Operations:");
        
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        // forEach() - perform action on each element
        System.out.print("   forEach: ");
        numbers.stream().forEach(n -> System.out.print(n + " "));
        System.out.println();
        
        // collect() - collect stream into collection
        List<Integer> evenNumbers = numbers.stream()
                                           .filter(n -> n % 2 == 0)
                                           .collect(Collectors.toList());
        System.out.println("   Collected even numbers: " + evenNumbers);
        
        // count() - count elements
        long count = numbers.stream()
                           .filter(n -> n > 5)
                           .count();
        System.out.println("   Count of numbers > 5: " + count);
        
        // reduce() - combine elements
        int sum = numbers.stream()
                        .reduce(0, Integer::sum);
        System.out.println("   Sum using reduce: " + sum);
        
        int product = numbers.stream()
                            .reduce(1, (a, b) -> a * b);
        System.out.println("   Product using reduce: " + product);
        
        // min() and max()
        Optional<Integer> min = numbers.stream().min(Integer::compareTo);
        Optional<Integer> max = numbers.stream().max(Integer::compareTo);
        System.out.println("   Min: " + min.orElse(0) + ", Max: " + max.orElse(0));
        
        // anyMatch(), allMatch(), noneMatch()
        boolean hasEven = numbers.stream().anyMatch(n -> n % 2 == 0);
        boolean allPositive = numbers.stream().allMatch(n -> n > 0);
        boolean noneNegative = numbers.stream().noneMatch(n -> n < 0);
        System.out.println("   Has even: " + hasEven);
        System.out.println("   All positive: " + allPositive);
        System.out.println("   None negative: " + noneNegative);
        
        System.out.println();
    }

    /**
     * Practical real-world examples
     */
    private static void practicalExamples() {
        System.out.println("4. Practical Examples:");
        
        List<Employee> employees = Arrays.asList(
            new Employee("Alice", "IT", 75000),
            new Employee("Bob", "HR", 55000),
            new Employee("Charlie", "IT", 85000),
            new Employee("David", "Finance", 65000),
            new Employee("Eve", "IT", 70000),
            new Employee("Frank", "HR", 60000)
        );
        
        // Group employees by department
        System.out.println("   Employees by department:");
        Map<String, List<Employee>> byDept = employees.stream()
            .collect(Collectors.groupingBy(Employee::getDepartment));
        byDept.forEach((dept, emps) -> {
            System.out.println("   " + dept + ":");
            emps.forEach(e -> System.out.println("     - " + e.getName()));
        });
        
        // Average salary by department
        System.out.println("\n   Average salary by department:");
        Map<String, Double> avgSalary = employees.stream()
            .collect(Collectors.groupingBy(
                Employee::getDepartment,
                Collectors.averagingDouble(Employee::getSalary)
            ));
        avgSalary.forEach((dept, avg) -> 
            System.out.printf("   %s: $%.2f%n", dept, avg)
        );
        
        // Top 3 earners
        System.out.println("\n   Top 3 earners:");
        employees.stream()
                 .sorted(Comparator.comparing(Employee::getSalary).reversed())
                 .limit(3)
                 .forEach(e -> System.out.println("   - " + e));
        
        // Statistics
        System.out.println("\n   Salary statistics:");
        IntSummaryStatistics stats = employees.stream()
            .mapToInt(Employee::getSalary)
            .summaryStatistics();
        System.out.println("   Min: $" + stats.getMin());
        System.out.println("   Max: $" + stats.getMax());
        System.out.println("   Average: $" + stats.getAverage());
        System.out.println("   Total: $" + stats.getSum());
        
        // Parallel streams for performance
        System.out.println("\n   Using parallel stream:");
        long startTime = System.currentTimeMillis();
        employees.parallelStream()
                 .filter(e -> e.getSalary() > 60000)
                 .forEach(e -> System.out.println("   - " + e.getName()));
        long endTime = System.currentTimeMillis();
        System.out.println("   Execution time: " + (endTime - startTime) + "ms");
        
        System.out.println();
    }

    /**
     * Employee class for demonstrations
     */
    static class Employee {
        private String name;
        private String department;
        private int salary;

        public Employee(String name, String department, int salary) {
            this.name = name;
            this.department = department;
            this.salary = salary;
        }

        public String getName() {
            return name;
        }

        public String getDepartment() {
            return department;
        }

        public int getSalary() {
            return salary;
        }

        @Override
        public String toString() {
            return name + " (" + department + ", $" + salary + ")";
        }
    }
}
