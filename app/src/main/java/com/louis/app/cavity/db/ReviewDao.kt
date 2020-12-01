package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.louis.app.cavity.model.Review

interface ReviewDao {
    @Insert
    suspend fun insertReview(review: Review)

    @Update
    suspend fun updateReview(review: Review)

    @Delete
    suspend fun deleteReview(review: Review)

    @Query("SELECT * FROM review")
    fun getAllReviews(): LiveData<List<Review>>
}
