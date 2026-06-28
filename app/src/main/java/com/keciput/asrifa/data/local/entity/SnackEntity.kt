package com.keciput.asrifa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snack_entity")
data class SnackEntity(
    @PrimaryKey val id: Int,
    val name: String?,
    val description: String?,
    val category: String?,
    val categoryId: String?,
    val price: Double,
    val originalPrice: Double? = null,
    val rating: Double,
    val reviewCount: Int,
    val weightDefault: String?,
    val imageUrl: String?,
    val variants: String?, // Disimpan sebagai string yang dipisahkan koma
    val hasBulkPackaging: Boolean = false,
    val isAvailable: Boolean = true,
    val isBestseller: Boolean = false,
    val isNew: Boolean = false,
    val isHot: Boolean = false,
    val isFeatured: Boolean = false,
    val isFlashSale: Boolean = false,
    val soldCount: Int = 0
)
