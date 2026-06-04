package com.louis.app.cavity.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery
import com.louis.app.cavity.domain.stats.BaseStat
import com.louis.app.cavity.domain.stats.BottleStatRow
import com.louis.app.cavity.domain.stats.PriceByCurrency
import com.louis.app.cavity.domain.stats.WineColorStat
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.Wine
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {
    @Query(
        """
    SELECT
        bottle.id AS bottleId,
        wine.naming AS naming,
        bottle.vintage AS vintage,
        bottle.price AS price,
        bottle.currency AS currency
    FROM bottle
    INNER JOIN wine ON bottle.wine_id = wine.id
    WHERE wine.county_id = :countyId
        AND bottle.consumed = 0
        AND (
            bottle.storage_location = :storageLocation
            OR :storageLocation IS NULL
        )
        """
    )
    fun getBottleStatsForCounty(
        countyId: Long,
        storageLocation: String?
    ): Flow<List<BottleStatRow>>

    @Query(
        """SELECT county.name AS label, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                        (SELECT COUNT(*) FROM bottle WHERE bottle.consumed = 0) * 100
                        AS percentage,
                        GROUP_CONCAT(DISTINCT bottle.id) AS bottleIds
                FROM bottle
                INNER JOIN wine ON wine_id = wine.id
                INNER JOIN county ON county_id = county.id
                WHERE bottle.consumed = 0
                GROUP BY county.name ORDER BY percentage DESC, county.name"""
    )
    fun getStockByCounty(): Flow<List<BaseStat>>

    @Query(
        """SELECT wine.color AS wcolor, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                        (SELECT COUNT(*) FROM bottle WHERE bottle.consumed = 0) * 100
                        AS percentage,
                        GROUP_CONCAT(DISTINCT bottle.id) AS bottleIds
                FROM bottle
                INNER JOIN wine ON wine_id = wine.id
                WHERE bottle.consumed = 0
                GROUP BY wine.color ORDER BY percentage DESC, wine.color"""
    )
    fun getStockByColor(): Flow<List<WineColorStat>>

    @Query(
        """SELECT bottle.vintage AS label, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                        (SELECT COUNT(*) FROM bottle WHERE bottle.consumed = 0) * 100
                        AS percentage,
                        GROUP_CONCAT(DISTINCT bottle.id) AS bottleIds
                FROM bottle
                WHERE bottle.consumed = 0
                GROUP BY bottle.vintage ORDER BY percentage DESC, bottle.vintage"""
    )
    fun getStockByVintage(): Flow<List<BaseStat>>

    @Query(
        """SELECT wine.naming AS label, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                        (SELECT COUNT(*) FROM bottle WHERE bottle.consumed = 0) * 100
                        AS percentage,
                        GROUP_CONCAT(DISTINCT bottle.id) AS bottleIds
                FROM bottle
                INNER JOIN wine on wine_id = wine.id
                WHERE bottle.consumed = 0
                GROUP BY wine.naming ORDER BY percentage DESC, wine.naming"""
    )
    fun getStockByNaming(): Flow<List<BaseStat>>

    @Query("""SELECT SUM(price) as sum, currency FROM bottle WHERE price != -1  GROUP BY currency""")
    fun getTotalPriceByCurrency(): Flow<List<PriceByCurrency>>

    @Query("""SELECT COUNT(*) FROM bottle WHERE consumed = 1""")
    fun getTotalConsumedBottles(): Flow<Int>

    @Query("""SELECT COUNT(*) FROM bottle WHERE consumed = 0""")
    fun getTotalStockBottles(): Flow<Int>

    @Transaction
    @Query("""SELECT * FROM bottle WHERE id IN (:ids)""")
    fun getBottlesByIds(ids: List<Long>): Flow<List<BoundedBottle>>

    @Transaction
    @RawQuery(observedEntities = [HistoryEntry::class, Bottle::class, Wine::class, County::class])
    fun getStatsRaw(query: SupportSQLiteQuery): Flow<List<BaseStat>>
}

