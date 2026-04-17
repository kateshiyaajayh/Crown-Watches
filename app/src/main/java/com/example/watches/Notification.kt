package com.example.watches

data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val type: String = "Order" // Order, Promo, System
)
