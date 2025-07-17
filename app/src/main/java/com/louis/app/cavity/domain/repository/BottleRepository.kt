package com.louis.app.cavity.domain.repository

import android.app.Application
import com.louis.app.cavity.model.Bottle

class BottleRepository private constructor(app: Application) : Repository(app) {
    companion object {
        @Volatile
        var instance: BottleRepository? = null

        fun getInstance(app: Application) =
            instance ?: synchronized(this) {
                instance ?: BottleRepository(app).also { instance = it }
            }
    }

    private val bottleDao = database.bottleDao()
    private val qGrapeDao = database.qGrapeDao()
    private val historyDao = database.historyDao()

    suspend fun insertBottle(bottle: Bottle) = bottleDao.insertBottle(bottle)

    suspend fun insertBottles(bottles: List<Bottle>) = bottleDao.insertBottles(bottles)

    suspend fun updateBottle(bottle: Bottle) = bottleDao.updateBottle(bottle)

    suspend fun deleteBottles(bottles: List<Bottle>) = bottleDao.deleteBottles(bottles)

    suspend fun deleteBottleById(bottleId: Long) = bottleDao.deleteBottleById(bottleId)

    fun getBottleById(bottleId: Long) = bottleDao.getBottleById(bottleId)

    suspend fun getBottleByIdNotLive(bottleId: Long) = bottleDao.getBottleByIdNotLive(bottleId)

    suspend fun getAllBottlesNotLive() = bottleDao.getAllBottlesNotLive()

    fun getBottlesForWine(wineId: Long) = bottleDao.getBottlesForWine(wineId)

    suspend fun getBottlesForWineNotLive(wineId: Long) = bottleDao.getBottlesForWineNotLive(wineId)

    fun getBoundedBottles() = bottleDao.getBoundedBottles()

    suspend fun getBoundedBottleByIdNotLive(bottleId: Long) =
        bottleDao.getBoundedBottleByIdNotLive(bottleId)

    suspend fun consumeBottle(bottleId: Long) = bottleDao.consumeBottle(bottleId)

    suspend fun fav(bottleId: Long) = bottleDao.fav(bottleId)

    suspend fun unfav(bottleId: Long) = bottleDao.unfav(bottleId)

    suspend fun removeTastingForBottle(bottleId: Long) = bottleDao.removeTastingForBottle(bottleId)

    suspend fun revertBottleConsumption(bottleId: Long) {
        assertTransaction {
            bottleDao.revertBottleConsumption(bottleId)
            historyDao.clearConsumptionsForBottle(bottleId)
        }
    }

    suspend fun getTastingBottleIdsIn(bottles: List<Long>) =
        bottleDao.getTastingBottleIdsIn(bottles)

    suspend fun boundBottlesToTasting(tastingId: Long, bottles: List<Long>) =
        bottleDao.boundBottlesToTasting(tastingId, bottles)

    suspend fun clearAllQGrapesForBottle(bottleId: Long) =
        qGrapeDao.clearAllQGrapesForBottle(bottleId)

    fun getAllBuyLocations() = bottleDao.getAllBuyLocations()

    suspend fun deleteAllBottles() = bottleDao.deleteAll()
}
