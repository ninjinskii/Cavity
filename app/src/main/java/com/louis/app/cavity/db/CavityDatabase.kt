package com.louis.app.cavity.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Wine

@Database(entities = [County::class, Wine::class], version = 1, exportSchema = false)
abstract class CavityDatabase : RoomDatabase() {

    abstract fun wineDao(): WineDao

    companion object {
        @Volatile
        private var instance: CavityDatabase? = null

        fun getInstance(context: Context): CavityDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): CavityDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                CavityDatabase::class.java,
                "cavity.db"
            ).build()
        }
    }
}