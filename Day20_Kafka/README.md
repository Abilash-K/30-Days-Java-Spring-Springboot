# Day 20: Event-Driven Architecture with Apache Kafka

## 📋 Table of Contents
- [Introduction](#introduction)
- [What is Apache Kafka?](#what-is-apache-kafka)
- [Kafka Architecture](#kafka-architecture)
- [Kafka Core Concepts](#kafka-core-concepts)
- [Setting Up Kafka](#setting-up-kafka)
- [Spring Boot with Kafka](#spring-boot-with-kafka)
- [Advanced Patterns](#advanced-patterns)
- [Best Practices](#best-practices)
- [Exercises](#exercises)
- [Resources](#resources)

## Introduction

Welcome to Day 20! Today we'll explore Apache Kafka, a distributed event streaming platform that's essential for building scalable, event-driven microservices. Kafka enables high-throughput, fault-tolerant, and real-time data processing.

## What is Apache Kafka?

### Overview
Apache Kafka is a distributed streaming platform used for:
- **Messaging**: Publish and subscribe to streams of records
- **Storage**: Store streams of records durably and reliably
- **Processing**: Process streams of records as they occur

### Use Cases

**1. Messaging System**
- Replacement for traditional message brokers
- Higher throughput and better scalability
- Built-in partitioning and replication

**2. Event Sourcing**
- Store all state changes as sequence of events
- Reconstruct application state from events
- Audit trail and debugging

**3. Stream Processing**
- Real-time analytics
- Data transformation pipelines
- Complex event processing

**4. Log Aggregation**
- Collect logs from multiple services
- Centralized log processing
- Real-time monitoring

**5. Metrics Collection**
- Collect application metrics
- Operational monitoring
- Performance tracking

## Kafka Architecture

### Core Components

```
┌─────────────────────────────────────────────────────────┐
│                    Kafka Cluster                        │
│                                                         │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐         │
│  │ Broker 1 │    │ Broker 2 │    │ Broker 3 │         │
│  │          │    │          │    │          │         │
│  │ Topics:  │    │ Topics:  │    │ Topics:  │         │
│  │ -orders  │    │ -orders  │    │ -orders  │         │
│  │ -users   │    │ -users   │    │ -users   │         │
│  └──────────┘    └──────────┘    └──────────┘         │
│                                                         │
└─────────────────────────────────────────────────────────┘
           ▲                              │
           │                              │
    ┌──────┴──────┐            ┌─────────▼──────┐
    │  Producers  │            │   Consumers    │
    │             │            │                │
    │ - Service A │            │ - Service X    │
    │ - Service B │            │ - Service Y    │
    └─────────────┘            └────────────────┘
           ▲                              │
           │                              │
       ┌───┴────┐                    ┌────▼────┐
       │ZooKeeper│◄──────────────────│ZooKeeper│
       │         │   Coordination    │         │
       └─────────┘                    └─────────┘
```

### Key Components

**1. Broker**
- Kafka server that stores data
- Handles read/write requests
- Manages data replication

**2. Topic**
- Category or feed name for messages
- Similar to table in database
- Can have multiple partitions

**3. Partition**
- Topics split into partitions
- Each partition is ordered, immutable sequence
- Enables parallel processing

**4. Producer**
- Publishes messages to topics
- Decides which partition to send data to
- Can send data synchronously or asynchronously

**5. Consumer**
- Reads messages from topics
- Part of consumer group for parallel processing
- Maintains offset (position in partition)

**6. ZooKeeper (being phased out)**
- Manages cluster metadata
- Leader election
- Configuration management

## Kafka Core Concepts

### 1. Topics and Partitions

```
Topic: orders
┌─────────────────────────────────────────────────┐
│ Partition 0: [msg1][msg2][msg3][msg4][msg5]    │
│ Partition 1: [msg6][msg7][msg8][msg9][msg10]   │
│ Partition 2: [msg11][msg12][msg13][msg14]      │
└─────────────────────────────────────────────────┘
```

**Characteristics:**
- Each partition is ordered
- Messages within partition are immutable
- Each message has unique offset
- Partitions enable scalability

### 2. Replication

```
Topic: orders (Replication Factor: 3)
┌────────────────────────────────────────┐
│ Partition 0                            │
│ Leader: Broker 1                       │
│ Replicas: Broker 2, Broker 3          │
└────────────────────────────────────────┘
```

**Benefits:**
- Fault tolerance
- High availability
- Data durability

### 3. Consumer Groups

```
Consumer Group: order-processors
┌─────────────────────────────────────────┐
│ Consumer 1 ──► Partition 0              │
│ Consumer 2 ──► Partition 1              │
│ Consumer 3 ──► Partition 2              │
└─────────────────────────────────────────┘
```

**Features:**
- Parallel consumption
- Load balancing
- Fault tolerance

### 4. Message Format

```json
{
  "key": "order-123",
  "value": {
    "orderId": "123",
    "userId": "user-456",
    "amount": 99.99,
    "status": "CREATED"
  },
  "timestamp": 1623456789000,
  "headers": {
    "correlation-id": "abc-123",
    "source": "order-service"
  }
}
```

## Setting Up Kafka

### Using Docker Compose

```yaml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
```

### Start Kafka

```bash
# Start services
docker-compose up -d

# Verify Kafka is running
docker ps

# Create topic
docker exec -it kafka kafka-topics --create \
  --topic orders \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# List topics
docker exec -it kafka kafka-topics --list \
  --bootstrap-server localhost:9092
```

## Spring Boot with Kafka

### Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Spring Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Jackson for JSON -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
</dependencies>
```

### Configuration (application.yml)

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
      properties:
        enable.idempotence: true
    
    consumer:
      group-id: order-service-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
      auto-offset-reset: earliest
      enable-auto-commit: false
    
    listener:
      ack-mode: manual
```

### Producer Implementation

**1. Event Model**
```java
public class OrderCreatedEvent {
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    
    // Constructors, getters, setters
    
    public OrderCreatedEvent() {
        this.timestamp = LocalDateTime.now();
    }
}
```

**2. Kafka Producer**
```java
@Service
public class OrderEventProducer {
    
    private static final String TOPIC = "order-events";
    
    @Autowired
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    
    public void publishOrderCreated(OrderCreatedEvent event) {
        // Send with callback
        ListenableFuture<SendResult<String, OrderCreatedEvent>> future = 
            kafkaTemplate.send(TOPIC, event.getOrderId(), event);
        
        future.addCallback(
            result -> {
                log.info("Order event published successfully: {}", 
                    event.getOrderId());
            },
            ex -> {
                log.error("Failed to publish order event: {}", 
                    event.getOrderId(), ex);
            }
        );
    }
    
    // Synchronous send
    public void publishOrderCreatedSync(OrderCreatedEvent event) 
            throws ExecutionException, InterruptedException {
        SendResult<String, OrderCreatedEvent> result = 
            kafkaTemplate.send(TOPIC, event.getOrderId(), event).get();
        log.info("Message sent to partition: {}", 
            result.getRecordMetadata().partition());
    }
    
    // Send with headers
    public void publishWithHeaders(OrderCreatedEvent event) {
        ProducerRecord<String, OrderCreatedEvent> record = 
            new ProducerRecord<>(TOPIC, event.getOrderId(), event);
        
        record.headers().add("correlation-id", 
            UUID.randomUUID().toString().getBytes());
        record.headers().add("source", 
            "order-service".getBytes());
        
        kafkaTemplate.send(record);
    }
}
```

**3. Using Producer in Service**
```java
@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderEventProducer eventProducer;
    
    @Transactional
    public Order createOrder(OrderRequest request) {
        // Create order
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setAmount(request.getAmount());
        order.setStatus(OrderStatus.CREATED);
        
        Order savedOrder = orderRepository.save(order);
        
        // Publish event
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(savedOrder.getId());
        event.setUserId(savedOrder.getUserId());
        event.setAmount(savedOrder.getAmount());
        
        eventProducer.publishOrderCreated(event);
        
        return savedOrder;
    }
}
```

### Consumer Implementation

**1. Basic Consumer**
```java
@Service
public class OrderEventConsumer {
    
    @KafkaListener(
        topics = "order-events",
        groupId = "notification-service"
    )
    public void consumeOrderEvent(OrderCreatedEvent event) {
        log.info("Received order event: {}", event.getOrderId());
        
        // Process event (send notification, update inventory, etc.)
        processOrderCreated(event);
    }
    
    private void processOrderCreated(OrderCreatedEvent event) {
        // Business logic here
        emailService.sendOrderConfirmation(event.getUserId());
    }
}
```

**2. Consumer with Manual Acknowledgment**
```java
@Service
public class OrderEventConsumer {
    
    @KafkaListener(
        topics = "order-events",
        groupId = "inventory-service"
    )
    public void consumeWithAck(
            ConsumerRecord<String, OrderCreatedEvent> record,
            Acknowledgment acknowledgment) {
        
        try {
            OrderCreatedEvent event = record.value();
            log.info("Processing order: {}", event.getOrderId());
            
            // Process event
            inventoryService.reserveItems(event);
            
            // Manual acknowledgment
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing order event", e);
            // Don't acknowledge - message will be reprocessed
        }
    }
}
```

**3. Consumer with Error Handling**
```java
@Service
public class OrderEventConsumer {
    
    @KafkaListener(
        topics = "order-events",
        groupId = "payment-service",
        errorHandler = "kafkaErrorHandler"
    )
    public void consumeWithErrorHandling(OrderCreatedEvent event) {
        log.info("Processing payment for order: {}", event.getOrderId());
        
        if (event.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderException("Invalid order amount");
        }
        
        paymentService.processPayment(event);
    }
}

@Component
public class KafkaErrorHandler implements ConsumerAwareListenerErrorHandler {
    
    @Override
    public Object handleError(Message<?> message, 
                            ListenerExecutionFailedException exception,
                            Consumer<?, ?> consumer) {
        log.error("Error in kafka listener", exception);
        
        // Implement retry logic or dead letter queue
        return null;
    }
}
```

### Configuration Class

```java
@Configuration
@EnableKafka
public class KafkaConfig {
    
    // Producer Configuration
    @Bean
    public ProducerFactory<String, OrderCreatedEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, 
            StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
            JsonSerializer.class);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        
        return new DefaultKafkaProducerFactory<>(config);
    }
    
    @Bean
    public KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    // Consumer Configuration
    @Bean
    public ConsumerFactory<String, OrderCreatedEvent> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "order-service-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, 
            StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, 
            JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        return new DefaultKafkaConsumerFactory<>(config);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> 
            kafkaListenerContainerFactory() {
        
        ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(AckMode.MANUAL);
        
        return factory;
    }
}
```

## Advanced Patterns

### 1. Transactional Messaging

```java
@Service
public class TransactionalOrderService {
    
    @Autowired
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Transactional
    @KafkaTransactional
    public void createOrderTransactional(OrderRequest request) {
        // Database transaction
        Order order = orderRepository.save(new Order(request));
        
        // Kafka transaction (atomic with DB transaction)
        OrderCreatedEvent event = new OrderCreatedEvent(order);
        kafkaTemplate.send("order-events", event.getOrderId(), event);
        
        // Both succeed or both fail
    }
}
```

### 2. Dead Letter Queue

```java
@Configuration
public class KafkaDeadLetterConfig {
    
    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<String, Object> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate);
    }
    
    @Bean
    public DefaultErrorHandler errorHandler(
            DeadLetterPublishingRecoverer recoverer) {
        return new DefaultErrorHandler(recoverer, 
            new FixedBackOff(1000L, 3L));
    }
}
```

### 3. Batch Processing

```java
@Service
public class BatchOrderConsumer {
    
    @KafkaListener(
        topics = "order-events",
        groupId = "batch-processor"
    )
    public void consumeBatch(List<OrderCreatedEvent> events) {
        log.info("Processing batch of {} orders", events.size());
        
        // Process batch
        List<Order> orders = events.stream()
            .map(this::processEvent)
            .collect(Collectors.toList());
        
        orderRepository.saveAll(orders);
    }
}
```

### 4. Kafka Streams

```java
@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {
    
    @Bean
    public KStream<String, OrderCreatedEvent> orderStream(
            StreamsBuilder builder) {
        
        KStream<String, OrderCreatedEvent> stream = 
            builder.stream("order-events");
        
        // Filter high-value orders
        stream.filter((key, order) -> 
                order.getAmount().compareTo(new BigDecimal("1000")) > 0)
              .to("high-value-orders");
        
        // Aggregate orders by user
        stream.groupBy((key, order) -> order.getUserId())
              .aggregate(
                  () -> new OrderStats(),
                  (userId, order, stats) -> stats.add(order),
                  Materialized.as("user-order-stats")
              );
        
        return stream;
    }
}
```

## Best Practices

### 1. Message Design
```java
// Good: Include all necessary information
public class OrderEvent {
    private String eventId;        // Unique event identifier
    private String eventType;      // Event type
    private LocalDateTime timestamp; // Event timestamp
    private String orderId;        // Business entity ID
    private OrderData data;        // Actual data
    private Map<String, String> metadata; // Additional context
}

// Bad: Missing context
public class OrderEvent {
    private String orderId;
    private BigDecimal amount;
}
```

### 2. Idempotency
```java
@Service
public class IdempotentOrderConsumer {
    
    @Autowired
    private ProcessedEventRepository processedEvents;
    
    @KafkaListener(topics = "order-events")
    public void consume(OrderCreatedEvent event) {
        String eventId = event.getEventId();
        
        // Check if already processed
        if (processedEvents.existsById(eventId)) {
            log.info("Event already processed: {}", eventId);
            return;
        }
        
        // Process event
        processOrder(event);
        
        // Mark as processed
        processedEvents.save(new ProcessedEvent(eventId));
    }
}
```

### 3. Error Handling
```java
@Service
public class RobustOrderConsumer {
    
    @Retryable(
        value = {TransientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    @KafkaListener(topics = "order-events")
    public void consumeWithRetry(OrderCreatedEvent event) {
        try {
            processOrder(event);
        } catch (PermanentException e) {
            // Send to DLQ
            sendToDeadLetterQueue(event, e);
        }
    }
}
```

### 4. Monitoring
```java
@Service
public class MonitoredOrderProducer {
    
    @Autowired
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    public void publishOrder(OrderCreatedEvent event) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            kafkaTemplate.send("order-events", event).get();
            
            meterRegistry.counter("kafka.producer.success", 
                "topic", "order-events").increment();
                
        } catch (Exception e) {
            meterRegistry.counter("kafka.producer.failure",
                "topic", "order-events").increment();
            throw new RuntimeException(e);
            
        } finally {
            sample.stop(Timer.builder("kafka.producer.duration")
                .tag("topic", "order-events")
                .register(meterRegistry));
        }
    }
}
```

## Exercises

### Exercise 1: Basic Kafka Setup
- Set up Kafka using Docker Compose
- Create topics for: orders, payments, notifications
- Verify setup with Kafka CLI tools

### Exercise 2: Producer-Consumer
Build a simple order processing system:
- Order Service (Producer): Creates orders and publishes events
- Notification Service (Consumer): Sends notifications
- Inventory Service (Consumer): Updates inventory

### Exercise 3: Event-Driven Saga
Implement a saga pattern for order processing:
- Order Service: Initiates order
- Payment Service: Processes payment
- Inventory Service: Reserves items
- Handle compensation on failures

### Exercise 4: Kafka Streams
Create a stream processing application:
- Real-time order statistics
- Fraud detection (orders over threshold)
- User activity tracking

### Exercise 5: Monitoring
Add monitoring to Kafka application:
- Producer metrics (success rate, latency)
- Consumer lag monitoring
- Error rate tracking
- Implement health checks

## Key Takeaways

✅ Kafka enables high-throughput, fault-tolerant event streaming  
✅ Topics are partitioned for scalability  
✅ Consumer groups enable parallel processing  
✅ Spring Kafka simplifies integration  
✅ Idempotency is crucial for message processing  
✅ Proper error handling prevents data loss  

## Resources

### Official Documentation
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka Documentation](https://spring.io/projects/spring-kafka)
- [Kafka Streams](https://kafka.apache.org/documentation/streams/)

### Books
- "Kafka: The Definitive Guide" by Neha Narkhede
- "Event-Driven Microservices" by Adam Bellemare

### Tools
- [Kafka Manager](https://github.com/yahoo/CMAK)
- [Kafdrop](https://github.com/obsidiandynamics/kafdrop)
- [Confluent Control Center](https://docs.confluent.io/platform/current/control-center/index.html)

## Next Steps

Tomorrow, we'll learn about **Docker Containerization Basics** to package our microservices for deployment!

---

[<< Previous: Day 19](../Day19_RabbitMQ/README.md) | [Back to Main](../README.md) | [Next: Day 21 >>](../Day21_Docker_Basics/README.md)
