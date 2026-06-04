package com.louis.app.cavity.domain.repository

import android.app.Application

class StatsRepository private constructor(app: Application) : Repository(app) {
    companion object {
        @Volatile
        var instance: StatsRepository? = null

        fun getInstance(app: Application) =
            instance ?: synchronized(this) {
                instance ?: StatsRepository(app).also { instance = it }
            }
    }

    private val statsDao = database.statsDao()

    fun getTotalPriceByCurrency() = statsDao.getTotalPriceByCurrency()
    fun getTotalConsumedBottles() = statsDao.getTotalConsumedBottles()
    fun getTotalStockBottles() = statsDao.getTotalStockBottles()
    fun getBottlesByIds(ids: List<Long>) = statsDao.getBottlesByIds(ids)

    /*fun getBottleStatsForCounty(countyId: Long, storageLocation: String?) =
        statsDao.getBottleStatsForCounty(countyId, storageLocation)*/
}
