# SQS Consumer - Detailed Documentation

## Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Technology Stack](#technology-stack)
4. [Project Structure](#project-structure)
5. [Prerequisites](#prerequisites)
6. [Installation & Setup](#installation--setup)
7. [Configuration](#configuration)
8. [Code Reference](#code-reference)
9. [Running the Application](#running-the-application)
10. [Testing](#testing)
11. [Building for Production](#building-for-production)
12. [Deployment to AWS](#deployment-to-aws)
13. [Troubleshooting](#troubleshooting)

---

## Project Overview

This is a Spring Boot 3.3 application built with Kotlin that consumes messages from Amazon SQS (Simple Queue Service) using Spring Cloud AWS. The application demonstrates a complete integration pattern for receiving and processing messages from an SQS queue.

### Key Features

- **SQS Message Consumer**: Listens to SQS queues using the `@SqsListener` annotation
- **LocalStack Integration**: Pre-configured for local development with LocalStack
- **FIFO Queue Support**: Handles FIFO queues with message group IDs
- **Spring Boot 3.3**: Modern Spring Boot framework with Kotlin
- **AWS SDK v2**: Uses the latest AWS SDK for Java
- **Test Coverage**: Includes JaCoCo for code coverage reporting

---

## Architecture

The application follows a simple yet scalable architecture:

```
┌─────────────────────────────────────────────────────────────────────┐
│                         SQS Consumer Application                     │
│                                                                      │
│  ┌──────────────┐     ┌──────────────────┐     ┌──────────────────┐  │
│  │   SQS Queue  │────▶│  SqsConsumer     │────▶│   Business       │  │
│  │  (LocalStack │     │    Service       │     │   Logic          │  │
│  │   or AWS)    │     │                  │     │   (process)      │  │
│  └──────────────┘     └──────────────────┘     └──────────────────┘  │
│                              │                                        │
│                              ▼                                        │
│                     ┌──────────────┐                                 │
│                     │   Console    │                                 │
│                     │   Output     │                                 │
│                     └──────────────┘                                 │
└─────────────────────────────────────────────────────────────────────┘
```

### Message Flow

1. Messages are sent to an SQS queue (FIFO or standard)
2. The `SqsConsumerService` listens to the queue using `@SqsListener`
3. When a message arrives, the `receiveMessage()` method is invoked
4. The business logic processes the message
5. The message is automatically acknowledged/deleted from the queue

---

## Technology Stack

| Component | Version |
|-----------|---------|
| Spring Boot | 3.3.3 |
| Kotlin | 1.9.25 |
| Java | 17 |
| AWS SDK v2 | 2.20.79 |
| Spring Cloud AWS | 3.0.1 |
| JaCoCo | 0.8.11 |
| Gradle | 8.10 |

### Dependencies

#### Core Dependencies
- `org.springframework.boot:spring-boot-starter-web` - Web framework
- `org.jetbrains.kotlin:kotlin-reflect` - Kotlin reflection
- `org.jetbrains.kotlin:kotlin-stdlib` - Kotlin standard library
- `spring-boot-starter-validation` - Input validation

#### AWS Dependencies
- `software.amazon.awssdk:sqs` - AWS SDK v2 for SQS
- `io.awspring.cloud:spring-cloud-aws-starter-sqs` - Spring Cloud AWS SQS integration

#### Testing Dependencies
- `org.springframework.boot:spring-boot-starter-test` - Spring Boot testing

---

## Project Structure

```
sqs-consumer/
├── src/
│   ├── main/
│   │   ├── kotlin/com/example/sqs_consumer/
│   │   │   ├── SqsConsumerApplication.kt    # Main application entry point
│   │   │   ├── config/
│   │   │   │   └── SqsConfig.kt            # AWS/SQS client configuration
│   │   │   ├── dto/
│   │   │   │   └── OrderMessage.kt         # Message DTO
│   │   │   └── infra/
│   │   │       └── SqsConsumerService.kt   # SQS message listener
│   │   └── resources/
│   │       └── application.yml             # Application configuration
│   └── test/
│       └── kotlin/com/example/sqs_consumer/
│           └── SqsConsumerApplicationTests.kt # Unit tests
├── build.gradle.kts                        # Gradle build configuration
├── settings.gradle.kts                     # Gradle settings
├── docker-compose.yml                      # LocalStack container definition
├── scripts/                                # Shell scripts
│   ├── create-sqs-queue.sh                # Queue creation script
│   └── publish-message.sh                  # Message publishing script
├── gradle/                                 # Gradle wrapper
├── .gradle/                                # Gradle cache
├── build/                                  # Build output
├── README.md                               # Quick start guide
├── CLAUDE.md                               # AI assistant guidance
├── DOC.md                                  # This documentation
├── IMPROVEMENTS.md                         # Improvement recommendations
└── IMPLEMENTATION_PLAN.md                 # Implementation plan
```

---

## Prerequisites

### Software Requirements

1. **Java 17** or higher
   ```bash
   java -version  # Should show Java 17+
   ```

2. **Gradle 8.10** (wrapper included)
   ```bash
   ./gradlew -v
   ```

3. **Docker** (for LocalStack)
   ```bash
   docker --version
   ```

4. **AWS CLI** (optional, for manual testing)
   ```bash
   aws --version
   ```

### Optional Tools

- **IntelliJ IDEA** or **VS Code** with Kotlin support
- **Postman** or **curl** for HTTP requests

---

## Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd sqs-consumer
```

### 2. Start LocalStack

Using Docker Compose (recommended):

```bash
docker-compose up -d
```

Or manually:

```bash
docker run --rm -it \
  -p 4566:4566 \
  -p 4571:4571 \
  -e SERVICES=sqs \
  localstack/localstack
```

### 3. Create the SQS Queue

Run the provided script:

```bash
./scripts/create-sqs-queue.sh
```

Or manually:

```bash
aws --endpoint-url=http://localhost:4566 sqs create-queue \
    --queue-name terraform-example-queue.fifo \
    --attributes FifoQueue=true
```

Expected output:
```json
{
    "QueueUrl": "http://localhost:4566/000000000000/terraform-example-queue.fifo"
}
```

---

## Configuration

### Application Properties (`src/main/resources/application.yml`)

```yaml
server:
  port: 8085

sqs:
  queue:
    url: "http://localhost:4566/000000000000/terraform-example-queue.fifo"

aws:
  sqs:
    endpoint: "http://localhost:4566"
    queue-name: "terraform-example-queue.fifo"
    region: "us-east-1"
    credentials:
      access-key: "mock_access_key"
      secret-key: "mock_secret_key"

spring:
  cloud:
    aws:
      sqs:
        listener:
          acknowledgment-mode: MANUAL
```

#### Configuration Properties

| Property | Description | Default |
|----------|-------------|---------|
| `server.port` | HTTP server port | 8085 |
| `sqs.queue.url` | SQS queue URL | Required |
| `aws.sqs.endpoint` | SQS endpoint URL | Required for LocalStack |
| `aws.sqs.region` | AWS region | us-east-1 |
| `aws.sqs.credentials.access-key` | AWS access key | - |
| `aws.sqs.credentials.secret-key` | AWS secret key | - |

### SQS Configuration (`SqsConfig.kt`)

The `SqsConfig` class configures the SQS client for LocalStack:

```kotlin
@Configuration
class SqsConfig {

    @Value("\${sqs.queue.url}")
    private val queueUrl: String = ""

    @Bean
    fun sqsAsyncClient(): SqsAsyncClient {
        return SqsAsyncClient.builder()
            .endpointOverride(URI.create("http://localhost:4566"))
            .region(Region.US_EAST_1)
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create("test", "test")
            ))
            .build()
    }

    @Bean
    fun sqsTemplate(sqsAsyncClient: SqsAsyncClient): SqsTemplate {
        return SqsTemplate.builder()
            .sqsAsyncClient(sqsAsyncClient)
            .build()
    }
}
```

#### Key Configuration Details

- **Endpoint**: `http://localhost:4566` (LocalStack)
- **Region**: `us-east-1`
- **Credentials**: Static mock credentials (`test`/`test`) for LocalStack
- **SqsTemplate**: Provides high-level operations for SQS

---

## Code Reference

### Main Application (`SqsConsumerApplication.kt`)

```kotlin
@SpringBootApplication
class SqsConsumerApplication

fun main(args: Array<String>) {
    SpringApplication.run(SqsConsumerApplication::class.java, *args)
}
```

This is the entry point for the Spring Boot application.

### SQS Consumer Service (`SqsConsumerService.kt`)

```kotlin
@Service
class SqsConsumerService {

    private val logger = LoggerFactory.getLogger(SqsConsumerService::class.java)

    @SqsListener("\${sqs.queue.url}")
    fun receiveMessage(message: String) {
        try {
            logger.info("Message received: === {} ===", message)
            processMessage(message)
            logger.info("Message processed successfully")
        } catch (e: Exception) {
            logger.error("Failed to process message: {}", message, e)
            throw e
        }
    }

    private fun processMessage(message: String) {
        logger.debug("Processing message: {}", message)
    }
}
```

#### Key Features

- **`@Service`**: Marks the class as a Spring service component
- **`@SqsListener`**: Automatically listens to the specified queue
- **`message: String`**: The message body received from the queue
- **Error Handling**: Try-catch with logging and re-throw for retry/DLQ

### Configuration Class (`SqsConfig.kt`)

```kotlin
@Configuration
class SqsConfig {
    // Bean definitions for SQS client and template
}
```

Provides:
- `SqsAsyncClient` - Low-level SQS operations
- `SqsTemplate` - High-level SQS operations

---

## Running the Application

### Development Mode

```bash
./gradlew bootRun
```

The application will start on port 8085.

### Sending Test Messages

Using the provided script:

```bash
./scripts/publish-message.sh "Hello from SQS!"
```

Or manually with AWS CLI:

```bash
aws --endpoint-url=http://localhost:4566 sqs send-message \
    --queue-url http://localhost:4566/000000000000/terraform-example-queue.fifo \
    --message-body "Hello from SQS!" \
    --message-group-id "test-group"
```

### Verifying Consumption

Check the application console output. You should see:

```
Message received: === Hello from SQS! ===
```

---

## Testing

### Running All Tests

```bash
./gradlew test
```

### Running a Specific Test Class

```bash
./gradlew test --tests "com.example.sqs_consumer.SqsConsumerApplicationTests"
```

### Generating Test Coverage Report

```bash
./gradlew jacocoTestReport
```

The report will be generated at:
- HTML: `build/reports/jacoco/test/html/index.html`
- XML: `build/reports/jacoco/test/jacocoTestReport.xml`

### Test Coverage Configuration

The JaCoCo configuration excludes certain packages from coverage:

```kotlin
exclude(
    "**/config/**",
    "**/entity/**",
    "**/dto/**"
)
```

---

## Building for Production

### Build JAR File

```bash
./gradlew bootJar
```

The JAR will be created at: `build/libs/sqs-consumer-0.0.1-SNAPSHOT.jar`

### Build with Tests

```bash
./gradlew build
```

### Clean Build

```bash
./gradlew clean build
```

---

## Deployment to AWS

### Pre-deployment Checklist

1. [ ] Update SQS configuration for AWS
2. [ ] Configure proper IAM credentials/role
3. [ ] Set correct AWS region
4. [ ] Update queue URLs
5. [ ] Configure logging

### Configuration Changes Required

#### 1. Update `SqsConfig.kt`

Remove LocalStack endpoint override:

```kotlin
@Bean
fun sqsAsyncClient(): SqsAsyncClient {
    return SqsAsyncClient.builder()
        .region(Region.US_EAST_1)  // Set your desired region
        .build()
}
```

#### 2. Update `application.yml`

```yaml
sqs:
  queue:
    url: "https://sqs.us-east-1.amazonaws.com/ACCOUNT_ID/QUEUE_NAME.fifo"
```

#### 3. IAM Permissions

Ensure the application has the following permissions:
- `sqs:ReceiveMessage`
- `sqs:DeleteMessage`
- `sqs:GetQueueUrl`

Example IAM policy:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "sqs:ReceiveMessage",
                "sqs:DeleteMessage",
                "sqs:GetQueueUrl"
            ],
            "Resource": "arn:aws:sqs:us-east-1:ACCOUNT_ID:QUEUE_NAME"
        }
    ]
}
```

### Deployment Options

#### Option 1: EC2 Instance

1. Build the JAR
2. Copy to EC2 instance
3. Run with: `java -jar sqs-consumer-0.0.1-SNAPSHOT.jar`

#### Option 2: ECS/EKS

1. Create Dockerfile
2. Push to ECR
3. Deploy as container

#### Option 3: Lambda

This application can be adapted to run as a Lambda function using Spring Cloud Function.

---

## Troubleshooting

### Common Issues

#### 1. LocalStack Not Running

**Error**: `ConnectException: Connection refused`

**Solution**: Start LocalStack
```bash
docker-compose up -d
```

#### 2. Queue Not Found

**Error**: `QueueDoesNotExist`

**Solution**: Create the queue
```bash
./scripts/create-sqs-queue.sh
```

#### 3. Invalid Credentials

**Error**: `InvalidClientTokenId`

**Solution**: Check AWS credentials configuration

#### 4. Port Already in Use

**Error**: `Port 8085 is already in use`

**Solution**: Change port in `application.yml`
```yaml
server:
  port: 8086
```

### Debugging Tips

1. **Enable AWS Debug Logging**:
   ```kotlin
   System.setProperty("software.amazon.awssdk.logging", "true")
   ```

2. **Check LocalStack Logs**:
   ```bash
   docker logs localstack
   ```

3. **Verify Queue Configuration**:
   ```bash
   aws --endpoint-url=http://localhost:4566 sqs get-queue-attributes \
       --queue-url <YOUR_QUEUE_URL> \
       --attribute-names All
   ```

### Health Checks

- LocalStack Health: `http://localhost:4566/_localstack/health`
- Application: `http://localhost:8085/actuator/health` (if actuator is enabled)

---

## Additional Resources

- [Spring Cloud AWS Documentation](https://docs.awspring.io/spring-cloud-aws/docs/3.0.1/reference/html/)
- [AWS SDK for Java v2](https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/welcome.html)
- [Amazon SQS Developer Guide](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/welcome.html)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)

---

## License

This project is licensed under the MIT License.

---

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request
