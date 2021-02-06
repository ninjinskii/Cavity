package com.louis.app.cavity.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import com.louis.app.cavity.model.Tasting

@Dao
interface TastingDao {
    @Insert
    suspend fun insertTasting(tasting: Tasting)

    @Update
    suspend fun updateTasting(tasting: Tasting)

    @Delete
    suspend fun deleteTasting(tasting: Tasting)
}
