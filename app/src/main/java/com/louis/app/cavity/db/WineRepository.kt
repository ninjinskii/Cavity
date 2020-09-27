package com.louis.app.cavity.db

import com.louis.app.cavity.model.*

// TODO: convert to singleton
class WineRepository(database: CavityDatabase) {
    val wineDao = database.wineDao()
    val bottleDao = database.bottleDao()
    val countyDao = database.countyDao()

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
    fun getWineWithBottles() = wineDao.getWineWithBottles()
    fun getWineWithBottlesNotLive() = wineDao.getWineWithBottlesNotLive()
    fun getWineWithBottlesByCounty(countyId: Long) = wineDao.getWineWithBottlesByCounty(countyId)

    fun getAllCounties() = countyDao.getAllCounties()
    fun getAllCountiesNotLive() = countyDao.getAllCountiesNotLive()

    fun getAllBottles() = bottleDao.getAllBottles()
    fun getBottleById(bottleId: Long) = bottleDao.getBottleById(bottleId)
    fun getBottleWithGrapesById(bottleId: Long) = bottleDao.getBottleWithGrapesById(bottleId)
    fun getBottleWithExpertAdviceById(bottleId: Long) =
        bottleDao.getBottleWithExpertAdvicesById(bottleId)
    fun updateBottle(bottle: Bottle) = bottleDao.updateBottle(bottle)
    fun deleteBottleById(bottleId: Long) = bottleDao.deleteBottleById(bottleId)

    fun getAllGrapes() = bottleDao.getAllGrapes()

    fun getAllExpertAdvices() = bottleDao.getAllExpertAdvices()

    fun getCountiesWithWinesNotLive() = countyDao.getCountiesWithWinesNotLive()

}
