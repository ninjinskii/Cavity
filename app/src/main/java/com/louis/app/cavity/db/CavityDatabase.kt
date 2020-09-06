package com.louis.app.cavity.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.louis.app.cavity.model.*
import kotlin.concurrent.thread

@Database(
    entities = [County::class, Wine::class, Bottle::class, Grape::class, ExpertAdvice::class],
    version = 15,
    exportSchema = false
)
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
                .addCallback(roomCallback)
                .fallbackToDestructiveMigration()
                .build()
        }

        private val roomCallback: Callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                thread {
                    val bottleDao = instance?.bottleDao()
                    val wineDao = instance?.wineDao()

                    wineDao?.insertWine(Wine(1, "a", "a", 1, "", 0, 0, ""))
                    wineDao?.insertWine(Wine(2, "a", "a", 1, "", 0, 0, ""))
                    wineDao?.insertWine(Wine(3, "a", "a", 1, "", 0, 0, ""))

                    bottleDao?.insertBottle(
                        Bottle(
                            0,
                            1,
                            0,
                            0,
                            1,
                            2010,
                            "",
                            0,
                            "",
                            ",",
                            "",
                            "",
                            "",
                            ""
                        )
                    )
                    bottleDao?.insertBottle(
                        Bottle(
                            0,
                            1,
                            0,
                            0,
                            1,
                            2010,
                            "",
                            0,
                            "",
                            ",",
                            "",
                            "",
                            "",
                            ""
                        )
                    )
                    bottleDao?.insertBottle(
                        Bottle(
                            0,
                            1,
                            0,
                            0,
                            1,
                            2011,
                            "",
                            0,
                            "",
                            ",",
                            "",
                            "",
                            "",
                            ""
                        )
                    )
                    bottleDao?.insertBottle(
                        Bottle(
                            0,
                            2,
                            0,
                            0,
                            1,
                            2012,
                            "",
                            0,
                            "",
                            ",",
                            "",
                            "",
                            "",
                            ""
                        )
                    )

                }
            }
        }
    }
}