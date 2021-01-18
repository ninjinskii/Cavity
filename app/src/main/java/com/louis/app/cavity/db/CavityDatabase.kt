package com.louis.app.cavity.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.louis.app.cavity.model.*
import com.louis.app.cavity.model.relation.FilledBottleReviewXRef
import com.louis.app.cavity.model.relation.QuantifiedBottleGrapeXRef
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
        QuantifiedBottleGrapeXRef::class,
        FilledBottleReviewXRef::class
    ],
    version = 28,
    exportSchema = false
)
abstract class CavityDatabase : RoomDatabase() {

    abstract fun wineDao(): WineDao
    abstract fun bottleDao(): BottleDao
    abstract fun countyDao(): CountyDao
    abstract fun grapeDao(): GrapeDao
    abstract fun qGrapeDao(): QuantifiedGrapeDao
    abstract fun reviewDao(): ReviewDao
    abstract fun fReviewDao(): FilledReviewDao

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
                val wineDao = instance?.wineDao()
                val countyDao = instance?.countyDao()

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

                    with(wineDao!!) {
                        insertWine(
                            Wine(
                                1,
                                "Immelé",
                                "Gerwurztraminer",
                                0,
                                "Saint Nicolas",
                                1,
                                0,
                                ""
                            )
                        )
                        insertWine(Wine(2, "Immelé", "Gerwurztraminer", 0, "", 1, 1, ""))
                        insertWine(
                            Wine(
                                3,
                                "Immelé",
                                "Pinot gris",
                                0,
                                "Vendanges Tardives",
                                1,
                                0,
                                ""
                            )
                        )
                        insertWine(Wine(4, "Immelé", "Pinot Noir", 1, "", 1, 0, ""))
                        insertWine(Wine(5, "Domaine F. Engel", "Gewurztraminer", 0, "", 1, 0, ""))
                        insertWine(Wine(6, "Domaine F. Engel", "Riesling", 0, "", 1, 0, ""))

                        insertWine(Wine(7, "Marc Dison", "Gevrey-Chambertin", 1, "", 2, 1, ""))
                        insertWine(
                            Wine(
                                8,
                                "Domaine Delience Frères",
                                "Clos de la Marole",
                                1,
                                "",
                                2,
                                1,
                                ""
                            )
                        )
                        insertWine(
                            Wine(
                                9,
                                "Domaine Masse Fabrice",
                                "Fa Brûlée",
                                1,
                                "Grand Cru Classé",
                                2,
                                0,
                                ""
                            )
                        )
                        insertWine(Wine(10, "Château de Santenay", "Mercurey", 1, "", 2, 0, ""))
                        insertWine(Wine(11, "Fes frères Muzards", "Santenay", 1, "VV", 2, 0, ""))

                        insertWine(Wine(12, "Savagnin", "Savagnin", 0, "", 5, 1, ""))
                        insertWine(Wine(13, "Vin du Jura", "Savagnin", 0, "", 5, 0, ""))

                        insertWine(Wine(14, "Domaine Fes Creisses", "Herault", 1, "", 4, 0, ""))
                        insertWine(
                            Wine(
                                15,
                                "Domaine Fes Creisses",
                                "Herault",
                                1,
                                "Fe Bastion",
                                4,
                                0,
                                ""
                            )
                        )
                        insertWine(Wine(16, "Domaine Des Fusils", "Herault", 1, "", 4, 1, ""))

                        insertWine(
                            Wine(
                                17,
                                "Mas Amiel pur schiste",
                                "Côte du Roussillon",
                                1,
                                "",
                                9,
                                0,
                                ""
                            )
                        )
                        insertWine(
                            Wine(
                                18,
                                "Domaine Bastide Miraflor",
                                "Côte du Roussillon",
                                1,
                                "",
                                9,
                                0,
                                ""
                            )
                        )

                        insertWine(Wine(19, "Domaine Fondrèche", "Ventoux", 1, "", 10, 1, ""))
                        insertWine(Wine(20, "Persia", "Ventoux", 1, "", 10, 1, ""))

                        insertWine(Wine(21, "Domaine de Marotte", "Foupiac", 2, "", 8, 0, ""))
                        insertWine(Wine(22, "Fe pradey", "Sainte Croix du Mont", 2, "", 8, 0, ""))
                        insertWine(Wine(23, "Château Delmond", "Sauternes", 2, "", 8, 0, ""))
                        insertWine(Wine(24, "Château la Raspide", "Sauternes", 2, "", 8, 1, ""))
                        insertWine(
                            Wine(
                                25,
                                "Château Fleur Haut Gaussens",
                                "Bordeau supérieur",
                                1,
                                "",
                                8,
                                0,
                                ""
                            )
                        )
                        insertWine(
                            Wine(
                                26,
                                "Fes tuileries de Fansac",
                                "Côte de Bourg",
                                1,
                                "",
                                8,
                                0,
                                ""
                            )
                        )
                        insertWine(Wine(27, "Château la Ouarde", "Graves", 1, "", 8, 0, ""))
                        insertWine(Wine(28, "Château la Carolle", "Graves", 1, "", 8, 0, ""))
                        insertWine(Wine(29, "Château de Fandiras", "Graves", 1, "", 8, 0, ""))

                        insertWine(
                            Wine(
                                30,
                                "Primitivo Rin Forzato Giordano",
                                "Fes Pouilles",
                                1,
                                "Amaretto passi",
                                7,
                                0,
                                ""
                            )
                        )
                        insertWine(
                            Wine(
                                31,
                                "Barolo le Terre",
                                "Toscane",
                                1,
                                "Amaretto passi",
                                7,
                                1,
                                ""
                            )
                        )
                        insertWine(
                            Wine(
                                32,
                                "Chianti classico Fa Palaia",
                                "Toscane",
                                1,
                                "",
                                7,
                                0,
                                ""
                            )
                        )
                        insertWine(Wine(33, "Ripasso Superieur Zeni", "Venetie", 1, "", 7, 0, ""))
                    }

                    with(bottleDao!!) {
                        insertBottle(
                            Bottle(
                                0,
                                1,
                                2014,
                                2024,
                                0,
                                1,
                                14F,
                                "",
                                "Vin succulent",
                                "Feclerc Fure",
                                -1,
                                "Notes amères",
                                "",
                            )
                        )
                        insertBottle(
                            Bottle(
                                0,
                                1,
                                2013,
                                2023,
                                1,
                                2,
                                14F,
                                "",
                                "Elu super vin dans un certain concours",
                                "",
                                -1,
                                "Doux",
                                "",
                            )
                        )
                        insertBottle(
                            Bottle(
                                0,
                                1,
                                2016,
                                2019,
                                0,
                                1,
                                25F,
                                "",
                                "",
                                "",
                                -1,
                                "Doux",
                                "",
                            )
                        )
                        insertBottle(Bottle(0, 2, 2017, 2019, 0, 1, 16F, "", "", "", -1, "", ""))

                        insertBottle(Bottle(0, 3, 2017, 2019, 0, 1, 16F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 4, 2009, 2013, 1, 1, 40F, "", "", "", -1, "", ""))
                        insertBottle(
                            Bottle(
                                0,
                                5,
                                2017,
                                2040,
                                0,
                                15,
                                16F,
                                "",
                                "",
                                "Cavavin",
                                -1,
                                "",
                                "",
                            )
                        )
                        insertBottle(Bottle(0, 6, 2018, 2019, 1, 1, 160F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 7, 2014, 2021, 0, 5, 16F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 8, 2016, 2030, 0, 1, 16F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 9, 2019, 2019, 0, 1, 16F, "", "", "", -1, "", ""))
                        insertBottle(
                            Bottle(
                                0,
                                10,
                                2020,
                                2030,
                                0,
                                1,
                                16F,
                                "",
                                "",
                                "Foire aux vins Fure",
                                -1,
                                "",
                                "",
                            )
                        )
                        insertBottle(
                            Bottle(
                                0,
                                10,
                                2019,
                                2031,
                                1,
                                1,
                                18F,
                                "",
                                "",
                                "Foire aux vins Fure",
                                -1,
                                "",
                                "",
                            )
                        )
                        insertBottle(Bottle(0, 11, 2005, 2019, 0, 5, 16F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 12, 2016, 2028, 1, 1, 15F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 13, 2019, 2023, 0, 1, 30F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 14, 2017, 2019, 0, 1, 7F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 15, 2014, 2023, 0, 1, 3F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 15, 2015, 2020, 1, 2, 3F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 16, 2013, 2019, 0, 40, 0F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 17, 2012, 2019, 0, 1, -1F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 18, 2011, 2023, 0, 1, 15F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 19, 2010, 2023, 0, 1, -1F, "", "", "", -1, "", ""))

                        insertBottle(Bottle(0, 20, 2011, 2019, 0, 2, 15F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 21, 2016, 2019, 0, 1, -1F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 22, 2012, 2025, 0, 1, 400F, "", "", "", -1, "", ""))
                        insertBottle(
                            Bottle(
                                0,
                                23,
                                2015,
                                2019,
                                1,
                                1,
                                1000F,
                                "",
                                "",
                                "Intermarché Belfor",
                                -1,
                                "",
                                "",
                            )
                        )
                        insertBottle(
                            Bottle(
                                0,
                                23,
                                2014,
                                2016,
                                1,
                                1,
                                20F,
                                "",
                                "",
                                "Un autre Intermarché",
                                -1,
                                "",
                                "",
                            )
                        )
                        insertBottle(Bottle(0, 24, 2018, 2021, 0, 1, 50F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 25, 2013, 2023, 0, 1, 63F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 26, 2014, 2019, 1, 1, 5F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 26, 2015, 2025, 1, 1, 5F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 27, 2019, 2024, 0, 1, 8F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 28, 2010, 2019, 0, 1, -1F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 29, 2011, 2019, 0, 1, 40F, "", "", "", -1, "", ""))

                        insertBottle(
                            Bottle(
                                0,
                                30,
                                2011,
                                2019,
                                0,
                                1,
                                30F,
                                "",
                                "Aîe",
                                "",
                                -1,
                                "Paille",
                                "",
                            )
                        )
                        insertBottle(Bottle(0, 31, 2016, 2021, 0, 1, 40F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 32, 2014, 2019, 0, 1, 20F, "", "", "", -1, "", ""))
                        insertBottle(Bottle(0, 32, 2016, 2019, 0, 1, 20F, "", "", "", -1, "", ""))
                        insertBottle(
                            Bottle(
                                0,
                                33,
                                2015,
                                2022,
                                0,
                                1,
                                7F,
                                "",
                                "",
                                "",
                                -1,
                                "Piquette",
                                "",
                            )
                        )
                    }
                }
            }
        }
    }
}
