package com.louis.app.cavity.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.model.relation.crossref.TastingFriendXRef

@Dao
interface TastingFriendXRefDao {
    @Insert
    suspend fun insertTastingFriendXRef(tasting: TastingFriendXRef)

    @Update
    suspend fun updateTastingFriendXRef(tasting: TastingFriendXRef)

    @Delete
    suspend fun deleteTastingFriendXRef(tasting: TastingFriendXRef)
}
