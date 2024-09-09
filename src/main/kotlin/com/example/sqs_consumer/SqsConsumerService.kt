package com.example.sqs_consumer

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.Message
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class SqsConsumerService(private val amazonSQS: AmazonSQSAsync) {

    @Value("\${sqs.queue.url}")
    private lateinit var queueUrl: String

    @Scheduled(fixedRate = 5000)
    @EventListener(ApplicationReadyEvent::class)
    fun receiveMessage() {
        // Receive messages from the queue
        val messages: List<Message> = amazonSQS.receiveMessage(queueUrl).messages

        for (message in messages) {
            println("Received message: ${message.body}")
            amazonSQS.deleteMessage(queueUrl, message.receiptHandle)
        }
    }
}