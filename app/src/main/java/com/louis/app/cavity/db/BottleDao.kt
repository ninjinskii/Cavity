package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Bottle

@Dao
interface BottleDao {

    @Insert
    fun insertBottle(bottle: Bottle)

    @Update
    fun updateBottle(bottle: Bottle)

    @Delete
    fun deleteBottle(bottle: Bottle)

    @Query("SELECT * FROM bottle")
    fun getAllBottles(): LiveData<List<Bottle>>
}