package com.example.sqs_consumer

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class SQSController {

    @Autowired
    private lateinit var service: SqsConsumerService

    @GetMapping("/consume")
    fun consumeMessage() {
        service.receiveMessage()
    }
}