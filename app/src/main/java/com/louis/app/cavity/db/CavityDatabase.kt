package com.louis.app.cavity.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.louis.app.cavity.model.*
import com.louis.app.cavity.model.relation.crossref.FilledBottleReviewXRef
import com.louis.app.cavity.model.relation.crossref.FriendHistoryEntryXRef
import com.louis.app.cavity.model.relation.crossref.QuantifiedBottleGrapeXRef
import com.louis.app.cavity.model.relation.crossref.TastingFriendXRef
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        County::class,
        Naming::class,
        Wine::class,
        Bottle::class,
        Grape::class,
        Review::class,
        QuantifiedBottleGrapeXRef::class,
        FilledBottleReviewXRef::class,
        HistoryEntry::class,
        Friend::class,
        FriendHistoryEntryXRef::class,
        Tasting::class,
        TastingFriendXRef::class,
    ],
    version = 48,
    exportSchema = false
)
abstract class CavityDatabase : RoomDatabase() {
    abstract fun countyDao(): CountyDao
    abstract fun namingDao(): NamingDao
    abstract fun wineDao(): WineDao
    abstract fun bottleDao(): BottleDao
    abstract fun grapeDao(): GrapeDao
    abstract fun qGrapeDao(): QuantifiedGrapeDao
    abstract fun reviewDao(): ReviewDao
    abstract fun fReviewDao(): FilledReviewDao
    abstract fun historyDao(): HistoryDao
    abstract fun friendDao(): FriendDao
    abstract fun tastingDao(): TastingDao
    abstract fun tastingXFriendDao(): TastingFriendXRefDao
    abstract fun historyXFriendDao(): FriendHistoryEntryXRefDao

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
                val bottleDao = instance?.bottleDao()
                val namingDao = instance?.namingDao()
                val wineDao = instance?.wineDao()
                val countyDao = instance?.countyDao()
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

                    val counties = 0..9
                    val namings = 0..10

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

                    val wineColors = 0..3

                    val friends = arrayOf(
                        "Jean",
                        "Lacour",
                        "Hervé",
                        "Simon",
                        "Clarinette"
                    )

                    repeat(10) {
                        namingDao!!.insertNaming(Naming(naming = wineNamings[it], countyId = counties.random().toLong()))
                    }

                    repeat(50) {
                        wineDao!!.insertWine(
                            Wine(0, wineNames.random(), wineColors.random(), "", (0..1).random(), "", namings.random().toLong())
                        )
                    }

                    repeat(150) {
                        try {
                            bottleDao!!.insertBottle(
                                Bottle(
                                    0,
                                    (0..50).random().toLong(),
                                    "20${(10..21).random()}".toInt(),
                                    "20${(21..35).random()}".toInt(),
                                    (0..1).random(),
                                    1,
                                    (0..300).random().toFloat(),
                                    "€",
                                    "",
                                    buyLocations.random(),
                                    (1486149968..System.currentTimeMillis()).random(),
                                    "",
                                    "",
                                    0
                                )
                            )
                        } catch (e: Exception) {
                            // Do nothing
                        }
                    }

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

                    repeat(300) {
                        try {
                            qGrapeDao!!.insertQuantifiedGrape(
                                QuantifiedBottleGrapeXRef(
                                    (0..70).random().toLong(),
                                    (1..4).random().toLong(),
                                    (5..15).random()
                                )
                            )
                        } catch (e: Exception) {
                            // Do nothing
                        }

                        try {
                            fReviewDao!!.insertFilledReview(
                                FilledBottleReviewXRef(
                                    (0..70).random().toLong(),
                                    (1..4).random().toLong(),
                                    (0..2).random()
                                )
                            )
                        } catch (e: Exception) {
                            // Do nothing
                        }
                    }

                    repeat(8) {
                        friendDao!!.insertFriend(
                            Friend(
                                0,
                                "${friends.random()} ${friends.random()}",
                                ""
                            )
                        )
                    }
                }
            }
        }
    }
}
