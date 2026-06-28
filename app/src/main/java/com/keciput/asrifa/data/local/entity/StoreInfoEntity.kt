package com.keciput.asrifa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.keciput.asrifa.domain.model.StoreInfo

@Entity(tableName = "store_info")
data class StoreInfoEntity(
    @PrimaryKey val id: Int = 1, // Only one store info record
    val openTime: String,
    val closeTime: String,
    val operatingDays: String, // Comma separated integers
    val holidayDates: String, // Comma separated strings
    val holidayMessage: String,
    val isNotificationEnabled: Boolean
) {
    fun asDomainModel(): StoreInfo {
        return StoreInfo(
            openTime = openTime,
            closeTime = closeTime,
            operatingDays = operatingDays.split(",").filter { it.isNotEmpty() }.map { it.toInt() },
            holidayDates = holidayDates.split(",").filter { it.isNotEmpty() },
            holidayMessage = holidayMessage,
            isNotificationEnabled = isNotificationEnabled
        )
    }
}

fun StoreInfo.asEntity(): StoreInfoEntity {
    return StoreInfoEntity(
        openTime = openTime,
        closeTime = closeTime,
        operatingDays = operatingDays.joinToString(","),
        holidayDates = holidayDates.joinToString(","),
        holidayMessage = holidayMessage,
        isNotificationEnabled = isNotificationEnabled
    )
}
