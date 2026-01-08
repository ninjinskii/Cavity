package com.louis.app.cavity.domain.repository

import android.app.Application
import com.louis.app.cavity.domain.repository.RepositoryUpsertResult.Companion.handleDatabaseError
import com.louis.app.cavity.domain.repository.RepositoryUpsertResult.InvalidName
import com.louis.app.cavity.domain.repository.RepositoryUpsertResult.Success
import com.louis.app.cavity.model.Tag
import com.louis.app.cavity.model.TagXBottle

class TagRepository private constructor(app: Application) : Repository(app) {
    companion object {
        @Volatile
        var instance: TagRepository? = null

        fun getInstance(app: Application) =
            instance ?: synchronized(this) {
                instance ?: TagRepository(app).also { instance = it }
            }
    }

    private val tagDao = database.tagDao()
    private val tagXBottleDao = database.tagXBottleDao()

    suspend fun updateTag(tag: Tag): RepositoryUpsertResult<Long> {
        if (!tag.hasValidName()) {
            return InvalidName
        }

        try {
            tagDao.updateTag(tag)
            return Success(tag.id)
        } catch (e: Exception) {
            return handleDatabaseError(e, errorReporter)
        }
    }

    suspend fun insertTag(tag: Tag): RepositoryUpsertResult<Long> {
        if (!tag.hasValidName()) {
            return InvalidName
        }

        try {
            val tagId = tagDao.insertTag(tag)
            return Success(tagId)
        } catch (e: Exception) {
            return handleDatabaseError(e, errorReporter)
        }
    }

    fun getAllTagBottleXRefs() = tagXBottleDao.getAllTagXBottles()
    fun getTagsForBottle(bottleId: Long) = tagXBottleDao.getTagsForBottle(bottleId)
    fun getBottlesForTag(tagId: Long) = tagXBottleDao.getTagsForBottle(tagId)
    suspend fun deleteTag(tag: Tag) = tagDao.deleteTag(tag)
    suspend fun getAllTagBottleXRefsNotLive() = tagXBottleDao.getAllTagXBottlesNotLive()
    suspend fun insertTagBottleXRefs(tagXBottles: List<TagXBottle>) =
        tagXBottleDao.insertTagXBottles(tagXBottles)
}
