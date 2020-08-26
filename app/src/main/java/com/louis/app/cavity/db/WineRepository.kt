package com.louis.app.cavity.db

import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Wine

class WineRepository(private val wineDao: WineDao, private val bottleDao: BottleDao) {
    fun getAllWines() = wineDao.getAllWines()
    fun insertWine(wine: Wine) = wineDao.insertWine(wine)
    fun getWineWithBottles() = wineDao.getWineWithBottles()
    fun getWineWithBottlesByCounty(countyId: Long) = wineDao.getWineWithBottlesByCounty(countyId)

    fun insertBottle(bottle: Bottle) = bottleDao.insertBottle(bottle)
    fun getAllBottles() = bottleDao.getAllBottles()

    fun insertCounty(county: County) = wineDao.insertCounty(county)
    fun getAllCounties() = wineDao.getAllCounties()
    fun getAllCountiesNotLive() = wineDao.getAllCountiesNotLive()
}
