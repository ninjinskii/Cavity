package com.louis.app.cavity.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Wine
import kotlin.concurrent.thread

@Database(entities = [County::class, Wine::class, Bottle::class], version = 3, exportSchema = false)
abstract class CavityDatabase : RoomDatabase() {

    abstract fun wineDao(): WineDao
    abstract fun bottleDao(): BottleDao

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
            )
                //.addCallback(roomCallback)
                .fallbackToDestructiveMigration()
                .build()
        }

        private val roomCallback: Callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                thread {
                    val vinDao = instance?.wineDao()

                    for (x in 0..5) {
                        vinDao?.insertWine(
                            Wine(
                                "Château la cour$x",
                                "Château-neuf du Pape",
                                1,
                                0,
                                1,
                                ""
                            )
                        )
                    }

                    for (x in 6..10) {
                        vinDao?.insertWine(
                            Wine(
                                "Château la cour$x",
                                "Château-neuf du Pape",
                                2,
                                0,
                                0,
                                ""
                            )
                        )
                    }

                    for (x in 11..15) {
                        vinDao?.insertWine(
                            Wine(
                                "Château la cour$x",
                                "Château-neuf du Pape",
                                3,
                                0,
                                1,
                                ""
                            )
                        )
                    }
                }
            }
        }
    }
}