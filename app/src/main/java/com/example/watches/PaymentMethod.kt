package com.example.watches

data class PaymentMethod(
    val id: String = "",
    val cardHolderName: String = "",
    val cardNumber: String = "",
    val expiryDate: String = "",
    val cardType: String = "Visa", // Visa, Mastercard, etc.
    @field:JvmField
    val isDefault: Boolean = false
)
