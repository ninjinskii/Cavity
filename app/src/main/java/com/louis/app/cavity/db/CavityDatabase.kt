package com.louis.app.cavity.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.louis.app.cavity.db.dao.*
import com.louis.app.cavity.model.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
    version = 55,
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
                .fallbackToDestructiveMigration()
                .addCallback(callback)
                .build()
        }

        private val callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val countyDao = instance?.countyDao()
                val wineDao = instance?.wineDao()
                val bottleDao = instance?.bottleDao()
                val grapeDao = instance?.grapeDao()
                val qGrapeDao = instance?.qGrapeDao()
                val reviewDao = instance?.reviewDao()
                val fReviewDao = instance?.fReviewDao()
                val historyDao = instance?.historyDao()
                val friendDao = instance?.friendDao()
                val tastingDao = instance?.tastingDao()
                val tastingFriendXRefDao = instance?.tastingXFriendDao()
                val friendHistoryEntryXRef = instance?.historyXFriendDao()

                GlobalScope.launch(IO) {
                    with(countyDao!!) {
                        insertCounty(County(1, "Alsace", 0))
                        insertCounty(County(2, "Bourgogne", 1))
                        insertCounty(County(3, "Beaujolais", 2))
                        insertCounty(County(4, "Fanguedoc", 3))
                        insertCounty(County(5, "Jura", 4))
                        insertCounty(County(6, "Suisse", 5))
                        insertCounty(County(7, "Italie", 6))
                        insertCounty(County(8, "Bordeaux", 7))
                        insertCounty(County(9, "Roussillion", 8))
                        insertCounty(County(10, "Vallée du Rhône", 9))
                    }

                    val counties = 1..10
                    val bottles = 1..29999

                    val wineNames = arrayOf(
                        "Immelé",
                        "Domaine F. Engel",
                        "Château la cour",
                        "Domaine des fusils",
                        "Ripasso",
                        "Domaine déliance frères",
                        "Domaine Masse Fabrice",
                        "Château Fleur Haut Gaussens",
                        "Château Delmond",
                        "Primitivo Rin Forzato Giordano"
                    )

                    val wineNamings = arrayOf(
                        "Graves",
                        "Gewurztraminer",
                        "Riesling",
                        "Amarone",
                        "Côte du Roussillon",
                        "Pinot gris",
                        "Clos de la Marolle",
                        "Bordeau supérieur",
                        "Côte de Bourg",
                        "Venetie",
                        "Savagnin"
                    )

                    val buyLocations = arrayOf(
                        "Leclerc Lure",
                        "Intermarché Lure",
                        "Fav Auchan",
                        "Lidl Héricourt",
                        "Qoqa",
                        "Cavavin"
                    )

                    val wineColors =
                        listOf(WineColor.RED, WineColor.WHITE, WineColor.ROSE, WineColor.SWEET)

                    val friends = arrayOf(
                        "Jean",
                        "Lacour",
                        "Hervé",
                        "Simon",
                        "Clarinette"
                    )

                    val wines = List(100) {
                        Wine(
                            0,
                            wineNames.random(),
                            wineNamings.random(),
                            wineColors.random(),
                            "",
                            (0..1).random(),
                            "",
                            counties.random().toLong(),
                        )
                    }

                    wineDao!!.insertWines(wines)

                    val bottlesList = List(30000) {
                        Bottle(
                            0,
                            (1..50).random().toLong(),
                            "20${(10..21).random()}".toInt(),
                            "20${(21..35).random()}".toInt(),
                            (0..1).random(),
                            1,
                            price = bottles.random().toFloat(),
                            "€",
                            "",
                            buyLocations.random(),
                            (1486149968..System.currentTimeMillis()).random(),
                            "",
                            BottleSize.NORMAL,
                            "",
                            0
                        )
                    }

                    bottleDao!!.insertBottles(bottlesList)

                    with(grapeDao!!) {
                        insertGrape(Grape(0, "Syrah"))
                        insertGrape(Grape(0, "Cabernet"))
                        insertGrape(Grape(0, "Sauvignon"))
                        insertGrape(Grape(0, "Pinot noir"))
                    }

                    with(reviewDao!!) {
                        insertReview(Review(0, "Parker", 0))
                        insertReview(Review(0, "Vignerons indépendants", 1))
                        insertReview(Review(0, "James Suckling", 2))
                        insertReview(Review(0, "Les étoiles", 3))
                    }

                    repeat(500) {
                        try {
                            qGrapeDao!!.insertQGrape(
                                QGrape(
                                    bottles.random().toLong(),
                                    (1..4).random().toLong(),
                                    (5..15).random()
                                )
                            )
                        } catch (e: Exception) {
                            // Do nothing
                        }

                        try {
                            fReviewDao!!.insertFReview(
                                FReview(
                                    bottles.random().toLong(),
                                    (1..4).random().toLong(),
                                    (0..2).random()
                                )
                            )
                        } catch (e: Exception) {
                            // Do nothing
                        }
                    }

                    repeat(8) {
                        try {
                            friendDao!!.insertFriend(
                                Friend(
                                    0,
                                    "${friends.random()} ${friends.random()}",
                                    ""
                                )
                            )
                        } catch (e: Exception) {
                        }
                    }

                    with(historyDao!!) {
                        val historyBottles = 300L..400L
                        val twenyone = 1609459200000..System.currentTimeMillis()
                        val tweny = 1577836800000L..1609459199000L
                        val historyBottlesLastYear = 401L..29999L
                        val types = listOf(0, 0, 0, 1, 1, 1, 2, 3)

                        val entries2021 = List(99) {
                            val type = types.random()
                            HistoryEntry(
                                0,
                                twenyone.random(),
                                it.toLong() + 1,
                                null,
                                "Commentaire",
                                type,
                                0
                            )
                        }

                        insertEntry(entries2021)

                        val otherEntries = List(20000) {
                            val type = types.random()
                            HistoryEntry(
                                0,
                                tweny.random(),
                                it.toLong() + 110,
                                null,
                                "Commentaire",
                                type,
                                0
                            )
                        }

                        insertEntry(otherEntries)
                    }

                    with(tastingDao!!) {
                        val opportunities =
                            listOf(
                                "Anniversaire Jean",
                                "Pot de départ Guy",
                                "Fête",
                                "Fête des voisins"
                            )
                        val time = System.currentTimeMillis()
                        val tastings = List(10) {
                            Tasting(0, time + 100000, opportunities.random(), 15, 15, 15, 15)
                        }

                        val tastingsXFriends = listOf(
                            TastingXFriend(1, 1),
                            TastingXFriend(1, 2),
                            TastingXFriend(1, 3),
                            TastingXFriend(1, 4),
                            TastingXFriend(1, 5),
                            TastingXFriend(2, 2)
                        )

                        tastings.forEach {
                            insertTasting(it)
                        }

                        tastingsXFriends.forEach {
                            tastingFriendXRefDao?.insertTastingXFriend(it)
                        }
                    }
                }
            }
        }
    }
}
