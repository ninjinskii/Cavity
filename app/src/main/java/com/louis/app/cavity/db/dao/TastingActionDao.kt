package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.TastingAction

@Dao
interface TastingActionDao {
    @Insert
    suspend fun insertTastingActions(tastingAction: List<TastingAction>)

    @Update
    suspend fun updateTastingAction(tastingAction: TastingAction)

    @Delete
    suspend fun deleteTastingAction(tastingAction: TastingAction)

    @Query("DELETE FROM tasting_action WHERE bottle_id=:bottleId")
    suspend fun deleteTastingActionsForBottle(bottleId: Long)
}
