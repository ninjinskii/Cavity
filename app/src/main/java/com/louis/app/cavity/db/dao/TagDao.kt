package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Tag
import com.louis.app.cavity.model.TagXBottle
import kotlinx.coroutines.flow.Flow

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
    suspend fun getAllTagsNotLive(): List<Tag>

    @Query("SELECT * FROM tag ORDER BY name")
    fun getAllTags(): Flow<List<Tag>>

    @Transaction
    @Query("SELECT * FROM bottle WHERE id=:bottleId")
    fun getTagsForBottle(bottleId: Long): LiveData<BottleWithTags>

    @Transaction
    @Query("SELECT * FROM bottle WHERE id=:bottleId")
    fun getTagsForBottleNotLive(bottleId: Long): BottleWithTags

    @Query("DELETE FROM tag")
    suspend fun deleteAll()
}

data class BottleWithTags(
    @Embedded val bottle: Bottle,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TagXBottle::class,
            parentColumn = "bottle_id",
            entityColumn = "tag_id"
        )
    )
    val tags: List<Tag>
)
