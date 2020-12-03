package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.relation.FilledBottleReviewXRef
import com.louis.app.cavity.model.relation.FilledReviewAndReview

@Dao
interface FilledReviewDao {

    @Insert
    suspend fun insertFilledReview(fReview: FilledBottleReviewXRef)

    @Update
    suspend fun updateFilledReview(fReview: FilledBottleReviewXRef)

    @Delete
    suspend fun deleteFilledReview(fReview: FilledBottleReviewXRef)

    @Query("DELETE FROM f_review WHERE bottle_id=:bottleId")
    suspend fun deleteFReviewForBottle(bottleId: Long)

    @Transaction
    @Query("SELECT * FROM f_review WHERE bottle_id=:bottleId")
    suspend fun getFReviewForBottleNotLive(bottleId: Long): List<FilledBottleReviewXRef>

    @Transaction
    @Query("SELECT * FROM f_review WHERE bottle_id=:bottleId AND review_id=:reviewId")
    suspend fun getFReview(bottleId: Long, reviewId: Long): FilledBottleReviewXRef

    @Transaction
    @Query("SELECT * FROM f_review WHERE bottle_id=:bottleId")
    fun getFReviewAndReviewForBottle(bottleId: Long): LiveData<List<FilledReviewAndReview>>

    @Query("DELETE FROM f_review WHERE bottle_id=:bottleId AND review_id=:reviewId")
    suspend fun deleteFReviewByPk(bottleId: Long, reviewId: Long)
}
