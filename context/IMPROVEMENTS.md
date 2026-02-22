# SQS Consumer Project - Improvements Document

This document outlines all improvements that can be applied to enhance the SQS Consumer project for production readiness, maintainability, and code quality.

---

## Table of Contents

1. [Code Quality Improvements](#1-code-quality-improvements)
2. [Configuration Improvements](#2-configuration-improvements)
3. [Error Handling & Resilience](#3-error-handling--resilience)
4. [Testing Improvements](#4-testing-improvements)
5. [Observability & Monitoring](#5-observability--monitoring)
6. [Security Improvements](#6-security-improvements)
7. [Project Structure](#7-project-structure)
8. [Build & Dependencies](#8-build--dependencies)

---

## 1. Code Quality Improvements

### 1.1 Replace println with SLF4J Logger

**Current Code (SqsConsumerService.kt):**
```kotlin
fun receiveMessage(message: String) {
    println("Message received: === $message ===")
}
```

**Issues:**
- No log levels (DEBUG, INFO, WARN, ERROR)
- No structured logging
- Cannot be disabled in production

**Recommended Fix:**
```kotlin
import org.slf4j.LoggerFactory

@Service
class SqsConsumerService {
    private val logger = LoggerFactory.getLogger(SqsConsumerService::class.java)

    @SqsListener("\${aws.sqs.queue-name}")
    fun receiveMessage(message: String) {
        logger.info("Message received: === {} ===", message)
    }
}
```

---

### 1.2 Use Message DTOs Instead of Raw Strings

**Current Code:**
```kotlin
fun receiveMessage(message: String)
```

**Recommended Fix - Create a DTO:**
```kotlin
package com.example.sqs_consumer.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderMessage(
    val orderId: String,
    val customerId: String,
    val amount: Double,
    val timestamp: String
)
```

**Update the consumer:**
```kotlin
@SqsListener("\${aws.sqs.queue-name}")
fun receiveMessage(message: OrderMessage) {
    logger.info("Processing order: {}", message.orderId)
    // Business logic here
}
```

---

### 1.3 Add Input Validation

Add validation annotations to DTOs:
```kotlin
@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderMessage(
    @NotBlank
    val orderId: String,

    @NotBlank
    val customerId: String,

    @Positive
    val amount: Double,

    @NotBlank
    val timestamp: String
)
```

Add validation dependency to `build.gradle.kts`:
```kotlin
implementation("org.springframework.boot:spring-boot-starter-validation")
```

---

### 1.4 Extract Business Logic to Domain Layer

**Current:** All logic is in `SqsConsumerService`

**Recommended:** Separate concerns
```
src/main/kotlin/comconsumer/
├── dto/example/sqs_/
│   └── OrderMessage.kt
├── domain/
│   └── OrderProcessor.kt          # Business logic
├── infra/
│   └── SqsConsumerService.kt       # Infrastructure only
└── config/
    └── SqsConfig.kt
```

---

## 2. Configuration Improvements

### 2.1 Externalize Hardcoded Values

**Current (SqsConfig.kt):**
```kotlin
val localstackUrl = "http://localhost:4566"
// Hardcoded credentials and region
```

**Recommended - Move to application.yml:**
```yaml
# application.yml
aws:
  sqs:
    endpoint: "http://localhost:4566"
    queue-name: "terraform-example-queue.fifo"
    region: "us-east-1"
    credentials:
      access-key: "mock_access_key"
      secret-key: "mock_secret_key"
```

**Update SqsConfig.kt:**
```kotlin
@Configuration
class SqsConfig(
    @Value("\${aws.sqs.endpoint}") private val endpoint: String,
    @Value("\${aws.sqs.region}") private val region: String,
    @Value("\${aws.sqs.credentials.access-key}") private val accessKey: String,
    @Value("\${aws.sqs.credentials.secret-key}") private val secretKey: String
) {
    @Bean
    fun sqsAsyncClient(): SqsAsyncClient {
        return SqsAsyncClient.builder()
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            ))
            .region(Region.of(region))
            .build()
    }
}
```

---

### 2.2 Use Configuration Properties Class

Create a type-safe configuration:
```kotlin
package com.example.sqs_consumer.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "aws.sqs")
data class SqsProperties(
    val endpoint: String,
    val queueName: String,
    val region: String,
    val credentials: SqsCredentials
)

data class SqsCredentials(
    val accessKey: String,
    val secretKey: String
)
```

---

### 2.3 Add Environment-Specific Profiles

Create `application-local.yml`, `application-dev.yml`, `application-prod.yml` with appropriate configurations.

**application-prod.yml example:**
```yaml
aws:
  sqs:
    endpoint: ""  # Use AWS default endpoint
    queue-name: "production-queue.fifo"
    region: "us-east-1"
    # Credentials from IAM role, not stored
```

---

## 3. Error Handling & Resilience

### 3.1 Add Try-Catch with Error Handling

```kotlin
@SqsListener("\${aws.sqs.queue-name}")
fun receiveMessage(message: OrderMessage) {
    try {
        logger.info("Processing order: {}", message.orderId)
        processOrder(message)
        logger.info("Successfully processed order: {}", message.orderId)
    } catch (e: Exception) {
        logger.error("Failed to process order {}: {}", message.orderId, e.message, e)
        throw e // Re-throw to trigger retry/DLQ
    }
}
```

---

### 3.2 Configure Dead Letter Queue (DLQ)

```yaml
cloud:
  aws:
    sqs:
      listener:
        ack-mode: manual
        retry:
          interceptor:
            max-attempts: 3
            back-off:
              initial-interval: 1000
              multiplier: 2.0
              max-interval: 10000
```

Add DLQ configuration in SqsConfig:
```kotlin
@Bean
fun queueMessagingTemplate(sqsAsyncClient: SqsAsyncClient): QueueMessagingTemplate {
    return QueueMessagingTemplate(sqsAsyncClient)
}
```

---

### 3.3 Add Spring Retry

Add dependency:
```kotlin
implementation("org.springframework.retry:spring-retry")
implementation("org.springframework.boot:spring-boot-starter-aop")
```

Enable retry:
```kotlin
@SpringBootApplication
@EnableRetry
class SqsConsumerApplication
```

---

### 3.4 Add Resilience4j Circuit Breaker

```kotlin
implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")

@SqsListener("\${aws.sqs.queue-name}")
@CircuitBreaker(name = "orderProcessing", fallbackMethod = "processOrderFallback")
fun receiveMessage(message: OrderMessage) {
    processOrder(message)
}

private fun processOrderFallback(message: OrderMessage, e: Exception) {
    logger.warn("Circuit breaker fallback for order: {}", message.orderId)
    // Handle fallback - e.g., save to database for later processing
}
```

---

## 4. Testing Improvements

### 4.1 Add Unit Tests

```kotlin
package com.example.sqs_consumer.infra

import com.example.sqs_consumer.dto.OrderMessage
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SqsConsumerServiceTest : DescribeSpec({
    describe("SqsConsumerService") {
        it("should log received message") {
            val service = SqsConsumerService()
            service.receiveMessage("test-message")
            // Add assertions for logging
        }
    }
})
```

---

### 4.2 Add Integration Tests with LocalStack

```kotlin
package com.example.sqs_consumer.integration

import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import io.awspring.cloud.sqs.operations.SqsTemplate

@Testcontainers
class SqsConsumerIntegrationTest {

    @Container
    val localstack = LocalStackContainer(DockerImageName.parse("localstack/localstack:3.0"))
        .withServices(SQS)

    @Test
    fun `should consume message from sqs`() {
        // Send message to LocalStack SQS
        // Verify consumer processes it
    }
}
```

Add Testcontainers dependencies:
```kotlin
testImplementation("org.testcontainers:testcontainers:1.19.3")
testImplementation("org.testcontainers:localstack:1.19.3")
testImplementation("org.testcontainers:junit-jupiter:1.19.3")
```

---

### 4.3 Add Testcontainers Support in build.gradle.kts

```kotlin
test {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")
}
```

---

## 5. Observability & Monitoring

### 5.1 Add Spring Boot Actuator

Add dependency:
```kotlin
implementation("org.springframework.boot:spring-boot-starter-actuator")
```

Configure in `application.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true
```

---

### 5.2 Add Micrometer Metrics

```kotlin
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Counter

@Service
class SqsConsumerService(
    private val meterRegistry: MeterRegistry
) {
    private val messageCounter = Counter.builder("sqs.messages.received")
        .description("Number of messages received")
        .register(meterRegistry)

    @SqsListener("\${aws.sqs.queue-name}")
    fun receiveMessage(message: OrderMessage) {
        messageCounter.increment()
        // Process message
    }
}
```

---

### 5.3 Add Custom Health Indicator

```kotlin
@Component
class SqsHealthIndicator(
    private val sqsAsyncClient: SqsAsyncClient
) : HealthIndicator {

    override fun health(): Health {
        return try {
            sqsAsyncClient.listQueues().get()
            Health.up().withDetail("sqs", "Connected").build()
        } catch (e: Exception) {
            Health.down().withDetail("sqs", e.message).build()
        }
    }
}
```

---

## 6. Security Improvements

### 6.1 Remove Hardcoded Credentials

Never store credentials in code or configuration files. Use:
- Environment variables
- AWS IAM roles (production)
- AWS Secrets Manager

```yaml
# Do NOT store credentials in production
# Use IAM role or environment variables instead
aws:
  sqs:
    # credentials will be loaded from IAM role in production
    region: "us-east-1"
```

---

### 6.2 Add Input Sanitization

```kotlin
import org.owasp.encoder.Encode

@SqsListener("\${aws.sqs.queue-name}")
fun receiveMessage(message: OrderMessage) {
    val sanitizedOrderId = Encode.forHtml(message.orderId)
    logger.info("Processing order: {}", sanitizedOrderId)
}
```

Add OWASP Encoder:
```kotlin
implementation("org.owasp.encoder:encoder:1.2.3")
```

---

### 6.3 Add Spring Security (if needed)

```kotlin
implementation("org.springframework.boot:spring-boot-starter-security")

@Configuration
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/actuator/health").permitAll()
                    .anyRequest().authenticated()
            }
            .build()
    }
}
```

---

## 7. Project Structure

### 7.1 Recommended Package Structure

```
src/main/kotlin/com/example/sqs_consumer/
├── SqsConsumerApplication.kt
├── config/
│   ├── SqsConfig.kt
│   └── SqsProperties.kt
├── dto/
│   └── OrderMessage.kt
├── domain/
│   ├── OrderProcessor.kt
│   └── OrderRepository.kt
├── infra/
│   └── SqsConsumerService.kt
└── health/
    └── SqsHealthIndicator.kt

src/test/kotlin/com/example/sqs_consumer/
├── infra/
│   └── SqsConsumerServiceTest.kt
└── integration/
    └── SqsConsumerIntegrationTest.kt
```

---

### 7.2 Separate Build Tools

Remove duplicate build tools (keep either Gradle OR Maven):
- Remove: `gradlew`, `gradle/wrapper/`, `build.gradle.kts` (if using Maven)
- OR remove: `mvnw`, `mvnw.cmd`, `.mvn/`, `pom.xml` (if using Gradle)

---

## 8. Build & Dependencies

### 8.1 Update Dependencies

Check for and apply updates:
- Spring Cloud AWS 3.x
- AWS SDK
- Spring Boot

### 8.2 Fix JaCoCo Exclusions

Update `build.gradle.kts`:
```kotlin
jacoco {
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.map {
            it.fileTree.exclude(
                "**/config/**",
                "**/*ApplicationKt.class"  // Only exclude main class
            )
        }))
    }
}
```

---

## Summary Checklist

| Priority | Improvement | Effort | Status |
|----------|-------------|--------|--------|
| High | Replace println with logger | Low | **Applied** |
| High | Externalize configuration | Low | **Applied** |
| High | Add error handling | Medium | **Applied** |
| High | Add unit tests | Medium | Pending |
| Medium | Add message DTOs | Low | **Applied** |
| Medium | Add integration tests | Medium | Pending |
| Medium | Add health checks | Low | Pending |
| Medium | Add DLQ configuration | Medium | Pending |
| Low | Add circuit breaker | Medium | Pending |
| Low | Add metrics | Medium | Pending |
| Low | Improve project structure | Medium | Pending |

---

## References

- [Spring Cloud AWS Documentation](https://docs.awspring.io/spring-cloud-aws/docs/3.0.1/reference/html/)
- [Spring Boot Best Practices](https://spring.io/best-practices)
- [AWS SQS Best Practices](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-best-practices.html)
- [12-Factor App](https://12factor.net/)
