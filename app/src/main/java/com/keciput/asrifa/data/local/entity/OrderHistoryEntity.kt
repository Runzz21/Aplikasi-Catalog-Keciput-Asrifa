package com.keciput.asrifa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_history_entity")
data class OrderHistoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val orderDate: Long,
    val itemsJson: String,
    val totalPrice: Double,
    val note: String,
    val itemCount: Int
)
