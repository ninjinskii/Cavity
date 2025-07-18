import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteTransactionListener
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.louis.app.cavity.db.CavityDatabase
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    @Rule
    @JvmField
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        CavityDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun migration_1_2() {
        // Create earliest version of the database.
        helper.createDatabase(TEST_DB, 1).apply {
            popualateDbWithMinimalData(this)

            Assert.assertThrows(SQLiteConstraintException::class.java) {
                // Should not be able to insert bottle with 'null' for 'apogee' before migration
                execSQL("INSERT INTO bottle VALUES (4, 1, 2020, null, 0, 0, '€', '', '', 0, '', 'NORMAL', '', 0, null);")
            }

            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 2, true).apply {
            val countBeforeInserts = query("SELECT * FROM bottle;").count

            use {
                beginTransactionWithListener(object : SQLiteTransactionListener {
                    override fun onBegin() {
                        // Should be able to insert bottle with 'null' for 'apogee' after migration
                        execSQL("INSERT INTO bottle VALUES (4, 1, 2020, null, 0, 0, '€', '', '', 0, '', 'NORMAL', '', 0, null);")
                    }

                    override fun onCommit() {
                        val countAfterInserts = query("SELECT * FROM bottle;").count
                        setTransactionSuccessful()
                        Assert.assertEquals(countBeforeInserts, countAfterInserts)
                    }

                    override fun onRollback() {
                        throw Exception("Transaction failed")
                    }
                })
            }
        }
    }

    @Test
    @Throws(IOException::class)
    fun migration_2_3() {
        // Create earliest version of the database.
        val db = helper.createDatabase(TEST_DB, 2).apply {
            popualateDbWithMinimalData(this)

            Assert.assertThrows(SQLiteException::class.java) {
                // Should not be able to insert bottle bottle with new fields storage_location & alcohol before migration
                execSQL("INSERT INTO bottle VALUES (4, 1, 2020, null, 0, 0, '€', '', '', 0, '', 'NORMAL', '', 'cellar', 12.5, 0, null);")
            }

//            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 3, true).apply {
            val countBeforeInserts = query("SELECT * FROM bottle;").count
            beginTransactionWithListener(object : SQLiteTransactionListener {
                override fun onBegin() {
                    // Should be able to insert bottle with new fields storage_location & alcohol
                    execSQL("INSERT INTO bottle VALUES (4, 1, 2020, null, 0, 0, '€', '', '', 0, '', 'NORMAL', '', 'cellar', 12.5, 0, null);")
                }

                override fun onCommit() {
                    val countAfterInserts = query("SELECT * FROM bottle;").count
                    setTransactionSuccessful()

                    Assert.assertEquals(countBeforeInserts, countAfterInserts)

                    // Pre existing bottles should have null as alcohol value & empty string as storage_location
                    val result =
                        db.query("SELECT alcohol, storage_location FROM bottle WHERE id = 1")
                    if (result.moveToFirst()) {
                        val alcohol = result.getDouble(0)
                        val storageLocation = result.getString(1)

                        Assert.assertEquals(alcohol, 12.5, 0.0)
                        Assert.assertEquals(storageLocation, "cellar")
                    } else {
                        throw Exception("result.moveToFirst() should be truthy")
                    }
                }

                override fun onRollback() {
                    throw Exception("Transaction failed")
                }
            })

        }

        db.close()
    }

    companion object {
        private const val TEST_DB = "migration-test"

        private fun popualateDbWithMinimalData(db: SupportSQLiteDatabase) {
            db.apply {
                execSQL("INSERT INTO county VALUES (1, 'Alsace', '0');")
                execSQL("INSERT INTO county VALUES (2, 'Bourgogne', '1');")

                execSQL("INSERT INTO wine VALUES (1, 'Domaine immelé', 'Riesling', 'WHITE', '', 0, '', 1, 1);")            // Associated with county 1
                execSQL("INSERT INTO wine VALUES (2, 'Domaine immelé', 'Pinot Noir', 'RED', '', 0, '', 1, 0);")            // Associated with county 1
                execSQL("INSERT INTO wine VALUES (3, 'Alainn Guyard', 'Gevrey-chambertin', 'RED', '', 0, '', 2, 0);")      // Associated with county 2

                execSQL("INSERT INTO tasting VALUES (1, 0, 'true', 'Anniversaire Guy', 'false');")

                execSQL("INSERT INTO bottle VALUES (1, 1, 2020, 2024, 0, 0, '€', '', '', 0, '', 'NORMAL', '', 0, null);")
                execSQL("INSERT INTO bottle VALUES (2, 2, 2021, 2025, 0, 0, '€', '', '', 0, '', 'NORMAL', '', 0, null);")
                execSQL("INSERT INTO bottle VALUES (3, 3, 2022, 2026, 0, 0, '€', '', '', 0, '', 'NORMAL', '', 0, 1);")     // Associated with tasting

                execSQL("INSERT INTO grape VALUES (1, 'Merlot');")

                execSQL("INSERT INTO review VALUES (1, 'Concours bordeau', 0);")

                execSQL("INSERT INTO q_grape VALUES (1, 1, 50);")                                                       // Associated vith bottle 1 & grape 1

                execSQL("INSERT INTO f_review VALUES (2, 1, 2);")                                                       // Associated vith bottle 2 & review 1

                execSQL("INSERT INTO tasting_action VALUES (1, 'SET_TO_JUG', 3, 0);")                                      // Associated with bottle 3

                execSQL("INSERT INTO friend VALUES (1, 'Someone', '');")

                execSQL("INSERT INTO history_entry VALUES (1, 0, 1, null, '', 1, 0);")                                     // Associated with bottle 1 - replenishment
                execSQL("INSERT INTO history_entry VALUES (2, 0, 1, null, '', 0, 0);")                                     // Associated with bottle 1 - consumption
                execSQL("INSERT INTO history_entry VALUES (3, 0, 1, 1, '', 0, 4);")                                        // Associated with bottle 2 & tasting 1 - consumption

                execSQL("INSERT INTO friend_history_entry_xref VALUES (1, 2);")                                             // Associated with friend 1 & entry 2
            }
        }
    }
}
