package com.louis.app.cavity.db.dao

import androidx.room.*
import com.louis.app.cavity.model.FReview
import com.louis.app.cavity.model.Review
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Insert
    suspend fun insertReview(review: Review): Long

    @Insert
    suspend fun insertReviews(reviews: List<Review>)

    @Update
    suspend fun updateReview(review: Review)

    @Delete
    suspend fun deleteReview(review: Review)

    @Query("SELECT * FROM review ORDER BY contest_name")
    fun getAllReviews(): Flow<List<Review>>

    @Query("SELECT * FROM review ORDER BY contest_name")
    suspend fun getAllReviewsNotLive(): List<Review>

    @Transaction
    @Query("SELECT * FROM review")
    fun getReviewWithFilledReviews(): Flow<List<ReviewWithFReviews>>

    @Query("DELETE FROM review")
    suspend fun deleteAll()
}

data class ReviewWithFReviews(
    @Embedded val review: Review,
    @Relation(
        parentColumn = "id",
        entityColumn = "review_id"
    )
    val fReview: List<FReview>
)
