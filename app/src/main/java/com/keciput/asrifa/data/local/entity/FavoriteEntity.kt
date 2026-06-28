package com.keciput.asrifa.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorite_entity",
    foreignKeys = [ForeignKey(
        entity = SnackEntity::class,
        parentColumns = ["id"],
        childColumns = ["snackId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class FavoriteEntity(
    @PrimaryKey val snackId: String,
    val savedAt: Long = System.currentTimeMillis()
)
