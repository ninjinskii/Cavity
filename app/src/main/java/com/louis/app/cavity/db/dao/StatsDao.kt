package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Ignore
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteQuery
import com.louis.app.cavity.db.StatsBottleIdsTypeConverter
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.model.WineColor
import com.louis.app.cavity.util.ColorUtil

@Dao
interface StatsDao {
    @Query("SELECT COUNT(*) FROM bottle INNER JOIN wine ON wine_id = wine.id WHERE county_id=:countyId AND consumed != 1 AND (bottle.storage_location = :storageLocation OR :storageLocation IS NULL)")
    fun getBottleCountForCounty(countyId: Long, storageLocation: String?): LiveData<Int>

    @Query("""SELECT SUM(price) as sum, currency FROM bottle INNER JOIN wine ON wine_id = wine.id WHERE price != -1 AND county_id=:countyId AND consumed != 1 AND (bottle.storage_location = :storageLocation OR :storageLocation IS NULL) GROUP BY currency""")
    fun getPriceByCurrencyForCounty(
        countyId: Long,
        storageLocation: String?
    ): LiveData<List<PriceByCurrency>>

    @Query(
        """SELECT wine.naming AS label, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                    (SELECT COUNT(*) 
                    FROM bottle INNER JOIN wine ON wine_id = wine.id 
                    WHERE wine.county_id=:countyId AND bottle.consumed = 0 AND (bottle.storage_location = :storageLocation OR :storageLocation IS NULL)) * 100
                    AS percentage,
                    GROUP_CONCAT(DISTINCT bottle.id) AS bottleIds
                FROM bottle
                INNER JOIN wine ON wine_id = wine.id
                WHERE bottle.consumed = 0 AND wine.county_id=:countyId AND (bottle.storage_location = :storageLocation OR :storageLocation IS NULL)
                GROUP BY naming ORDER BY percentage"""
    )
    fun getNamingsForCounty(countyId: Long, storageLocation: String?): LiveData<List<BaseStat>>

    @Query(
        """SELECT bottle.vintage AS label, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                    (SELECT COUNT(*) 
                    FROM bottle INNER JOIN wine ON wine_id = wine.id 
                    WHERE wine.county_id=:countyId AND bottle.consumed = 0 AND (bottle.storage_location = :storageLocation OR :storageLocation IS NULL)) * 100
                    AS percentage,
                    GROUP_CONCAT(DISTINCT bottle.id) AS bottleIds
                FROM bottle
                INNER JOIN wine ON wine_id = wine.id
                WHERE bottle.consumed = 0 AND wine.county_id=:countyId AND (bottle.storage_location = :storageLocation OR :storageLocation IS NULL)
                GROUP BY vintage ORDER BY percentage, label"""
    )
    fun getVintagesForCounty(countyId: Long, storageLocation: String?): LiveData<List<BaseStat>>

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
    fun getStockByCounty(): LiveData<List<BaseStat>>

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
    fun getStockByColor(): LiveData<List<WineColorStat>>

    @Query(
        """SELECT bottle.vintage AS label, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                        (SELECT COUNT(*) FROM bottle WHERE bottle.consumed = 0) * 100
                        AS percentage,
                        GROUP_CONCAT(DISTINCT bottle.id) AS bottleIds
                FROM bottle
                WHERE bottle.consumed = 0
                GROUP BY bottle.vintage ORDER BY percentage DESC, bottle.vintage"""
    )
    fun getStockByVintage(): LiveData<List<BaseStat>>

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
    fun getStockByNaming(): LiveData<List<BaseStat>>

    @Query("""SELECT SUM(price) as sum, currency FROM bottle WHERE price != -1  GROUP BY currency""")
    fun getTotalPriceByCurrency(): LiveData<List<PriceByCurrency>>

    @Query("""SELECT COUNT(*) FROM bottle WHERE consumed = 1""")
    fun getTotalConsumedBottles(): LiveData<Int>

    @Query("""SELECT COUNT(*) FROM bottle WHERE consumed = 0""")
    fun getTotalStockBottles(): LiveData<Int>

    @Transaction
    @Query("""SELECT * FROM bottle WHERE id IN (:ids)""")
    fun getBottlesByIds(ids: List<Long>): LiveData<List<BoundedBottle>>

    @Transaction
    @RawQuery(observedEntities = [HistoryEntry::class, Bottle::class, Wine::class, County::class])
    fun getStatsRaw(query: SupportSQLiteQuery): LiveData<List<BaseStat>>
}

interface Stat {
    val label: String
    val count: Int
    val percentage: Float
    val color: Int
    val bottleIds: List<Long>
}

data class BaseStat(
    override val label: String,
    override val count: Int,
    override val percentage: Float,
    @field:TypeConverters(StatsBottleIdsTypeConverter::class) override val bottleIds: List<Long>
) : Stat {
    @Ignore
    override val color = ColorUtil.next()
}

data class WineColorStat(
    val wcolor: WineColor,
    override val count: Int,
    override val percentage: Float,
    @field:TypeConverters(StatsBottleIdsTypeConverter::class) override val bottleIds: List<Long>
) : Stat {
    @Ignore
    override val label = wcolor.name

    @Ignore
    override val color = ColorUtil.getColorResForWineColor(wcolor.ordinal)
}

data class PriceByCurrency(
    val sum: Long,
    val currency: String
) {
    override fun toString(): String {
        return "$sum $currency"
    }
}

