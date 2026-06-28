package com.keciput.asrifa.data

data class Snack(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val weight: String = "",
    val expired: String = "",
    val imageUrl: String = "",
    val isAvailable: Boolean = true
)
