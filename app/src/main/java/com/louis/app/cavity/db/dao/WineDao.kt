package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.model.WineColor
import kotlinx.coroutines.flow.Flow

@Dao
interface WineDao {
    @Insert
    suspend fun insertWine(wine: Wine): Long

    @Insert
    suspend fun insertWines(wine: List<Wine>)

    @Update
    suspend fun updateWine(wine: Wine)

    @Delete
    suspend fun deleteWine(wine: Wine)

    @Query("UPDATE wine SET hidden = 1 WHERE id =:wineId")
    suspend fun hideWineById(wineId: Long)

    @Query("DELETE FROM wine WHERE id =:wineId")
    suspend fun deleteWineById(wineId: Long)

    @Query("SELECT * FROM wine WHERE id =:wineId")
    fun getWineById(wineId: Long): LiveData<Wine>

    @Query("SELECT DISTINCT naming FROM wine WHERE county_id =:countyId ORDER BY naming")
    fun getNamingsForCounty(countyId: Long): LiveData<List<String>>

    @Query("SELECT * FROM wine WHERE color =:color AND is_organic =:isOrganic AND cuvee =:cuvee")
    suspend fun getWineByAttributes(color: WineColor, isOrganic: Int, cuvee: String): List<Wine>

    @Transaction
    @Query("SELECT * FROM wine WHERE id =:wineId")
    suspend fun getWineByIdNotLive(wineId: Long): Wine

    @Query("SELECT * FROM wine")
    suspend fun getAllWinesNotLive(): List<Wine>

    @Transaction
    @Query("SELECT * FROM wine WHERE id =:wineId")
    suspend fun getWineFullNamingByIdNotLive(wineId: Long): Wine

    @Transaction
    @Query("""
        SELECT w.*, 
               (SELECT COUNT(*) 
                FROM bottle b 
                WHERE b.wine_id = w.id AND b.consumed = 0) AS remainingBottles
        FROM wine w
        WHERE w.county_id = :countyId
        AND w.hidden != 1
        ORDER BY CASE w.color
            WHEN 'RED' THEN 0
            WHEN 'WHITE' THEN 1
            WHEN 'SWEET' THEN 2
            WHEN 'ROSE' THEN 3
        END, w.naming
    """)
    fun getWinesWithBottlesByCounty(countyId: Long): LiveData<List<WineWithBottles>>

    @Query("DELETE FROM wine")
    suspend fun deleteAll()
}

data class WineWithBottles(
    @Embedded val wine: Wine,
    @Relation(
        parentColumn = "id",
        entityColumn = "wine_id"
    )
    val bottles: List<Bottle>,
    val remainingBottles: Int
)
