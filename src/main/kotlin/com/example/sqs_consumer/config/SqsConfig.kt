package com.example.sqs_consumer.config

import io.awspring.cloud.sqs.operations.SqsTemplate
import java.net.URI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient

@Configuration
class SqsConfig {

    @Bean
    fun sqsAsyncClient(): SqsAsyncClient {
        val localstackUrl = "http://localhost:4566"

        return SqsAsyncClient.builder()
            .endpointOverride(URI.create(localstackUrl))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("mock_access_key", "mock_access_key")))
            .region(Region.US_EAST_1) // LocalStack default region
            .build()
    }

    @Bean
    fun sqsTemplate(sqsAsyncClient: SqsAsyncClient): SqsTemplate {
        return SqsTemplate.builder().sqsAsyncClient(sqsAsyncClient).build()
    }
}