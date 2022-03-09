package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Ignore
import androidx.room.Query
import com.louis.app.cavity.model.WineColor
import com.louis.app.cavity.util.ColorUtil

@Dao
interface StatsDao {
    @Query("SELECT COUNT(*) FROM bottle INNER JOIN wine ON wine_id = wine.id WHERE county_id=:countyId")
    fun getBottleCountForCounty(countyId: Long): LiveData<Int>

    @Query(
        """SELECT wine.naming AS label, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                    (SELECT COUNT(*) 
                    FROM bottle INNER JOIN wine ON wine_id = wine.id 
                    WHERE wine.county_id=:countyId AND bottle.consumed = 0) * 100
                    AS percentage 
                FROM bottle
                INNER JOIN wine ON wine_id = wine.id
                WHERE bottle.consumed = 0 AND wine.county_id=:countyId
                GROUP BY naming ORDER BY percentage"""
    )
    fun getNamingsForCounty(countyId: Long): LiveData<List<BaseStat>>

    @Query(
        """SELECT bottle.vintage AS label, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                    (SELECT COUNT(*) 
                    FROM bottle INNER JOIN wine ON wine_id = wine.id 
                    WHERE wine.county_id=:countyId AND bottle.consumed = 0) * 100
                    AS percentage 
                FROM bottle
                INNER JOIN wine ON wine_id = wine.id
                WHERE bottle.consumed = 0 AND wine.county_id=:countyId
                GROUP BY vintage ORDER BY percentage, label"""
    )
    fun getVintagesForCounty(countyId: Long): LiveData<List<BaseStat>>

    @Query(
        """SELECT county.name AS label, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                        (SELECT COUNT(*) FROM bottle WHERE bottle.consumed = 0) * 100
                        AS percentage 
                FROM bottle
                INNER JOIN wine ON wine_id = wine.id
                INNER JOIN county ON county_id = county.id
                WHERE bottle.consumed = 0
                GROUP BY county.name ORDER BY percentage DESC, county.name"""
    )
    fun getStockByCounty(): LiveData<List<BaseStat>>

    @Query(
        """SELECT county.name AS label, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                        (SELECT COUNT(*) FROM history_entry 
                        WHERE (type = 1 OR type = 3) AND date BETWEEN :start AND :end) * 100
                        AS percentage 
                    FROM history_entry
                    INNER JOIN bottle ON bottle_id = bottle.id
                    INNER JOIN wine ON wine_id = wine.id
                    INNER JOIN county ON county_id = county.id
                    WHERE history_entry.date BETWEEN :start AND :end AND (type = 1 OR type = 3)
                    GROUP BY county.name ORDER BY percentage DESC, county.name"""
    )
    fun getReplenishmentsByCounty(start: Long, end: Long): LiveData<List<BaseStat>>

    @Query(
        """SELECT county.name AS label, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                        (SELECT COUNT(*) FROM history_entry 
                        WHERE (type = 0 OR type = 2 OR type = 4) AND date BETWEEN :start AND :end) * 100
                        AS percentage 
                    FROM history_entry
                    INNER JOIN bottle ON bottle_id = bottle.id
                    INNER JOIN wine ON wine_id = wine.id
                    INNER JOIN county ON county_id = county.id
                    WHERE history_entry.date BETWEEN :start AND :end AND (type = 0 OR type = 2 OR type = 4)
                    GROUP BY county.name ORDER BY percentage DESC, county.name"""
    )
    fun getConsumptionsByCounty(start: Long, end: Long): LiveData<List<BaseStat>>

    @Query(
        """SELECT wine.color AS wcolor, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                        (SELECT COUNT(*) FROM bottle WHERE bottle.consumed = 0) * 100
                        AS percentage 
                FROM bottle
                INNER JOIN wine ON wine_id = wine.id
                WHERE bottle.consumed = 0
                GROUP BY wine.color ORDER BY percentage DESC, wine.color"""
    )
    fun getStockByColor(): LiveData<List<WineColorStat>>

    @Query(
        """SELECT wine.color AS wcolor, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                        (SELECT COUNT(*) FROM history_entry 
                        WHERE (type = 1 OR type = 3) AND date BETWEEN :start AND :end) * 100
                        AS percentage 
                    FROM history_entry
                    INNER JOIN bottle ON bottle_id = bottle.id
                    INNER JOIN wine ON wine_id = wine.id
                    WHERE history_entry.date BETWEEN :start AND :end AND (type = 1 OR type = 3)
                    GROUP BY wine.color ORDER BY percentage DESC, wine.color"""
    )
    fun getReplenishmentsByColor(start: Long, end: Long): LiveData<List<WineColorStat>>

    @Query(
        """SELECT wine.color AS wcolor, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                        (SELECT COUNT(*) FROM history_entry 
                        WHERE (type = 0 OR type = 2 OR type = 4) AND date BETWEEN :start AND :end) * 100
                        AS percentage 
                    FROM history_entry
                    INNER JOIN bottle ON bottle_id = bottle.id
                    INNER JOIN wine ON wine_id = wine.id
                    WHERE history_entry.date BETWEEN :start AND :end AND (type = 0 OR type = 2 OR type = 4)
                    GROUP BY wine.color ORDER BY percentage DESC, wine.color"""
    )
    fun getConsumptionsByColor(start: Long, end: Long): LiveData<List<WineColorStat>>

    @Query(
        """SELECT bottle.vintage AS label, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                        (SELECT COUNT(*) FROM bottle WHERE bottle.consumed = 0) * 100
                        AS percentage 
                FROM bottle
                WHERE bottle.consumed = 0
                GROUP BY bottle.vintage ORDER BY percentage DESC, bottle.vintage"""
    )
    fun getStockByVintage(): LiveData<List<BaseStat>>

    @Query(
        """SELECT bottle.vintage AS label, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                        (SELECT COUNT(*) FROM history_entry 
                        WHERE (type = 1 OR type = 3) AND date BETWEEN :start AND :end) * 100
                        AS percentage 
                    FROM history_entry
                    INNER JOIN bottle ON bottle_id = bottle.id
                    WHERE history_entry.date BETWEEN :start AND :end AND (type = 1 OR type = 3)
                    GROUP BY bottle.vintage ORDER BY percentage DESC, bottle.vintage"""
    )
    fun getReplenishmentsByVintage(start: Long, end: Long): LiveData<List<BaseStat>>

    @Query(
        """SELECT bottle.vintage AS label, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                        (SELECT COUNT(*) FROM history_entry 
                        WHERE (type = 0 OR type = 2 OR type = 4) AND date BETWEEN :start AND :end) * 100
                        AS percentage 
                    FROM history_entry
                    INNER JOIN bottle ON bottle_id = bottle.id
                    WHERE history_entry.date BETWEEN :start AND :end AND (type = 0 OR type = 2 OR type = 4)
                    GROUP BY bottle.vintage ORDER BY percentage DESC, bottle.vintage"""
    )
    fun getConsumptionsByVintage(start: Long, end: Long): LiveData<List<BaseStat>>

    @Query(
        """SELECT wine.naming AS label, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                        (SELECT COUNT(*) FROM bottle WHERE bottle.consumed = 0) * 100
                        AS percentage 
                FROM bottle
                INNER JOIN wine on wine_id = wine.id
                WHERE bottle.consumed = 0
                GROUP BY wine.naming ORDER BY percentage DESC, wine.naming"""
    )
    fun getStockByNaming(): LiveData<List<BaseStat>>

    @Query(
        """SELECT wine.naming AS label, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                        (SELECT COUNT(*) FROM history_entry 
                        WHERE (type = 1 OR type = 3) AND date BETWEEN :start AND :end) * 100
                        AS percentage 
                    FROM history_entry
                    INNER JOIN bottle ON bottle_id = bottle.id
                    INNER JOIN wine ON wine_id = wine.id
                    WHERE history_entry.date BETWEEN :start AND :end AND (type = 1 OR type = 3)
                    GROUP BY wine.naming ORDER BY percentage DESC, wine.naming"""
    )
    fun getReplenishmentsByNaming(start: Long, end: Long): LiveData<List<BaseStat>>

    @Query(
        """SELECT wine.naming AS label, COUNT(*) AS count, (cast( COUNT (*) AS REAL)) / 
                        (SELECT COUNT(*) FROM history_entry 
                        WHERE (type = 0 OR type = 2 OR type = 4) AND date BETWEEN :start AND :end) * 100
                        AS percentage 
                    FROM history_entry
                    INNER JOIN bottle ON bottle_id = bottle.id
                    INNER JOIN wine ON wine_id = wine.id
                    WHERE history_entry.date BETWEEN :start AND :end AND (type = 0 OR type = 2 OR type = 4)
                    GROUP BY wine.naming ORDER BY percentage DESC, wine.naming"""
    )
    fun getConsumptionsByNaming(start: Long, end: Long): LiveData<List<BaseStat>>

    @Query("""SELECT SUM(price) as sum, currency FROM bottle WHERE price != -1  GROUP BY currency""")
    fun getTotalPriceByCurrency(): LiveData<List<PriceByCurrency>>

    @Query("""SELECT COUNT(*) FROM bottle WHERE consumed = 1""")
    fun getTotalConsumedBottles(): LiveData<Int>

    @Query("""SELECT COUNT(*) FROM bottle WHERE consumed = 0""")
    fun getTotalStockBottles(): LiveData<Int>
}

interface Stat {
    val label: String
    val count: Int
    val percentage: Float
    val color: Int
}

data class BaseStat(
    override val label: String,
    override val count: Int,
    override val percentage: Float,
) : Stat {
    @Ignore
    override val color = ColorUtil.next()
}

data class WineColorStat(
    val wcolor: WineColor,
    override val count: Int,
    override val percentage: Float,
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

