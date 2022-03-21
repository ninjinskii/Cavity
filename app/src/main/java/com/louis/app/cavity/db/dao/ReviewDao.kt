package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.FReview
import com.louis.app.cavity.model.Review

@Dao
interface ReviewDao {
    @Insert
    suspend fun insertReview(review: Review): Long

    @Update
    suspend fun updateReview(review: Review)

    @Delete
    suspend fun deleteReview(review: Review)

    @Query("SELECT * FROM review ORDER BY contest_name")
    fun getAllReviews(): LiveData<List<Review>>

    @Query("SELECT * FROM review ORDER BY contest_name")
    suspend fun getAllReviewsNotLive(): List<Review>

    @Transaction
    @Query("SELECT * FROM review")
    fun getReviewWithFilledReviews(): LiveData<List<ReviewWithFReviews>>

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
