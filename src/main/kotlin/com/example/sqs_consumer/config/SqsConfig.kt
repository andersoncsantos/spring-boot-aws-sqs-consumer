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
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            ))
            .region(Region.of(region))
            .build()
    }

    @Bean
    fun sqsTemplate(sqsAsyncClient: SqsAsyncClient): SqsTemplate {
        return SqsTemplate.builder().sqsAsyncClient(sqsAsyncClient).build()
    }
}