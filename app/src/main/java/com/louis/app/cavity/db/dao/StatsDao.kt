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
    suspend fun getStockByColor(): List<IntStat>

    @Query(
        """SELECT COUNT (*) as count, color
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id 
                INNER JOIN wine ON wine_id = wine.id
                WHERE type = 1 OR type = 3 AND date BETWEEN :start AND :end GROUP BY color"""
    )
    suspend fun getReplenishmentsByColor(start: Long, end: Long): List<IntStat>

    @Query(
        """SELECT COUNT (*) as count, color
                FROM history_entry
                INNER JOIN bottle ON bottle_id = bottle.id 
                INNER JOIN wine ON wine_id = wine.id
                WHERE type = 0 OR type = 2 AND date BETWEEN :start AND :end GROUP BY color"""
    )
    suspend fun getConsumptionsByColor(start: Long, end: Long): List<IntStat>


}

data class IntStat(val count: Int, val color: Int)
data class StringStat(val count: Int, val stringValue: String)
