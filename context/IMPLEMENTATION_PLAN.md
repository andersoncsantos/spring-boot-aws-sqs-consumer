# Implementation Plan for SQS Consumer Improvements

This plan outlines the step-by-step implementation of improvements for the SQS Consumer project.

---

## Phase 1: Critical Improvements (High Priority)

### Task 1.1: Replace println with SLF4J Logger
**Files:** `src/main/kotlin/com/example/sqs_consumer/infra/SqsConsumerService.kt`

**Steps:**
1. Add SLF4J import: `import org.slf4j.LoggerFactory`
2. Create logger instance: `private val logger = LoggerFactory.getLogger(SqsConsumerService::class.java)`
3. Replace `println()` with `logger.info()`

**Status:** ✅ Applied

---

### Task 1.2: Externalize Configuration
**Files:**
- `src/main/kotlin/com/example/sqs_consumer/config/SqsConfig.kt`
- `src/main/resources/application.yml`

**Steps:**
1. Update `application.yml` to add AWS configuration:
   ```yaml
   aws:
     sqs:
       endpoint: "http://localhost:4566"
       queue-name: "terraform-example-queue.fifo"
       region: "us-east-1"
       credentials:
         access-key: "mock_access_key"
         secret-key: "mock_secret_key"
   ```
2. Modify `SqsConfig.kt` to use constructor injection with `@Value` annotations
3. Remove hardcoded values from SqsConfig.kt

**Status:** ✅ Applied

---

### Task 1.3: Add Error Handling
**File:** `src/main/kotlin/com/example/sqs_consumer/infra/SqsConsumerService.kt`

**Steps:**
1. Add try-catch block around message processing
2. Add proper error logging
3. Re-throw exceptions to trigger retry/DLQ

**Status:** ✅ Applied

---

## Phase 2: Core Functionality (Medium Priority)

### Task 2.1: Create Message DTOs
**New Files:** `src/main/kotlin/com/example/sqs_consumer/dto/OrderMessage.kt`

**Steps:**
1. Create DTO class with Jackson annotations
2. Update `SqsConsumerService` to use `OrderMessage` instead of `String`
3. Add validation annotations (`@NotBlank`, `@Positive`)

**Status:** ✅ Applied

---

### Task 2.2: Add Validation Dependency
**File:** `build.gradle.kts`

**Steps:**
1. Add dependency: `implementation("org.springframework.boot:spring-boot-starter-validation")`

**Status:** ✅ Applied

---

### Task 2.3: Configure Dead Letter Queue
**Files:**
- `src/main/resources/application.yml`
- `src/main/kotlin/com/example/sqs_consumer/config/SqsConfig.kt`

**Steps:**
1. Add DLQ configuration in `application.yml`
2. Add retry configuration with max attempts and backoff
3. Add `QueueMessagingTemplate` bean if needed

**Status:** Not started

---

### Task 2.4: Add Spring Retry
**Files:**
- `build.gradle.kts`
- `src/main/kotlin/com/example/sqs_consumer/SqsConsumerApplication.kt`

**Steps:**
1. Add dependencies:
   ```kotlin
   implementation("org.springframework.retry:spring-retry")
   implementation("org.springframework.boot:spring-boot-starter-aop")
   ```
2. Add `@EnableRetry` to main application class

**Status:** Not started

---

## Phase 3: Testing (Medium Priority)

### Task 3.1: Add Unit Tests
**New File:** `src/test/kotlin/com/example/sqs_consumer/infra/SqsConsumerServiceTest.kt`

**Steps:**
1. Create unit test class using JUnit 5
2. Add test for message processing
3. Add test for error handling

**Status:** Not started

---

### Task 3.2: Add Integration Tests
**New Files:**
- `src/test/kotlin/com/example/sqs_consumer/integration/SqsConsumerIntegrationTest.kt`
- `src/test/resources/application-test.yml`

**Steps:**
1. Add Testcontainers dependencies to `build.gradle.kts`
2. Create integration test with LocalStack
3. Create test-specific configuration

**Status:** Not started

---

## Phase 4: Observability (Medium Priority)

### Task 4.1: Add Spring Boot Actuator
**Files:**
- `build.gradle.kts`
- `src/main/resources/application.yml`

**Steps:**
1. Add dependency: `implementation("org.springframework.boot:spring-boot-starter-actuator")`
2. Configure actuator endpoints in `application.yml`

**Status:** Not started

---

### Task 4.2: Add Custom Health Indicator
**New File:** `src/main/kotlin/com/example/sqs_consumer/health/SqsHealthIndicator.kt`

**Steps:**
1. Create `SqsHealthIndicator` class
2. Implement health check for SQS connection

**Status:** Not started

---

### Task 4.3: Add Micrometer Metrics
**File:** `src/main/kotlin/com/example/sqs_consumer/infra/SqsConsumerService.kt`

**Steps:**
1. Add `MeterRegistry` dependency (from actuator)
2. Create counters for messages received/processed/failed
3. Increment counters in message processing

**Status:** Not started

---

## Phase 5: Resilience (Low Priority)

### Task 5.1: Add Resilience4j Circuit Breaker
**Files:**
- `build.gradle.kts`
- `src/main/kotlin/com/example/sqs_consumer/infra/SqsConsumerService.kt`

**Steps:**
1. Add dependency: `implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")`
2. Add `@CircuitBreaker` annotation to consumer method
3. Implement fallback method

**Status:** Not started

---

## Phase 6: Project Cleanup (Low Priority)

### Task 6.1: Remove Duplicate Build Tools
**Files to remove:**
- If keeping Gradle: Remove `mvnw`, `mvnw.cmd`, `.mvn/`, `pom.xml`
- If keeping Maven: Remove `gradlew`, `gradlew.bat`, `gradle/`, `build.gradle.kts`

**Status:** Not started

---

### Task 6.2: Fix JaCoCo Exclusions
**File:** `build.gradle.kts`

**Steps:**
1. Update exclusions to only exclude what's needed
2. Remove non-existent directory exclusions (`entity/`, `dto/`)

**Status:** ✅ Applied

---

## Implementation Order

```
Phase 1 (Critical)
├── Task 1.1: Replace println with Logger
├── Task 1.2: Externalize Configuration
└── Task 1.3: Add Error Handling

Phase 2 (Core Functionality)
├── Task 2.1: Create Message DTOs
├── Task 2.2: Add Validation Dependency
├── Task 2.3: Configure Dead Letter Queue
└── Task 2.4: Add Spring Retry

Phase 3 (Testing)
├── Task 3.1: Add Unit Tests
└── Task 3.2: Add Integration Tests

Phase 4 (Observability)
├── Task 4.1: Add Spring Boot Actuator
├── Task 4.2: Add Custom Health Indicator
└── Task 4.3: Add Micrometer Metrics

Phase 5 (Resilience)
└── Task 5.1: Add Resilience4j Circuit Breaker

Phase 6 (Cleanup)
├── Task 6.1: Remove Duplicate Build Tools
└── Task 6.2: Fix JaCoCo Exclusions
```

---

## Dependencies to Add

| Dependency | Purpose | Phase |
|------------|---------|-------|
| spring-boot-starter-validation | Input validation | 2 |
| spring-retry + spring-boot-starter-aop | Retry mechanism | 2 |
| testcontainers | Integration testing | 3 |
| spring-boot-starter-actuator | Health/metrics | 4 |
| resilience4j | Circuit breaker | 5 |

---

## Files to Modify

| File | Tasks |
|------|-------|
| `src/main/kotlin/com/example/sqs_consumer/infra/SqsConsumerService.kt` | 1.1, 1.3, 2.1, 4.3, 5.1 |
| `src/main/kotlin/com/example/sqs_consumer/config/SqsConfig.kt` | 1.2, 2.3 |
| `src/main/kotlin/com/example/sqs_consumer/SqsConsumerApplication.kt` | 2.4 |
| `src/main/resources/application.yml` | 1.2, 2.3, 4.1 |
| `build.gradle.kts` | 2.2, 2.4, 3.2, 4.1, 5.1, 6.2 |

## Files to Create

| File | Tasks |
|------|-------|
| `src/main/kotlin/com/example/sqs_consumer/dto/OrderMessage.kt` | 2.1 |
| `src/main/kotlin/com/example/sqs_consumer/health/SqsHealthIndicator.kt` | 4.2 |
| `src/test/kotlin/com/example/sqs_consumer/infra/SqsConsumerServiceTest.kt` | 3.1 |
| `src/test/kotlin/com/example/sqs_consumer/integration/SqsConsumerIntegrationTest.kt` | 3.2 |
| `src/test/resources/application-test.yml` | 3.2 |
