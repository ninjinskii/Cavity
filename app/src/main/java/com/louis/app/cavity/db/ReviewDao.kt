package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Review

@Dao
interface ReviewDao {
    @Insert
    suspend fun insertReview(review: Review): Long

    @Update
    suspend fun updateReview(review: Review)

    @Delete
    suspend fun deleteReview(review: Review)

    @Query("SELECT * FROM review")
    fun getAllReviews(): LiveData<List<Review>>

    @Query("SELECT * FROM review")
    fun getAllReviewsNotLive(): List<Review>
}
