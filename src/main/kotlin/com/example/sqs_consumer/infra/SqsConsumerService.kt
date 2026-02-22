package com.example.sqs_consumer.infra

import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

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