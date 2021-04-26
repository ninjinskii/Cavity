package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface StatsDao {
    @Query(
        """SELECT COUNT (*) as count, color
                FROM bottle
                INNER JOIN wine ON wine_id = wine.id
                WHERE bottle.consumed = 0
                GROUP BY color"""
    )
    fun getStockByColor(): LiveData<List<ColorStat>>

    @Query(
        """SELECT COUNT (*) as count, color
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id 
                INNER JOIN wine ON wine_id = wine.id
                WHERE date BETWEEN :start AND :end AND type = 1 OR type = 3 GROUP BY color"""
    )
    fun getReplenishmentsByColor(start: Long, end: Long): LiveData<List<ColorStat>>

    @Query(
        """SELECT COUNT (*) as count, color
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id 
                INNER JOIN wine ON wine_id = wine.id
                WHERE date BETWEEN :start AND :end AND type = 0 OR type = 2 GROUP BY color"""
    )
    fun getConsumptionsByColor(start: Long, end: Long): LiveData<List<ColorStat>>

    @Query(
        """SELECT COUNT (*) as count, vintage
                FROM bottle
                WHERE bottle.consumed = 0
                GROUP BY vintage"""
    )
    fun getStockByVintage(): LiveData<List<VintageStat>>

    @Query(
        """SELECT COUNT (*) as count, vintage
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id
                WHERE date BETWEEN :start AND :end AND type = 1 OR type = 3 GROUP BY vintage"""
    )
    fun getReplenishmentsByVintage(start: Long, end: Long): LiveData<List<VintageStat>>

    @Query(
        """SELECT COUNT (*) as count, vintage
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id
                WHERE date BETWEEN :start AND :end AND type = 0 OR type = 2 GROUP BY vintage"""
    )
    fun getConsumptionsByVintage(start: Long, end: Long): LiveData<List<VintageStat>>


}

data class ColorStat(val count: Int, val color: Int)
data class VintageStat(val count: Int, val vintage: String)
