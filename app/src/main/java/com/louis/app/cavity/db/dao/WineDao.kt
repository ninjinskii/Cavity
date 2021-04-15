package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Naming
import com.louis.app.cavity.model.Wine

@Dao
interface WineDao {
    @Insert
    suspend fun insertWine(wine: Wine)

    @Update
    suspend fun updateWine(wine: Wine)

    @Delete
    suspend fun deleteWine(wine: Wine)

    @Query("DELETE FROM wine WHERE id =:wineId")
    suspend fun deleteWineById(wineId: Long)

    @Query("SELECT * FROM wine WHERE id =:wineId")
    fun getWineById(wineId: Long): LiveData<Wine>

    @Transaction
    @Query("SELECT * FROM wine WHERE id =:wineId")
    suspend fun getWineByIdNotLive(wineId: Long): WineAndNaming

    @Transaction
    @Query("SELECT * FROM wine WHERE id =:wineId")
    suspend fun getWineFullNamingByIdNotLive(wineId: Long): WineAndFullNaming

    @Transaction
    @Query("SELECT * FROM wine WHERE county_id =:countyId ORDER BY color, naming_id")
    fun getWineWithBottlesByCounty(countyId: Long): LiveData<List<WineWithBottles>>

//    @Transaction
//    @Query("SELECT * FROM wine INNER JOIN bottle ON wine.id = bottle.wine_id WHERE county_id =:countyId AND consumed = 0 ORDER BY color, naming")
//    fun getWineWithBottlesByCounty(countyId: Long): LiveData<List<WineWithBottles>>
}

data class WineAndNaming(
    @Embedded val wine: Wine,
    @Relation(
        entity = Naming::class,
        parentColumn = "naming_id",
        entityColumn = "id",
        projection = ["naming"]
    )
    val naming: String
)

data class WineAndFullNaming(
    @Embedded val wine: Wine,
    @Relation(
        parentColumn = "naming_id",
        entityColumn = "id",
    )
    val naming: Naming
)

data class WineWithBottles(
    @Embedded val wineAndNaming: WineAndNaming,
    @Relation(
        parentColumn = "id",
        entityColumn = "wine_id"
    )
    val bottles: List<Bottle>
)
