package com.keciput.asrifa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.keciput.asrifa.data.local.entity.SnackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SnackDao {
    @Query("SELECT * FROM snack_entity WHERE isAvailable = 1")
    fun getAllSnacks(): Flow<List<SnackEntity>>

    @Query("SELECT * FROM snack_entity WHERE id = :id")
    suspend fun getSnackById(id: Int): SnackEntity?

    @Query("""
        SELECT * FROM snack_entity 
        WHERE isAvailable = 1 
        AND (:category IS NULL OR category = :category)
        ORDER BY rating DESC
    """)
    fun getSnacksByFilter(
        category: String? = null
    ): Flow<List<SnackEntity>>

    @Query("""
        SELECT * FROM snack_entity 
        WHERE isAvailable = 1 
        AND (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
    """)
    fun searchSnacks(query: String): Flow<List<SnackEntity>>

    @Query("SELECT * FROM snack_entity WHERE rating >= :minRating ORDER BY rating DESC LIMIT :limit")
    fun getTopRatedSnacks(minRating: Double = 4.0, limit: Int = 10): Flow<List<SnackEntity>>

    @Upsert
    suspend fun upsertSnacks(snacks: List<SnackEntity>)

    @Query("DELETE FROM snack_entity")
    suspend fun deleteAllSnacks()

    @Delete
    suspend fun deleteSnack(snack: SnackEntity)
}
