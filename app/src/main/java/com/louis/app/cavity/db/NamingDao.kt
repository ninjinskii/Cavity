package com.louis.app.cavity.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import com.louis.app.cavity.model.Naming

@Dao
interface NamingDao {
    @Insert
    suspend fun insertNaming(naming: Naming)

    @Update
    suspend fun updateNaming(naming: Naming)

    @Delete
    fun deleteNaming(naming: Naming)
}
