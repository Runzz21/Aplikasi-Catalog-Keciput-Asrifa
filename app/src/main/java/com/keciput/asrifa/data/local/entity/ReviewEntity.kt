package com.keciput.asrifa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review_entity")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val snackId: Int,
    val userName: String,
    val rating: Int,
    val comment: String,
    val reviewDate: Long
)
