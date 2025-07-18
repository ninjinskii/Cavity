package com.louis.app.cavity.domain.repository

import android.app.Application
import com.louis.app.cavity.model.County

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

    fun getBottleCountForCounty(countyId: Long, storageLocation: String?) = statsDao.getBottleCountForCounty(countyId, storageLocation)
    fun getPriceByCurrencyForCounty(countyId: Long, storageLocation: String?) = statsDao.getPriceByCurrencyForCounty(countyId, storageLocation)
    fun getNamingsStatsForCounty(countyId: Long, storageLocation: String?) = statsDao.getNamingsForCounty(countyId, storageLocation)
    fun getVintagesStatsForCounty(countyId: Long, storageLocation: String?) = statsDao.getVintagesForCounty(countyId, storageLocation)
    fun getStockByCounty() = statsDao.getStockByCounty()
    fun getReplenishmentsByCounty(start: Long, end: Long) =
        statsDao.getReplenishmentsByCounty(start, end)

    fun getConsumptionsByCounty(start: Long, end: Long) =
        statsDao.getConsumptionsByCounty(start, end)

    fun getStockByColor() = statsDao.getStockByColor()
    fun getReplenishmentsByColor(start: Long, end: Long) =
        statsDao.getReplenishmentsByColor(start, end)

    fun getConsumptionsByColor(start: Long, end: Long) =
        statsDao.getConsumptionsByColor(start, end)

    fun getStockByVintage() = statsDao.getStockByVintage()
    fun getReplenishmentsByVintage(start: Long, end: Long) =
        statsDao.getReplenishmentsByVintage(start, end)

    fun getConsumptionsByVintage(start: Long, end: Long) =
        statsDao.getConsumptionsByVintage(start, end)

    fun getStockByNaming() = statsDao.getStockByNaming()
    fun getReplenishmentsByNaming(start: Long, end: Long) =
        statsDao.getReplenishmentsByNaming(start, end)

    fun getConsumptionsByNaming(start: Long, end: Long) =
        statsDao.getConsumptionsByNaming(start, end)

    fun getTotalPriceByCurrency() = statsDao.getTotalPriceByCurrency()
    fun getTotalConsumedBottles() = statsDao.getTotalConsumedBottles()
    fun getTotalStockBottles() = statsDao.getTotalStockBottles()
    fun getBottlesByIds(ids: List<Long>) = statsDao.getBottlesByIds(ids)
}
