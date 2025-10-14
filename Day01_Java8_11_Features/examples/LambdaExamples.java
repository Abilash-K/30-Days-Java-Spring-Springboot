package examples;

import java.util.*;
import java.util.function.*;

/**
 * Comprehensive Lambda Expression Examples
 * Demonstrates various ways to use lambda expressions in Java 8
 */
public class LambdaExamples {

    public static void main(String[] args) {
        System.out.println("=== Lambda Expressions Examples ===\n");
        
        basicLambdas();
        functionalInterfaces();
        methodReferences();
        lambdasWithCollections();
    }

    /**
     * Basic lambda expression examples
     */
    private static void basicLambdas() {
        System.out.println("1. Basic Lambda Expressions:");
        
        // No parameters
        Runnable noParams = () -> System.out.println("   Hello from Lambda!");
        noParams.run();
        
        // Single parameter (parentheses optional)
        Consumer<String> singleParam = message -> System.out.println("   " + message);
        singleParam.accept("Single parameter lambda");
        
        // Multiple parameters
        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
        System.out.println("   5 + 3 = " + add.apply(5, 3));
        
        // Multi-line lambda
        BiFunction<Integer, Integer, Integer> multiply = (a, b) -> {
            int result = a * b;
            return result;
        };
        System.out.println("   5 * 3 = " + multiply.apply(5, 3));
        
        System.out.println();
    }

    /**
     * Functional Interface examples
     */
    private static void functionalInterfaces() {
        System.out.println("2. Functional Interfaces:");
        
        // Predicate - tests a condition
        Predicate<Integer> isEven = num -> num % 2 == 0;
        System.out.println("   Is 4 even? " + isEven.test(4));
        System.out.println("   Is 5 even? " + isEven.test(5));
        
        // Function - transforms input to output
        Function<String, Integer> stringLength = String::length;
        System.out.println("   Length of 'Hello': " + stringLength.apply("Hello"));
        
        // Consumer - accepts input, returns nothing
        Consumer<String> printer = msg -> System.out.println("   Consumer: " + msg);
        printer.accept("Processing data");
        
        // Supplier - provides output without input
        Supplier<Double> randomSupplier = Math::random;
        System.out.println("   Random number: " + randomSupplier.get());
        
        // BiPredicate - tests condition with two inputs
        BiPredicate<String, Integer> lengthChecker = (str, len) -> str.length() > len;
        System.out.println("   Is 'Hello' longer than 3? " + lengthChecker.test("Hello", 3));
        
        System.out.println();
    }

    /**
     * Method reference examples
     */
    private static void methodReferences() {
        System.out.println("3. Method References:");
        
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David");
        
        // Static method reference
        System.out.println("   Converting to uppercase:");
        names.stream()
             .map(String::toUpperCase)
             .forEach(name -> System.out.println("   - " + name));
        
        // Instance method reference
        String prefix = "   Mr. ";
        Consumer<String> greet = prefix::concat;
        
        // Constructor reference
        Supplier<List<String>> listSupplier = ArrayList::new;
        List<String> newList = listSupplier.get();
        System.out.println("   Created new list: " + newList.getClass().getSimpleName());
        
        System.out.println();
    }

    /**
     * Lambda expressions with collections
     */
    private static void lambdasWithCollections() {
        System.out.println("4. Lambdas with Collections:");
        
        List<Person> people = Arrays.asList(
            new Person("Alice", 30),
            new Person("Bob", 25),
            new Person("Charlie", 35),
            new Person("David", 28)
        );
        
        // Sorting with lambda
        System.out.println("   Sorted by age:");
        people.stream()
              .sorted((p1, p2) -> Integer.compare(p1.getAge(), p2.getAge()))
              .forEach(p -> System.out.println("   - " + p));
        
        // Filtering with lambda
        System.out.println("\n   People older than 28:");
        people.stream()
              .filter(p -> p.getAge() > 28)
              .forEach(p -> System.out.println("   - " + p));
        
        // Mapping with lambda
        System.out.println("\n   Names in uppercase:");
        people.stream()
              .map(p -> p.getName().toUpperCase())
              .forEach(name -> System.out.println("   - " + name));
        
        System.out.println();
    }

    /**
     * Simple Person class for demonstrations
     */
    static class Person {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        @Override
        public String toString() {
            return name + " (" + age + ")";
        }
    }
}
