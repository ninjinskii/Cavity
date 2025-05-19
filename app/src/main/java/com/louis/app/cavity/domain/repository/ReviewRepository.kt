package com.louis.app.cavity.domain.repository

import android.app.Application
import com.louis.app.cavity.db.CavityDatabase
import com.louis.app.cavity.domain.error.ErrorReporter
import com.louis.app.cavity.domain.error.SentryErrorReporter
import com.louis.app.cavity.model.FReview
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.domain.repository.RepositoryUpsertResult.*
import com.louis.app.cavity.domain.repository.RepositoryUpsertResult.Companion.handleDatabaseError

class ReviewRepository private constructor(app: Application) {
    companion object {
        @Volatile
        var instance: ReviewRepository? = null

        fun getInstance(app: Application) =
            instance ?: synchronized(this) {
                instance ?: ReviewRepository(app).also { instance = it }
            }
    }

    private val errorReporter: ErrorReporter = SentryErrorReporter.getInstance(app)
    private val database = CavityDatabase.getInstance(app)
    private val reviewDao = database.reviewDao()
    private val fReviewDao = database.fReviewDao()

    suspend fun insertReview(review: Review): RepositoryUpsertResult<Long> {
        if (!review.hasValidName()) {
            return Failure
        }

        try {
            val reviewId = reviewDao.insertReview(review)
            return Success(reviewId)
        } catch (e: Exception) {
            return handleDatabaseError(e, errorReporter)
        }
    }

    suspend fun insertReviews(reviews: List<Review>) = reviewDao.insertReviews(reviews)

    suspend fun updateReview(review: Review): RepositoryUpsertResult<Long> {
        if (!review.hasValidName()) {
            return Failure
        }

        try {
            reviewDao.updateReview(review)
            return Success(review.id)
        } catch (e: Exception) {
            return handleDatabaseError(e, errorReporter)
        }
    }

    suspend fun deleteReview(review: Review) = reviewDao.deleteReview(review)
    fun getAllReviews() = reviewDao.getAllReviews()
    suspend fun getAllReviewsNotLive() = reviewDao.getAllReviewsNotLive()
    fun getReviewWithFilledReviews() = reviewDao.getReviewWithFilledReviews()
    suspend fun getAllFReviewsNotLive() = fReviewDao.getAllFReviewsNotLive()
    suspend fun insertFilledReviews(fReviews: List<FReview>) = fReviewDao.insertFReviews(fReviews)

    fun getFReviewAndReviewForBottle(bottleId: Long) =
        fReviewDao.getFReviewAndReviewForBottle(bottleId)

    suspend fun getFReviewAndReviewForBottleNotLive(bottleId: Long) =
        fReviewDao.getFReviewAndReviewForBottleNotLive(bottleId)

    suspend fun replaceFReviewsForBottle(bottleId: Long, fReviews: List<FReview>) {
        if (!database.inTransaction()) {
            throw IllegalStateException("This method should be called inside a transaction")
        }

        fReviewDao.clearAllFReviewsForBottle(bottleId)
        fReviewDao.insertFReviews(fReviews)
    }

    suspend fun deleteAllReviews() = reviewDao.deleteAll()
    suspend fun deleteAllFReviews() = fReviewDao.deleteAll()
}
