package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.louis.app.cavity.model.Tag
import com.louis.app.cavity.model.TagXBottle
import kotlinx.coroutines.flow.Flow

@Dao
interface TagXBottleDao {
    @Insert
    suspend fun insertTagXBottle(tagXBottle: TagXBottle)

    @Insert
    suspend fun insertTagXBottles(tagXBottle: List<TagXBottle>)

    @Update
    suspend fun updateTagXBottle(tagXBottle: TagXBottle)

    @Delete
    suspend fun deleteTagXBottle(tagXBottle: TagXBottle)

    @Query("SELECT * FROM tag_bottle_xref")
    suspend fun getAllTagXBottlesNotLive(): List<TagXBottle>

    @Query("SELECT * FROM tag_bottle_xref")
    fun getAllTagXBottles(): Flow<List<TagXBottle>>

    @Query("SELECT tag_id FROM tag_bottle_xref WHERE bottle_id=:bottleId")
    fun getTagIdsForBottle(bottleId: Long): Flow<List<Long>>

    @Query("SELECT * FROM tag_bottle_xref WHERE tag_id=:tagId")
    fun getBottlesForTag(tagId: Long): LiveData<List<TagXBottle>>

    @Query("DELETE FROM tag_bottle_xref WHERE bottle_id=:bottleId")
    suspend fun clearTagsForBottleId(bottleId: Long)

    @Query("DELETE FROM tag_bottle_xref")
    suspend fun deleteAll()
}
