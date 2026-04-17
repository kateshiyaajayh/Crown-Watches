package com.example.watches

data class Product(
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val description: String = "",
    var quantity: Int = 1,
    var isWishlisted: Boolean = false
)
