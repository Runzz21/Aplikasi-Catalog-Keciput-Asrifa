package com.keciput.asrifa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.keciput.asrifa.data.local.entity.StoreInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreInfoDao {
    @Query("SELECT * FROM store_info WHERE id = 1")
    fun getStoreInfo(): Flow<StoreInfoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoreInfo(storeInfo: StoreInfoEntity)
}
