package com.louis.app.cavity.domain.stats

import android.app.Application
import androidx.sqlite.db.SimpleSQLiteQuery
import com.louis.app.cavity.db.CavityDatabase
import kotlinx.coroutines.flow.Flow

class RoomStatsQueries(app: Application) : StatsQueries {
    private val database = CavityDatabase.getInstance(app)
    private val statsDao = database.statsDao()

    override fun getStockByCounty() = statsDao.getStockByCounty()
    override fun getStockByColor() = statsDao.getStockByColor()
    override fun getStockByVintage() = statsDao.getStockByVintage()
    override fun getStockByNaming() = statsDao.getStockByNaming()

    override fun getBottleStatsForCounty(countyId: Long, storageLocation: String?) =
        statsDao.getBottleStatsForCounty(countyId, storageLocation)

    override fun getStatsByHistoryEntry(
        start: Long,
        end: Long,
        types: List<Int>,
        groupByColumn: String
    ): Flow<List<BaseStat>> {
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
