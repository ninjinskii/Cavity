package com.louis.app.cavity.db

import android.app.Application
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Wine

class WineRepository(private val wineDao: WineDao, private val bottleDao: BottleDao) {
    fun getAllWines() = wineDao.getAllWines()

    fun insertWine(wine: Wine) = wineDao.insertWine(wine)

    fun getWineWithBottles() = wineDao.getWineWithBottles()

    fun insertBottle(bottle: Bottle) = bottleDao.insertBottle(bottle)

    fun getAllCounties() = wineDao.getAllCounties()
}