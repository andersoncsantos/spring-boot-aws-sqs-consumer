# Spring Boot AWS SQS Consumer

A Spring Boot application built with Kotlin that demonstrates how to consume messages from Amazon SQS (Simple Queue Service) using Spring Cloud AWS.

## Features

- **SQS Message Consumer**: Listens to SQS queues and processes messages
- **LocalStack Integration**: Configured for local development with LocalStack
- **Spring Boot 3.3**: Built on Spring Boot framework with Kotlin
- **AWS SDK v2**: Uses the latest AWS SDK for Java v2
- **Spring Cloud AWS**: Leverages Spring Cloud AWS for seamless SQS integration

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   SQS Queue     │───▶│  SqsConsumer     │───▶│   Business      │
│  (LocalStack)   │    │    Service       │    │     Logic       │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## Prerequisites

- Java 17
- Kotlin 1.9.25
- Gradle (wrapper included)
- LocalStack (for local development)
- Docker (to run LocalStack)

## Quick Start

### 1. Start LocalStack

```bash
docker run --rm -it -p 4566:4566 -p 4571:4571 localstack/localstack
```

### 2. Create SQS Queue

```bash
aws --endpoint-url=http://localhost:4566 sqs create-queue \
    --queue-name terraform-example-queue.fifo \
    --attributes FifoQueue=true
```

### 3. Run the Application

```bash
./gradlew bootRun
```

### 4. Send Test Messages

```bash
aws --endpoint-url=http://localhost:4566 sqs send-message \
    --queue-url http://localhost:4566/000000000000/terraform-example-queue.fifo \
    --message-body "Hello from SQS!" \
    --message-group-id "test-group"
```

## Configuration

### Application Properties

Configure the SQS queue URL in `application.yml`:

```yaml
sqs:
  queue:
    url: "http://localhost:4566/000000000000/terraform-example-queue.fifo"
```

### SQS Configuration (`SqsConfig.kt`)

The application is configured to connect to LocalStack for local development:

- **Endpoint**: `http://localhost:4566`
- **Region**: `us-east-1`
- **Credentials**: Mock credentials for LocalStack

### Message Consumer (`SqsConsumerService.kt`)

The `SqsConsumerService` class contains the message listener:

```kotlin
@Service
class SqsConsumerService(
    @param:Value("\${sqs.queue.url}") private val queueUrl: String
) {

    @SqsListener("\${sqs.queue.url}")
    fun receiveMessage(message: String) {
        println("Message received: === $message ===")
    }
}
```

## Dependencies

### Core Dependencies
- `spring-boot-starter-web`: Web framework
- `kotlin-reflect` & `kotlin-stdlib`: Kotlin runtime
- `software.amazon.awssdk:sqs`: AWS SDK v2 for SQS
- `spring-cloud-aws-starter-sqs`: Spring Cloud AWS SQS integration

### Build Configuration
- **Spring Boot**: 3.3.3
- **Kotlin**: 1.9.25
- **Java**: 17
- **AWS SDK**: 2.20.79
- **Spring Cloud AWS**: 3.0.1

## Project Structure

```
src/
├── main/kotlin/com/example/sqs_consumer/
│   ├── SqsConsumerApplication.kt      # Main application class
│   ├── config/
│   │   └── SqsConfig.kt               # SQS configuration
│   └── infra/
│       └── SqsConsumerService.kt      # Message consumer service
└── test/kotlin/com/example/sqs_consumer/
    └── SqsConsumerApplicationTests.kt # Basic tests
```

## Development

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

### Creating JAR

```bash
./gradlew bootJar
```

## Production Deployment

For production deployment, update the `SqsConfig` to:

1. Remove LocalStack endpoint override
2. Use proper AWS credentials (IAM roles recommended)
3. Configure appropriate AWS region
4. Update queue URLs to production queues

## Troubleshooting

### Common Issues

1. **LocalStack not running**: Ensure LocalStack is running on port 4566
2. **Queue not found**: Create the SQS queue before starting the application
3. **Connection refused**: Check LocalStack endpoint configuration

### Logs

The application logs received messages to the console. For production, configure proper logging with Logback or Log4j2.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.
