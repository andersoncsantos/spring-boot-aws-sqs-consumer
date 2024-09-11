package com.example.sqs_consumer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SqsConsumerApplication

fun main(args: Array<String>) {
	runApplication<SqsConsumerApplication>(*args)
}
