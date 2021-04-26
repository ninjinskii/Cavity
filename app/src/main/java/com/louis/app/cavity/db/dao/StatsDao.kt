package com.louis.app.cavity.db.dao

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
    suspend fun getStockByColor(): List<ColorStat>

    @Query(
        """SELECT COUNT (*) as count, color
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id 
                INNER JOIN wine ON wine_id = wine.id
                WHERE date BETWEEN :start AND :end AND type = 1 OR type = 3 GROUP BY color"""
    )
    fun getReplenishmentsByColor(start: Long, end: Long): List<ColorStat>

    @Query(
        """SELECT COUNT (*) as count, color
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id 
                INNER JOIN wine ON wine_id = wine.id
                WHERE date BETWEEN :start AND :end AND type = 0 OR type = 2 GROUP BY color"""
    )
    suspend fun getConsumptionsByColor(start: Long, end: Long): List<ColorStat>

    @Query(
        """SELECT COUNT (*) as count, vintage
                FROM bottle
                WHERE bottle.consumed = 0
                GROUP BY vintage"""
    )
    suspend fun getStockByVintage(): List<VintageStat>

    @Query(
        """SELECT COUNT (*) as count, vintage
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id
                WHERE date BETWEEN :start AND :end AND type = 1 OR type = 3 GROUP BY vintage"""
    )
    suspend fun getReplenishmentsByVintage(start: Long, end: Long): List<VintageStat>

    @Query(
        """SELECT COUNT (*) as count, vintage
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id
                WHERE date BETWEEN :start AND :end AND type = 0 OR type = 2 GROUP BY vintage"""
    )
    suspend fun getConsumptionsByVintage(start: Long, end: Long): List<VintageStat>


}

data class ColorStat(val count: Int, val color: Int)
data class VintageStat(val count: Int, val vintage: String)
data class StringStat(val count: Int, val stringValue: String)
