package com.keciput.asrifa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.keciput.asrifa.domain.model.PackagingType

@Entity(tableName = "cart_entity")
data class CartEntity(
    @PrimaryKey val id: String,
    val snackId: Int,
    val snackName: String,
    val imageUrl: String,
    val selectedVariant: String? = null,
    val packagingType: PackagingType = PackagingType.ECERAN,
    val quantity: Int,
    val pricePerUnit: Double,
    val addedAt: Long = System.currentTimeMillis()
)
