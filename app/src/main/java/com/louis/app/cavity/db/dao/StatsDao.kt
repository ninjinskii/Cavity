package com.louis.app.cavity.db.dao

import androidx.annotation.ColorRes
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Ignore
import androidx.room.Query
import com.louis.app.cavity.util.ColorUtil

@Dao
interface StatsDao {
    @Query("SELECT COUNT(*) FROM bottle INNER JOIN wine ON wine_id = wine.id WHERE county_id=:countyId")
    fun getBottleCountForCounty(countyId: Long): LiveData<Int>

    @Query(
        """SELECT wine.naming AS label, round((cast( COUNT (*) AS REAL)) / 
                    (SELECT COUNT(*) 
                        FROM bottle INNER JOIN wine ON wine_id = wine.id 
                        WHERE wine.county_id=:countyId AND bottle.consumed = 0) * 100, 0) AS percentage FROM bottle
                INNER JOIN wine ON wine_id = wine.id
                WHERE bottle.consumed = 0 AND wine.county_id=:countyId
                GROUP BY naming"""
    )
    fun getNamingsForCounty(countyId: Long): LiveData<List<BaseStat>>

    @Query(
        """SELECT bottle.vintage AS label, round((cast( COUNT (*) AS REAL)) / 
                    (SELECT COUNT(*) 
                        FROM bottle INNER JOIN wine ON wine_id = wine.id 
                        WHERE wine.county_id=:countyId AND bottle.consumed = 0) * 100, 0) AS percentage FROM bottle
                INNER JOIN wine ON wine_id = wine.id
                WHERE bottle.consumed = 0 AND wine.county_id=:countyId
                GROUP BY vintage ORDER BY percentage, label"""
    )
    fun getVintagesForCounty(countyId: Long): LiveData<List<BaseStat>>

    @Query(
        """SELECT COUNT (*) as count, county.name as label
                FROM bottle
                INNER JOIN wine ON wine_id = wine.id
                INNER JOIN county ON wine.county_id = county.id
                WHERE bottle.consumed = 0
                GROUP BY label"""
    )
    fun getStockByCounty(): LiveData<List<CountyStat>>

    @Query(
        """SELECT COUNT (*) as count, county.name as label
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id
                INNER JOIN wine ON wine_id = wine.id
                INNER JOIN county ON wine.county_id = county.id
                WHERE date BETWEEN :start AND :end AND type = 1 OR type = 3 GROUP BY label"""
    )
    fun getReplenishmentsByCounty(start: Long, end: Long): LiveData<List<CountyStat>>

    @Query(
        """SELECT COUNT (*) as count, county.name as label
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id
                INNER JOIN wine ON wine_id = wine.id
                INNER JOIN county ON wine.county_id = county.id
                WHERE date BETWEEN :start AND :end AND type = 0 OR type = 2 GROUP BY label"""
    )
    fun getConsumptionsByCounty(start: Long, end: Long): LiveData<List<CountyStat>>

    @Query(
        """SELECT COUNT (*) as count, color, '' as label
                FROM bottle
                INNER JOIN wine ON wine_id = wine.id
                WHERE bottle.consumed = 0
                GROUP BY color"""
    )
    fun getStockByColor(): LiveData<List<ColorStat>>

    @Query(
        """SELECT COUNT (*) as count, color, '' as label
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id 
                INNER JOIN wine ON wine_id = wine.id
                WHERE date BETWEEN :start AND :end AND type = 1 OR type = 3 GROUP BY color"""
    )
    fun getReplenishmentsByColor(start: Long, end: Long): LiveData<List<ColorStat>>

    @Query(
        """SELECT COUNT (*) as count, color, '' as label
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id 
                INNER JOIN wine ON wine_id = wine.id
                WHERE date BETWEEN :start AND :end AND type = 0 OR type = 2 GROUP BY color"""
    )
    fun getConsumptionsByColor(start: Long, end: Long): LiveData<List<ColorStat>>

    @Query(
        """SELECT COUNT (*) as count, vintage as label
                FROM bottle
                WHERE bottle.consumed = 0
                GROUP BY label"""
    )
    fun getStockByVintage(): LiveData<List<VintageStat>>

    @Query(
        """SELECT COUNT (*) as count, vintage as label
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id
                WHERE date BETWEEN :start AND :end AND type = 1 OR type = 3 GROUP BY label"""
    )
    fun getReplenishmentsByVintage(start: Long, end: Long): LiveData<List<VintageStat>>

    @Query(
        """SELECT COUNT (*) as count, vintage as label
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id
                WHERE date BETWEEN :start AND :end AND type = 0 OR type = 2 GROUP BY label"""
    )
    fun getConsumptionsByVintage(start: Long, end: Long): LiveData<List<VintageStat>>

    @Query(
        """SELECT COUNT (*) as count, naming as label
                FROM bottle
                INNER JOIN wine ON wine_id = wine.id
                WHERE bottle.consumed = 0
                GROUP BY label"""
    )
    fun getStockByNaming(): LiveData<List<NamingStat>>

    @Query(
        """SELECT COUNT (*) as count, naming as label
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id
                INNER JOIN wine ON wine_id = wine.id
                WHERE date BETWEEN :start AND :end AND type = 1 OR type = 3 GROUP BY label"""
    )
    fun getReplenishmentsByNaming(start: Long, end: Long): LiveData<List<NamingStat>>

    @Query(
        """SELECT COUNT (*) as count, naming as label
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id
                INNER JOIN wine ON wine_id = wine.id
                WHERE date BETWEEN :start AND :end AND type = 0 OR type = 2 GROUP BY label"""
    )
    fun getConsumptionsByNaming(start: Long, end: Long): LiveData<List<NamingStat>>

}

interface NewStat {
    val label: String
    val percentage: Float
}

data class BaseStat(override val label: String, override val percentage: Float) : NewStat

interface Stat {
    val count: Int
    var label: String
    var color: Int?
    val safeColor: Int
}

data class CountyStat(
    override val count: Int,
    override var label: String = "",
    @ColorRes override var color: Int?
) : Stat {
    @Ignore
    override val safeColor = color ?: ColorUtil.next()
}

data class ColorStat(
    override val count: Int,
    override var label: String = "",
    @ColorRes override var color: Int?
) : Stat {
    @Ignore
    override val safeColor = ColorUtil.getColorResForWineColor(color ?: 0)
}

data class VintageStat(
    override val count: Int,
    override var label: String = "",
    @ColorRes override var color: Int?
) : Stat {
    @Ignore
    override val safeColor = color ?: ColorUtil.next()
}

data class NamingStat(
    override val count: Int,
    override var label: String = "",
    @ColorRes override var color: Int?
) : Stat {
    @Ignore
    override val safeColor = color ?: ColorUtil.next()
}
