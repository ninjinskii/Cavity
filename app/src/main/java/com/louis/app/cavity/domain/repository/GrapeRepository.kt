package com.louis.app.cavity.domain.repository

import android.app.Application
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.QGrape
import com.louis.app.cavity.domain.repository.RepositoryUpsertResult.*
import com.louis.app.cavity.domain.repository.RepositoryUpsertResult.Companion.handleDatabaseError

class GrapeRepository private constructor(app: Application) : Repository(app) {
    companion object {
        @Volatile
        var instance: GrapeRepository? = null

        fun getInstance(app: Application) =
            instance ?: synchronized(this) {
                instance ?: GrapeRepository(app).also { instance = it }
            }
    }

    private val grapeDao = database.grapeDao()
    private val qGrapeDao = database.qGrapeDao()

    suspend fun updateGrape(grape: Grape): RepositoryUpsertResult<Long> {
        if (!grape.hasValidName()) {
            return InvalidName
        }

        try {
            grapeDao.updateGrape(grape)
            return Success(grape.id)
        } catch (e: Exception) {
            return handleDatabaseError(e, errorReporter)
        }
    }

    suspend fun insertGrape(grape: Grape): RepositoryUpsertResult<Long> {
        if (!grape.hasValidName()) {
            return InvalidName
        }

        try {
            val grapeId = grapeDao.insertGrape(grape)
            return Success(grapeId)
        } catch (e: Exception) {
            return handleDatabaseError(e, errorReporter)
        }
    }

    suspend fun insertGrapes(grapes: List<Grape>) = grapeDao.insertGrapes(grapes)
    suspend fun deleteGrape(grape: Grape) = grapeDao.deleteGrape(grape)
    fun getAllGrapes() = grapeDao.getAllGrapes()
    suspend fun getAllGrapesNotLive() = grapeDao.getAllGrapesNotLive()
    fun getGrapeWithQuantifiedGrapes() = grapeDao.getGrapeWithQuantifiedGrapes()
    suspend fun getAllQGrapesNotLive() = qGrapeDao.getAllQGrapesNotLive()
    suspend fun insertQGrapes(grapes: List<QGrape>) = qGrapeDao.insertQGrapes(grapes)

    fun getQGrapesAndGrapeForBottle(bottleId: Long) =
        qGrapeDao.getQGrapesAndGrapeForBottle(bottleId)

    suspend fun getQGrapesAndGrapeForBottleNotLive(bottleId: Long) =
        qGrapeDao.getQGrapesAndGrapeForBottleNotLive(bottleId)

    suspend fun deleteAllGrapes() = grapeDao.deleteAll()
    suspend fun deleteAllQGrapes() = qGrapeDao.deleteAll()
}
