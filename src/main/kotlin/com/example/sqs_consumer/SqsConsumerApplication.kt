package com.example.sqs_consumer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SqsConsumerApplication

fun main(args: Array<String>) {
	runApplication<SqsConsumerApplication>(*args)
}
