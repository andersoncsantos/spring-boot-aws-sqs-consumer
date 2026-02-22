package com.example.sqs_consumer.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderMessage(
    @NotBlank
    val orderId: String,

    @NotBlank
    val customerId: String,

    @Positive
    val amount: Double,

    @NotBlank
    val timestamp: String
)
