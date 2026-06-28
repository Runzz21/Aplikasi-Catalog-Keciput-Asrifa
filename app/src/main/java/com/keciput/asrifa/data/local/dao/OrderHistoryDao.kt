package com.keciput.asrifa.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.keciput.asrifa.data.local.entity.OrderHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderHistoryDao {
    @Query("SELECT * FROM order_history_entity WHERE userId = :userId ORDER BY orderDate DESC")
    fun getOrderHistoryByUser(userId: String): Flow<List<OrderHistoryEntity>>

    @Upsert
    suspend fun insertOrderHistory(order: OrderHistoryEntity)
}
