package com.example.watches

data class BannerItem(
    val id: Int,
    val type: BannerType,
    val resourceUri: String, // For drawable or raw resource
    val title: String? = null,
    val description: String? = null
)

enum class BannerType {
    IMAGE, VIDEO
}
