package com.keciput.asrifa.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.keciput.asrifa.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM review_entity WHERE snackId = :snackId ORDER BY reviewDate DESC")
    fun getReviewsBySnackId(snackId: Int): Flow<List<ReviewEntity>>

    @Upsert
    suspend fun upsertReviews(reviews: List<ReviewEntity>)
}
