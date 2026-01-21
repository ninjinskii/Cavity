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

    fun getAllTags() = tagDao.getAllTags()
    suspend fun getAllTagsNotLive() = tagDao.getAllTagsNotLive()
    fun getAllTagBottleXRefs() = tagXBottleDao.getAllTagXBottles()
    fun getTagsForBottle(bottleId: Long) = tagDao.getTagsForBottle(bottleId)
    fun getTagsForBottleNotLive(bottleId: Long) = tagDao.getTagsForBottleNotLive(bottleId)
    fun getBottlesForTag(tagId: Long) = tagXBottleDao.getBottlesForTag(tagId)
    fun getTagIdsForBottle(bottleId: Long) = tagXBottleDao.getTagIdsForBottle(bottleId)

    suspend fun getAllTagBottleXRefsNotLive() = tagXBottleDao.getAllTagXBottlesNotLive()
    suspend fun clearTagsForBottle(bottleId: Long) = tagXBottleDao.clearTagsForBottleId(bottleId)
    suspend fun insertTagBottleXRefs(tagXBottles: List<TagXBottle>) =
        tagXBottleDao.insertTagXBottles(tagXBottles)
    suspend fun deleteTagBottleXref(tagXBottle: TagXBottle) =
        tagXBottleDao.deleteTagXBottle(tagXBottle)
}
