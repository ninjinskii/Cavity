package com.louis.app.cavity.db.dao

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

    @Query("SELECT * FROM tasting_action WHERE id=:tastingActionId")
    suspend fun getTastingActionById(tastingActionId: Long): TastingAction

    @Query("DELETE FROM tasting_action WHERE bottle_id=:bottleId")
    suspend fun deleteTastingActionsForBottle(bottleId: Long)
}
