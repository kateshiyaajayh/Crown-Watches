package com.example.watches

data class Address(
    val id: String = "",
    val fullName: String = "",
    val mobileNumber: String = "",
    val addressLine: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val addressType: String = "Home", // Home, Office, etc.
    @field:JvmField
    val isDefault: Boolean = false
)
