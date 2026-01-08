package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.louis.app.cavity.model.Tag

@Dao
interface TagDao {
    @Insert
    suspend fun insertTag(tag: Tag): Long

    @Insert
    suspend fun insertTags(tag: List<Tag>)

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("SELECT * FROM tag ORDER BY name")
    fun getAllTags(): LiveData<List<Tag>>
}
