package com.louis.app.cavity.domain.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.louis.app.cavity.db.dao.BaseStat

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

    fun getBottleCountForCounty(countyId: Long, storageLocation: String?) =
        statsDao.getBottleCountForCounty(countyId, storageLocation)

    fun getPriceByCurrencyForCounty(countyId: Long, storageLocation: String?) =
        statsDao.getPriceByCurrencyForCounty(countyId, storageLocation)

    fun getNamingsStatsForCounty(countyId: Long, storageLocation: String?) =
        statsDao.getNamingsForCounty(countyId, storageLocation)

    fun getVintagesStatsForCounty(countyId: Long, storageLocation: String?) =
        statsDao.getVintagesForCounty(countyId, storageLocation)

    fun getStockByCounty() = statsDao.getStockByCounty()
    fun getStockByColor() = statsDao.getStockByColor()
    fun getStockByVintage() = statsDao.getStockByVintage()
    fun getStockByNaming() = statsDao.getStockByNaming()

    fun getTotalPriceByCurrency() = statsDao.getTotalPriceByCurrency()
    fun getTotalConsumedBottles() = statsDao.getTotalConsumedBottles()
    fun getTotalStockBottles() = statsDao.getTotalStockBottles()
    fun getBottlesByIds(ids: List<Long>) = statsDao.getBottlesByIds(ids)

    fun getStatsByHistoryEntry(
        start: Long,
        end: Long,
        types: List<Int>,
        groupByColumn: String
    ): LiveData<List<BaseStat>> {
        val typesList = types.joinToString(",")
        val sql = """
        SELECT $groupByColumn AS label,
               COUNT(*) AS count,
               (CAST(COUNT(*) AS REAL)) / 
               (SELECT COUNT(*) FROM history_entry 
                WHERE type IN ($typesList) AND date BETWEEN ? AND ?) * 100 AS percentage,
               GROUP_CONCAT(DISTINCT bottle.id) AS bottleIds
        FROM history_entry
        INNER JOIN bottle ON bottle_id = bottle.id
        INNER JOIN wine ON wine_id = wine.id
        INNER JOIN county ON county_id = county.id
        WHERE date BETWEEN ? AND ?
          AND type IN ($typesList)
        GROUP BY $groupByColumn
        ORDER BY percentage DESC, label
    """.trimIndent()

        val args = arrayOf(start, end, start, end)
        return statsDao.getStatsRaw(SimpleSQLiteQuery(sql, args))
    }
}
