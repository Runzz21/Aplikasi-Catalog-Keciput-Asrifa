package com.keciput.asrifa.data.repository

import com.keciput.asrifa.data.local.dao.ReviewDao
import com.keciput.asrifa.data.local.entity.ReviewEntity
import com.keciput.asrifa.domain.model.Review
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val reviewDao: ReviewDao
) : ReviewRepository {

    override fun getReviewsForSnack(snackId: Int): Flow<List<Review>> {
        return reviewDao.getReviewsBySnackId(snackId).map { entities ->
            entities.map { it.asDomainModel() }
        }
    }

    override suspend fun addReviews(reviews: List<Review>) {
        val entities = reviews.map { 
            ReviewEntity(
                id = it.id,
                snackId = it.snackId,
                userName = it.userName,
                rating = it.rating,
                comment = it.comment,
                reviewDate = it.reviewDate
            )
        }
        reviewDao.upsertReviews(entities)
    }
}
