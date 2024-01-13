package com.louis.app.cavity.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        Tasting::class,
        TastingXFriend::class,
        HistoryXFriend::class,
        TastingAction::class,
    ],
    version = 2,
    exportSchema = false
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

    companion object {
        @Volatile
        private var instance: CavityDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.beginTransaction()
                    database.execSQL(
                        "CREATE TABLE `bottle_temp` " +
                            "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                            " `wine_id` INTEGER NOT NULL," +
                            " `vintage` INTEGER NOT NULL," +
                            " `apogee` INTEGER," +
                            " `is_favorite` INTEGER NOT NULL," +
                            " `price` REAL NOT NULL," +
                            " `currency` TEXT NOT NULL," +
                            " `other_info` TEXT NOT NULL," +
                            " `buy_location` TEXT NOT NULL," +
                            " `buy_date` INTEGER NOT NULL," +
                            " `tasting_taste_comment` TEXT NOT NULL," +
                            " `bottle_size` TEXT NOT NULL," +
                            " `pdf_path` TEXT NOT NULL," +
                            " `consumed` INTEGER NOT NULL," +
                            " `tasting_id` INTEGER," +
                            " FOREIGN KEY(`wine_id`) REFERENCES `wine`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE );"
                    )

                    database.execSQL("CREATE INDEX index_bottle_wine_id ON bottle_temp(wine_id ASC);")

                    database.execSQL("CREATE INDEX index_bottle_tasting_id ON bottle_temp(tasting_id ASC);")

                    database.execSQL("INSERT INTO bottle_temp(id, wine_id, vintage, apogee, is_favorite, price, currency, other_info, buy_location, buy_date, tasting_taste_comment, bottle_size, pdf_path, consumed, tasting_id) SELECT * FROM bottle;")

                    database.execSQL("DROP TABLE bottle;")

                    database.execSQL("ALTER TABLE bottle_temp RENAME TO bottle;")
                } finally {
                    database.endTransaction()
                }
            }
        }

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
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}
