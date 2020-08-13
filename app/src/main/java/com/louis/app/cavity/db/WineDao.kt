package com.louis.app.cavity.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import com.louis.app.cavity.model.Wine

@Dao
interface WineDao {

    @Insert
    fun insertWine(wine: Wine)

    @Update
    fun updateWine(wine: Wine)

    @Delete
    fun deleteWine(wine: Wine)
}