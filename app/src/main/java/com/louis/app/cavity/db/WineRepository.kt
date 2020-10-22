package com.louis.app.cavity.db

import android.app.Application
import com.louis.app.cavity.model.*

class WineRepository private constructor(app: Application) {

    companion object {
        @Volatile
        var instance: WineRepository? = null

        fun getInstance(app: Application) =
            instance ?: synchronized(this) {
                instance ?: WineRepository(app).also { instance = it }
            }
    }

    private val database = CavityDatabase.getInstance(app)

    private val wineDao = database.wineDao()
    private val bottleDao = database.bottleDao()
    private val countyDao = database.countyDao()

    fun insertWine(wine: Wine) = wineDao.insertWine(wine)
    fun updateWine(wine: Wine) = wineDao.updateWine(wine)
    fun deleteWine(wine: Wine) = wineDao.deleteWine(wine)

    fun insertCounty(county: County) = countyDao.insertCounty(county)

    fun insertBottle(bottle: Bottle) = bottleDao.insertBottle(bottle)

    fun updateGrape(grape: Grape) = bottleDao.updateGrape(grape)
    fun deleteGrape(grape: Grape) = bottleDao.deleteGrape(grape)
    fun insertGrape(grape: Grape) = bottleDao.insertGrape(grape)

    fun updateAdvice(advice: ExpertAdvice) = bottleDao.updateAdvice(advice)
    fun deleteAdvice(advice: ExpertAdvice) = bottleDao.deleteAdvice(advice)
    fun insertAdvice(advice: ExpertAdvice) = bottleDao.insertAdvice(advice)

    fun getAllWines() = wineDao.getAllWines()
    fun getWineByIdNotLive(wineId: Long) = wineDao.getWineByIdNotLive(wineId)
    fun getWineWithBottles() = wineDao.getWineWithBottles()
    fun getWineWithBottlesNotLive() = wineDao.getWineWithBottlesNotLive()
    fun getWineWithBottlesByCounty(countyId: Long) = wineDao.getWineWithBottlesByCounty(countyId)

    fun getAllCounties() = countyDao.getAllCounties()
    fun getAllCountiesNotLive() = countyDao.getAllCountiesNotLive()

    fun getAllBottles() = bottleDao.getAllBottles()
    fun getBottleByIdNotLive(bottleId: Long) = bottleDao.getBottleByIdNotLive(bottleId)
    fun getGrapesForBottleNotLive(bottleId: Long) = bottleDao.getGrapesForBottleNotLive(bottleId)
    fun getExpertAdvicesForBottleNotLive(bottleId: Long) =
        bottleDao.getExpertAdvicesForBottleNotLive(bottleId)

    fun updateBottle(bottle: Bottle) = bottleDao.updateBottle(bottle)
    fun deleteBottleById(bottleId: Long) = bottleDao.deleteBottleById(bottleId)
    fun getBottlesAndWineNotLive() = bottleDao.getBottlesAndWineNotLive()
    fun getBottleWithGrapesNotLive() = bottleDao.getBottleWithGrapesNotLive()

    fun getAllGrapes() = bottleDao.getAllGrapes()

    fun getAllExpertAdvices() = bottleDao.getAllExpertAdvices()

    fun getCountiesWithWinesNotLive() = countyDao.getCountiesWithWinesNotLive()

}
