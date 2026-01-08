package com.louis.app.cavity.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.louis.app.cavity.db.dao.*
import com.louis.app.cavity.model.*

@Database(
    entities = [
        County::class,
        Wine::class,
        Bottle::class,
        Grape::class,
        Review::class,
        QGrape::class,
        FReview::class,
        HistoryEntry::class,
        Friend::class,
        Tag::class,
        Tasting::class,
        TastingXFriend::class,
        HistoryXFriend::class,
        TastingAction::class,
        TagXBottle::class
    ],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
    ]

)
abstract class CavityDatabase : RoomDatabase() {
    abstract fun countyDao(): CountyDao
    abstract fun wineDao(): WineDao
    abstract fun bottleDao(): BottleDao
    abstract fun grapeDao(): GrapeDao
    abstract fun qGrapeDao(): QuantifiedGrapeDao
    abstract fun reviewDao(): ReviewDao
    abstract fun fReviewDao(): FilledReviewDao
    abstract fun historyDao(): HistoryDao
    abstract fun historyXFriendDao(): HistoryXFriendDao
    abstract fun friendDao(): FriendDao
    abstract fun statsDao(): StatsDao
    abstract fun tastingDao(): TastingDao
    abstract fun tastingXFriendDao(): TastingXFriendDao
    abstract fun tastingActionDao(): TastingActionDao
    abstract fun tagDao(): TagDao
    abstract fun tagXBottleDao(): TagXBottleDao

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
                .build()
        }
    }
}
