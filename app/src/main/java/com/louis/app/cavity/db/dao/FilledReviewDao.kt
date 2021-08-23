package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.FReview
import com.louis.app.cavity.model.Review

@Dao
interface FilledReviewDao {
    @Insert
    suspend fun insertFReview(fReview: FReview)

    @Insert
    suspend fun insertFReviews(fReview: List<FReview>)

    @Update
    suspend fun updateFReview(fReview: FReview)

    @Delete
    suspend fun deleteFReview(fReview: FReview)

    @Transaction
    @Query("SELECT * FROM f_review WHERE bottle_id=:bottleId")
    suspend fun getFReviewsForBottleNotLive(bottleId: Long): List<FReview>

    @Transaction
    @Query("SELECT * FROM f_review WHERE bottle_id=:bottleId")
    fun getFReviewAndReviewForBottle(bottleId: Long): LiveData<List<FReviewAndReview>>

    @Transaction
    @Query("SELECT * FROM f_review WHERE bottle_id=:bottleId")
    suspend fun getFReviewAndReviewForBottleNotLive(bottleId: Long): List<FReviewAndReview>

    @Query("DELETE FROM f_review WHERE bottle_id=:bottleId AND review_id=:reviewId")
    suspend fun deleteFReviewByPk(bottleId: Long, reviewId: Long)

    @Query("DELETE FROM f_review WHERE bottle_id=:bottleId")
    suspend fun clearAllFReviewsForBottle(bottleId: Long)

    @Query("DELETE FROM f_review")
    suspend fun deleteAll()
}

data class FReviewAndReview(
    @Embedded val fReview: FReview,
    @Relation(
        parentColumn = "review_id",
        entityColumn = "id",
    )
    val review: Review
)

