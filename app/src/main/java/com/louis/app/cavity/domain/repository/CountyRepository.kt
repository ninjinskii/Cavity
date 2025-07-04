package com.louis.app.cavity.domain.repository

import android.app.Application
import com.louis.app.cavity.model.County
import com.louis.app.cavity.domain.repository.RepositoryUpsertResult.*
import com.louis.app.cavity.domain.repository.RepositoryUpsertResult.Companion.handleDatabaseError

class CountyRepository private constructor(app: Application) : Repository(app) {
    companion object {
        @Volatile
        var instance: CountyRepository? = null

        fun getInstance(app: Application) =
            instance ?: synchronized(this) {
                instance ?: CountyRepository(app).also { instance = it }
            }
    }

    private val countyDao = database.countyDao()

    suspend fun insertCounty(county: County): RepositoryUpsertResult<Long> {
        if (!county.hasValidName()) {
            return InvalidName
        }

        try {
            val countyId = countyDao.insertCounty(county)
            return Success(countyId)
        } catch (e: Exception) {
            return handleDatabaseError(e, errorReporter)
        }
    }

    suspend fun insertCounties(counties: List<County>) = countyDao.insertCounties(counties)

    suspend fun updateCounty(county: County): RepositoryUpsertResult<Long> {
        if (!county.hasValidName()) {
            return InvalidName
        }

        try {
            countyDao.updateCounty(county)
            return Success(county.id)
        } catch (e: Exception) {
            return handleDatabaseError(e, errorReporter)
        }
    }

    fun getAllCounties() = countyDao.getAllCounties()

    fun getNonEmptyCounties() = countyDao.getNonEmptyCounties()

    fun getCountiesWithWines() = countyDao.getCountiesWithWines()

    suspend fun getAllCountiesNotLive() = countyDao.getAllCountiesNotLive()

    suspend fun updateCounties(counties: List<County>) = countyDao.updateCounties(counties)

    suspend fun deleteCounty(countyId: Long) = countyDao.deleteCounty(countyId)

    suspend fun deleteAllCounties() = countyDao.deleteAll()
}
