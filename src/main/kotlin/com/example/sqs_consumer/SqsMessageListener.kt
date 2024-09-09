package com.example.sqs_consumer

import org.slf4j.LoggerFactory
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class SqsMessageListener {
    private val logger = LoggerFactory.getLogger(SqsMessageListener::class.java)

    @SqsListener("terraform-example-queue.fifo")
    fun receiveMessage(message: String) {
        logger.info("Received message: $message")
        // Implement your business logic here
    }
}