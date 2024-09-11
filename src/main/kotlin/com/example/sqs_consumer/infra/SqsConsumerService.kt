package com.example.sqs_consumer.infra

import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.stereotype.Service

@Service
class SqsConsumerService {

    @SqsListener("http://localhost:4566/000000000000/terraform-example-queue.fifo")
    fun receiveMessage(message: String) {
        println("Message received: === $message ===")
    }
}