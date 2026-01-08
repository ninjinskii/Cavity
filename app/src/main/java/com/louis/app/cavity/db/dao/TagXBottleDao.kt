package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.louis.app.cavity.model.TagXBottle

@Dao
interface TagXBottleDao {
    @Insert
    suspend fun insertTagXBottle(tasting: TagXBottle)

    @Insert
    suspend fun insertTagXBottles(tastings: List<TagXBottle>)

    @Update
    suspend fun updateTagXBottle(tasting: TagXBottle)

    @Delete
    suspend fun deleteTagXBottle(tasting: TagXBottle)

    @Query("SELECT * FROM tag_bottle_xref")
    suspend fun getAllTagXBottlesNotLive(): List<TagXBottle>

    @Query("SELECT * FROM tag_bottle_xref")
    fun getAllTagXBottles(): LiveData<List<TagXBottle>>

    @Query("SELECT * FROM tag_bottle_xref WHERE bottle_id=:bottleId")
    fun getTagsForBottle(bottleId: Long): LiveData<List<TagXBottle>>

    @Query("SELECT * FROM tag_bottle_xref WHERE tag_id=:tagId")
    fun getBottlesForTag(tagId: Long): LiveData<List<TagXBottle>>

    @Query("DELETE FROM tag_bottle_xref")
    suspend fun deleteAll()
}
