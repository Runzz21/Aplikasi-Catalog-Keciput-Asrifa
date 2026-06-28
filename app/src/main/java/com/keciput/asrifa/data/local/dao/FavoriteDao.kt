package com.keciput.asrifa.data.local.dao

import androidx.room.*
import com.keciput.asrifa.data.local.entity.FavoriteEntity
import com.keciput.asrifa.data.local.entity.SnackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Transaction
    @Query("""SELECT s.* FROM snack_entity s INNER JOIN favorite_entity f ON s.id = f.snackId ORDER BY f.savedAt DESC""")
    fun getFavoriteSnacks(): Flow<List<SnackEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_entity WHERE snackId = :snackId)")
    fun isFavorite(snackId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorite_entity WHERE snackId = :snackId")
    suspend fun removeFavorite(snackId: String)

    @Query("SELECT COUNT(*) FROM favorite_entity")
    fun getFavoriteCount(): Flow<Int>
}
