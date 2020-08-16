package com.louis.app.cavity.db

import android.app.Application

class WineRepository(private val wineDao: WineDao) {
    fun getAllWines() = wineDao.getAllWines()
}