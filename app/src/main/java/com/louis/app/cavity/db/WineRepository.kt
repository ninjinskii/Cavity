package com.louis.app.cavity.db

import android.app.Application
import com.louis.app.cavity.model.Bottle

class WineRepository(private val wineDao: WineDao, private val bottleDao: BottleDao) {
    fun getAllWines() = wineDao.getAllWines()

    fun getWineWithBottles() = wineDao.getWineWithBottles()

    fun insertBottle(bottle: Bottle) = bottleDao.insertBottle(bottle)
}