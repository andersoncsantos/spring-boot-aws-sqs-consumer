# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.3 application built with Kotlin that consumes messages from Amazon SQS using Spring Cloud AWS. The application is configured for local development with LocalStack.

## Common Commands

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run the application
./gradlew bootRun

# Create JAR file
./gradlew bootJar

# Generate JaCoCo test coverage report
./gradlew jacocoTestReport

# Run a single test class
./gradlew test --tests "com.example.sqs_consumer.SqsConsumerApplicationTests"
```

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   SQS Queue     │───▶│  SqsConsumer     │───▶│   Business      │
│  (LocalStack)   │    │    Service       │    │     Logic       │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### Key Components

- **SqsConsumerApplication.kt** - Main Spring Boot entry point
- **config/SqsConfig.kt** - Configures SqsAsyncClient for LocalStack (endpoint: `http://localhost:4566`) and SqsTemplate bean
- **infra/SqsConsumerService.kt** - Message listener using `@SqsListener` annotation
- **application.yml** - Queue URL configuration (`sqs.queue.url`) and server port (8085)

### Dependencies

- Spring Boot 3.3.3
- Kotlin 1.9.25
- Java 17
- AWS SDK v2 (2.20.79)
- Spring Cloud AWS 3.0.1

## Local Development

Start LocalStack via docker-compose:
```bash
docker-compose up -d
```

Create the FIFO queue before running the app:
```bash
aws --endpoint-url=http://localhost:4566 sqs create-queue \
    --queue-name terraform-example-queue.fifo \
    --attributes FifoQueue=true
```

Send test messages:
```bash
aws --endpoint-url=http://localhost:4566 sqs send-message \
    --queue-url http://localhost:4566/000000000000/terraform-example-queue.fifo \
    --message-body "Hello from SQS!" \
    --message-group-id "test-group"
```

## Production Notes

When deploying to AWS:
1. Remove LocalStack endpoint override in `SqsConfig.kt`
2. Use IAM role-based credentials instead of static credentials
3. Configure proper AWS region
4. Update queue URLs in `application.yml`
