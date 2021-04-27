package com.louis.app.cavity.db.dao

import android.content.Context
import android.content.res.Resources
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.louis.app.cavity.util.ColorUtil

@Dao
interface StatsDao {
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

interface Stat {
    val count: Int
    var label: String
    var color: Int?

    // Used to get string / color from R resources
    fun resolve(context: Context?): Stat = this
}

data class CountyStat(
    override val count: Int,
    override var label: String = "",
    override var color: Int? = null
) : Stat

data class ColorStat(
    override val count: Int,
    override var color: Int?,
    override var label: String = "",
) : Stat {
    override fun resolve(context: Context?): Stat {
        color?.let {
            label = context?.getString(ColorUtil.getStringResForWineColor(it)) ?: ""
            color = context?.let { ctx ->
                try {
                    ContextCompat.getColor(ctx, ColorUtil.getColorResForWineColor(it))
                } catch (e: Resources.NotFoundException) {
                    null
                }
            }
        }

        return this
    }
}

data class VintageStat(
    override val count: Int,
    override var label: String = "",
    override var color: Int? = null
) : Stat

data class NamingStat(
    override val count: Int,
    override var label: String = "",
    override var color: Int? = null
) : Stat
