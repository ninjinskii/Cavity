package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.model.relation.GrapeWithQuantifiedGrapes
import com.louis.app.cavity.model.relation.ReviewWithFilledReviews

@Dao
interface ReviewDao {
    @Insert
    suspend fun insertReview(review: Review): Long

    @Update
    suspend fun updateReview(review: Review)

    @Delete
    suspend fun deleteReview(review: Review)

    @Query("SELECT * FROM review")
    suspend fun getAllReviewsNotLive(): List<Review>

    @Transaction
    @Query("SELECT * FROM review")
    fun getReviewWithFilledReviews(): LiveData<List<ReviewWithFilledReviews>>
}
