# Clean Code Recommendations for SQS Consumer Project

Applying Clean Code principles to your project will enhance its readability, maintainability, and overall quality. Here are some specific recommendations based on the current project structure:

### 1. Externalize Configuration (Avoid Magic Strings)

A core principle of Clean Code is to avoid hardcoding configuration values, often called "magic strings" or "magic numbers". Your `SqsConfig.kt` and `SqsConsumerService.kt` files have several hardcoded values. Moving these to your `application.yml` file makes your application more flexible and easier to configure for different environments (like local, development, and production).

**Recommendation:**

Move the SQS endpoint, queue name, and credentials from your Kotlin files into `application.yml`.

**`src/main/resources/application.yml`:**

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

Then, you can use Spring Boot's `@Value` annotation or configuration properties to inject these values into your code.

**`src/main/kotlin/com/example/sqs_consumer/config/SqsConfig.kt`:**

```kotlin
package com.example.sqs_consumer.config

import io.awspring.cloud.sqs.operations.SqsTemplate
import java.net.URI
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient

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
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .region(Region.of(region))
            .build()
    }

    @Bean
    fun sqsTemplate(sqsAsyncClient: SqsAsyncClient): SqsTemplate {
        return SqsTemplate.builder().sqsAsyncClient(sqsAsyncClient).build()
    }
}
```

**`src/main/kotlin/com/example/sqs_consumer/infra/SqsConsumerService.kt`:**

```kotlin
package com.example.sqs_consumer.infra

import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SqsConsumerService {

    private val logger = LoggerFactory.getLogger(javaClass)

    @SqsListener("\${aws.sqs.queue-name}")
    fun receiveMessage(message: String) {
        logger.info("Message received: === {} ===", message)
    }
}
```

### 2. Use a Logger, Not `println`

Using a dedicated logging framework like SLF4J (which is included with Spring Boot) is preferable to `System.out.println()`. Loggers provide configurable log levels (INFO, DEBUG, ERROR, etc.), structured output, and the ability to direct logs to different places (console, files, etc.).

This change is included in the `SqsConsumerService` example above.

### 3. Write Meaningful Tests

Your `SqsConsumerApplicationTests.kt` has a `contextLoads()` test, which is a good first step to ensure your Spring application context can start. However, to follow Clean Code principles, you should also have tests that verify the behavior of your application.

**Recommendation:**

Create a test that sends a message to your SQS queue and verifies that your `SqsConsumerService` receives and processes it correctly. You can use libraries like Testcontainers with LocalStack to create an integration test that runs against a real (but local) SQS queue.

This approach ensures that your consumer works as expected and helps prevent regressions as you add more features.
