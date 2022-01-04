package com.louis.app.cavity.db

import android.app.Application
import androidx.room.withTransaction
import com.louis.app.cavity.model.*
import com.louis.app.cavity.util.L
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.*

class WineRepository private constructor(app: Application) {
    companion object {
        @Volatile
        var instance: WineRepository? = null

        fun getInstance(app: Application) =
            instance ?: synchronized(this) {
                instance ?: WineRepository(app).also { instance = it }
            }
    }

    private val database = CavityDatabase.getInstance(app)

    private val wineDao = database.wineDao()
    private val bottleDao = database.bottleDao()
    private val countyDao = database.countyDao()
    private val grapeDao = database.grapeDao()
    private val qGrapeDao = database.qGrapeDao()
    private val reviewDao = database.reviewDao()
    private val fReviewDao = database.fReviewDao()
    private val historyDao = database.historyDao()
    private val friendDao = database.friendDao()
    private val historyXFriendDao = database.historyXFriendDao()
    private val tastingXFriendDao = database.tastingXFriendDao()
    private val statsDao = database.statsDao()
    private val tastingDao = database.tastingDao()
    private val tastingActionDao = database.tastingActionDao()

    // Only used for db migration from Cavity 2 for now.
    private val moshi by lazy {
        Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    }


    // Wine
    suspend fun insertWine(wine: Wine) = wineDao.insertWine(wine)
    suspend fun insertWines(wines: List<Wine>) = wineDao.insertWines(wines)
    suspend fun updateWine(wine: Wine) = wineDao.updateWine(wine)
    suspend fun deleteWine(wine: Wine) = wineDao.deleteWine(wine)
    suspend fun deleteWineById(wineId: Long) = wineDao.deleteWineById(wineId)
    fun getWineById(wineId: Long) = wineDao.getWineById(wineId)
    suspend fun getWineByIdNotLive(wineId: Long) = wineDao.getWineByIdNotLive(wineId)
    suspend fun getWineFullNamingByIdNotLive(wineId: Long) =
        wineDao.getWineFullNamingByIdNotLive(wineId)

    fun getNamingsForCounty(countyId: Long) = wineDao.getNamingsForCounty(countyId)
    fun getWineWithBottlesByCounty(countyId: Long) = wineDao.getWineWithBottlesByCounty(countyId)

    fun getCountiesWithWines() = countyDao.getCountiesWithWines()

    fun getBoundedBottles() = bottleDao.getBoundedBottles()
    suspend fun getBoundedBottlesNotLive() = bottleDao.getBoundedBottlesNotLive()
    suspend fun deleteAllWines() = wineDao.deleteAll()


    // County
    suspend fun insertCounty(county: County) {
        if (county.name.isBlank()) {
            throw IllegalArgumentException("County name is blank.")
        }

        countyDao.insertCounty(county)
    }

    suspend fun updateCounty(county: County) {
        if (county.name.isBlank()) {
            throw IllegalArgumentException("County name is blank.")
        }

        countyDao.updateCounty(county)
    }

    fun getAllCounties() = countyDao.getAllCounties()
    suspend fun getAllCountiesNotLive() = countyDao.getAllCountiesNotLive()
    suspend fun updateCounties(counties: List<County>) = countyDao.updateCounties(counties)
    suspend fun deleteCounty(countyId: Long) = countyDao.deleteCounty(countyId)
    suspend fun deleteAllCounties() = countyDao.deleteAll()

    // Bottle
    suspend fun insertBottle(bottle: Bottle) = bottleDao.insertBottle(bottle)
    suspend fun insertBottles(bottles: List<Bottle>) = bottleDao.insertBottles(bottles)
    suspend fun updateBottle(bottle: Bottle) = bottleDao.updateBottle(bottle)
    suspend fun updateBottles(bottles: List<Bottle>) = bottleDao.updateBottles(bottles)
    suspend fun deleteBottleById(bottleId: Long) = bottleDao.deleteBottleById(bottleId)
    fun getBottleById(bottleId: Long) = bottleDao.getBottleById(bottleId)
    suspend fun getBottleByIdNotLive(bottleId: Long) = bottleDao.getBottleByIdNotLive(bottleId)
    fun getBottlesForWine(wineId: Long) = bottleDao.getBottlesForWine(wineId)
    suspend fun consumeBottle(bottleId: Long) = bottleDao.consumeBottle(bottleId)
    suspend fun fav(bottleId: Long) = bottleDao.fav(bottleId)
    suspend fun unfav(bottleId: Long) = bottleDao.unfav(bottleId)
    suspend fun removeTastingForBottle(bottleId: Long) = bottleDao.removeTastingForBottle(bottleId)

    fun getFReviewAndReviewForBottle(bottleId: Long) =
        fReviewDao.getFReviewAndReviewForBottle(bottleId)

    suspend fun getFReviewAndReviewForBottleNotLive(bottleId: Long) =
        fReviewDao.getFReviewAndReviewForBottleNotLive(bottleId)

    suspend fun getFReviewsForBottleNotLive(bottleId: Long) =
        fReviewDao.getFReviewsForBottleNotLive(bottleId)

    suspend fun revertBottleConsumption(bottleId: Long) {
        database.withTransaction {
            bottleDao.revertBottleConsumption(bottleId)
            historyDao.onBottleConsumptionReverted(bottleId)
        }
    }

    suspend fun declareGiftedBottle(entry: HistoryEntry, friendId: Long) {
        database.withTransaction {
            val entryId = historyDao.insertEntry(entry)
            historyXFriendDao.insertHistoryXFriend(
                HistoryXFriend(
                    entryId,
                    friendId
                )
            )
        }
    }

    suspend fun getTastingBottleIdsIn(bottles: List<Long>) =
        bottleDao.getTastingBottleIdsIn(bottles)

    suspend fun boundBottlesToTasting(tastingId: Long, bottles: List<Long>) =
        bottleDao.boundBottlesToTasting(tastingId, bottles)

    suspend fun deleteAllBottles() = bottleDao.deleteAll()

    // Grape
    suspend fun updateGrape(grape: Grape) {
        if (grape.name.isBlank()) {
            throw IllegalArgumentException("Grape name is blank.")
        }

        grapeDao.updateGrape(grape)
    }

    suspend fun insertGrape(grape: Grape): Long {
        if (grape.name.isBlank()) {
            throw IllegalArgumentException("Grape name is blank.")
        }

        return grapeDao.insertGrape(grape)
    }

    suspend fun deleteGrape(grape: Grape) = grapeDao.deleteGrape(grape)
    fun getAllGrapes() = grapeDao.getAllGrapes()
    suspend fun getAllGrapesNotLive() = grapeDao.getAllGrapesNotLive()
    fun getGrapeWithQuantifiedGrapes() = grapeDao.getGrapeWithQuantifiedGrapes()
    suspend fun insertQGrape(qGrape: QGrape) = qGrapeDao.insertQGrape(qGrape)
    suspend fun updateQuantifiedGrape(qGrape: QGrape) = qGrapeDao.updateQGrape(qGrape)
    suspend fun deleteQuantifiedGrape(qGrape: QGrape) = qGrapeDao.deleteQGrape(qGrape)

    fun getQGrapesAndGrapeForBottle(bottleId: Long) =
        qGrapeDao.getQGrapesAndGrapeForBottle(bottleId)

    suspend fun getQGrapesAndGrapeForBottleNotLive(bottleId: Long) =
        qGrapeDao.getQGrapesAndGrapeForBottleNotLive(bottleId)

    suspend fun insertGrapeAndQGrape(bottleId: Long, grape: Grape, qGrapeValue: Int) {
        database.withTransaction {
            val grapeId = insertGrape(grape)
            insertQGrape(QGrape(bottleId, grapeId, qGrapeValue))
        }
    }

    suspend fun replaceQGrapesForBottle(bottleId: Long, qGrapes: List<QGrape>) {
        database.withTransaction {
            qGrapeDao.clearAllQGrapesForBottle(bottleId)
            qGrapeDao.insertQGrape(qGrapes)
        }
    }

    suspend fun deleteAllGrapes() = grapeDao.deleteAll()


    // Review
    suspend fun insertReview(review: Review): Long {
        if (review.contestName.isBlank()) {
            throw IllegalArgumentException("Review contestName is blank.")
        }

        return reviewDao.insertReview(review)
    }

    suspend fun updateReview(review: Review) {
        if (review.contestName.isBlank()) {
            throw IllegalArgumentException("Review contestName is blank.")
        }

        reviewDao.updateReview(review)
    }

    suspend fun deleteReview(review: Review) = reviewDao.deleteReview(review)
    fun getAllReviews() = reviewDao.getAllReviews()
    suspend fun getAllReviewsNotLive() = reviewDao.getAllReviewsNotLive()
    fun getReviewWithFilledReviews() = reviewDao.getReviewWithFilledReviews()
    suspend fun insertFilledReview(fReview: FReview) = fReviewDao.insertFReview(fReview)
    suspend fun insertFilledReviews(fReviews: List<FReview>) = fReviewDao.insertFReviews(fReviews)
    suspend fun updateFilledReview(fReview: FReview) = fReviewDao.updateFReview(fReview)
    suspend fun deleteFilledReview(fReview: FReview) = fReviewDao.deleteFReview(fReview)
    suspend fun deleteFReviewByPk(bottleId: Long, reviewId: Long) =
        fReviewDao.deleteFReviewByPk(bottleId, reviewId)

    suspend fun insertReviewAndFReview(bottleId: Long, review: Review, fReviewValue: Int) {
        database.withTransaction {
            val reviewId = insertReview(review)
            insertFilledReview(FReview(bottleId, reviewId, fReviewValue))
        }
    }

    suspend fun replaceFReviewsForBottle(bottleId: Long, fReviews: List<FReview>) {
        database.withTransaction {
            fReviewDao.clearAllFReviewsForBottle(bottleId)
            fReviewDao.insertFReviews(fReviews)
        }
    }

    suspend fun deleteAllReviews() = reviewDao.deleteAll()
    suspend fun deleteAllFReviews() = fReviewDao.deleteAll()

    // Friend
    suspend fun insertFriend(friend: Friend) {
        if (friend.name.isBlank()) {
            throw IllegalArgumentException("Friend name is blank.")
        }

        friendDao.insertFriend(friend)
    }

    suspend fun updateFriend(friend: Friend) {
        if (friend.name.isBlank()) {
            throw IllegalArgumentException("Friend name is blank.")
        }

        friendDao.updateFriend(friend)
    }

    suspend fun deleteFriend(friend: Friend) = friendDao.deleteFriend(friend)
    fun getAllFriends() = friendDao.getAllFriends()
    suspend fun getAllFriendsNotLive() = friendDao.getAllFriendsNotLive()

    suspend fun insertFriendHistoryXRef(fxh: List<HistoryXFriend>) =
        historyXFriendDao.insertHistoryXFriend(fxh)

    suspend fun insertFriendHistoryXRef(fxh: HistoryXFriend) =
        historyXFriendDao.insertHistoryXFriend(fxh)


    // History
    suspend fun updateEntry(entry: HistoryEntry) = historyDao.updateEntry(entry)
    fun getAllEntries() = historyDao.getAllEntries()
    fun getYears() = historyDao.getYears()
    fun getBoundedEntriesNotPagedNotLive() = historyDao.getBoundedEntriesNotPagedNotLive()
    fun getEntriesByType(type1: Int, type2: Int) = historyDao.getEntriesByType(type1, type2)
    fun getEntriesForBottle(bottleId: Long) = historyDao.getEntriesForBottle(bottleId)
    fun getEntriesForDate(date: Long) = historyDao.getEntriesForDate(date)
    fun getFavoriteEntries() = historyDao.getFavoriteEntries()
    fun getOldestEntryDate() = historyDao.getOldestEntryDate()
    fun getAllEntriesNotPagedNotLive() = historyDao.getAllEntriesNotPagedNotLive()

    fun getBoundedEntriesBetween(start: Long, end: Long) =
        historyDao.getBoundedEntriesBetween(start, end)

    suspend fun insertHistoryEntry(entry: HistoryEntry): Long {
        return database.withTransaction {
            when (entry.type) {
                0, 2, 4 -> consumeBottle(entry.bottleId)
                1, 3 -> {
                    revertBottleConsumption(entry.bottleId)
                    historyDao.clearExistingReplenishments(entry.bottleId)
                }
            }

            historyDao.insertEntry(entry)
        }
    }

    suspend fun insertHistoryEntryAndFriends(entry: HistoryEntry, friends: List<Long>) {
        database.withTransaction {
            val historyId = insertHistoryEntry(entry)
            val historyXFriends = friends.map { HistoryXFriend(historyId, it) }
            insertFriendHistoryXRef(historyXFriends)
        }
    }

    suspend fun deleteAllHistoryEntries() = historyDao.deleteAll()


    // Stats
    fun getStockByCounty() = statsDao.getStockByCounty()
    fun getReplenishmentsByCounty(start: Long, end: Long) =
        statsDao.getReplenishmentsByCounty(start, end)

    fun getConsumptionsByCounty(start: Long, end: Long) =
        statsDao.getConsumptionsByCounty(start, end)

    fun getStockByColor() = statsDao.getStockByColor()
    fun getReplenishmentsByColor(start: Long, end: Long) =
        statsDao.getReplenishmentsByColor(start, end)

    fun getConsumptionsByColor(start: Long, end: Long) =
        statsDao.getConsumptionsByColor(start, end)

    fun getStockByVintage() = statsDao.getStockByVintage()
    fun getReplenishmentsByVintage(start: Long, end: Long) =
        statsDao.getReplenishmentsByVintage(start, end)

    fun getConsumptionsByVintage(start: Long, end: Long) =
        statsDao.getConsumptionsByVintage(start, end)

    fun getStockByNaming() = statsDao.getStockByNaming()
    fun getReplenishmentsByNaming(start: Long, end: Long) =
        statsDao.getReplenishmentsByNaming(start, end)

    fun getConsumptionsByNaming(start: Long, end: Long) =
        statsDao.getConsumptionsByNaming(start, end)


    // Tastings
    suspend fun insertTasting(tasting: Tasting) = tastingDao.insertTasting(tasting)
    suspend fun updateTasting(tasting: Tasting) = tastingDao.updateTasting(tasting)
    suspend fun deleteTasting(tasting: Tasting) = tastingDao.deleteTasting(tasting)
    suspend fun deleteTastings(tastings: List<Tasting>) = tastingDao.deleteTastings(tastings)
    suspend fun getAllTastingsNotLive() = tastingDao.getAllTastingsNotLive()
    suspend fun getEmptyTastings() = tastingDao.getEmptyTastings()
    fun getUndoneTastings() = tastingDao.getUndoneTastings()
    fun getLastTasting() = tastingDao.getLastTasting()
    suspend fun getTastingById(tastingId: Long) = tastingDao.getTastingById(tastingId)
    suspend fun getBoundedTastingById(tastingId: Long) =
        tastingDao.getTastingWithFriendsById(tastingId)

    suspend fun insertTastingFriendXRef(tastingId: Long, friends: List<Long>) {
        database.withTransaction {
            friends.forEach {
                tastingXFriendDao.insertTastingXFriend(TastingXFriend(tastingId, it))
            }
        }
    }

    fun getBottlesWithTastingActionsForTasting(tastingId: Long) =
        tastingDao.getBottlesWithTastingActionsForTasting(tastingId)

    suspend fun getBottlesWithTastingActionsForTastingNotLive(tastingId: Long) =
        tastingDao.getBottlesWithTastingActionsForTastingNotLive(tastingId)


    // Tasting actions
    suspend fun insertTastingActions(tastingActions: List<TastingAction>) =
        tastingActionDao.insertTastingActions(tastingActions)

    suspend fun updateTastingAction(tastingAction: TastingAction) =
        tastingActionDao.updateTastingAction(tastingAction)

    suspend fun deleteTastingAction(tastingAction: TastingAction) =
        tastingActionDao.deleteTastingAction(tastingAction)

    suspend fun deleteTastingActionsForBottle(bottleId: Long) =
        tastingActionDao.deleteTastingActionsForBottle(bottleId)

    suspend fun importDbFromExternalDir(externalDirPath: String) {
        val file = File("$externalDirPath/db.json")

        if (!file.exists()) {
            throw IllegalStateException("Cannot find '[externalDir]/files/db.json")
        }

        val adapter = moshi.adapter(DbTablesJsonAdapter::class.java)
        val data = StringBuffer("")

        withContext(IO) {
            try {
                val fIn = FileInputStream(file)
                val isr = InputStreamReader(fIn)
                val buffreader = BufferedReader(isr)
                var readString: String? = buffreader.readLine()

                while (readString != null) {
                    L.v("read line")
                    data.append(readString)
                    readString = buffreader.readLine()
                }
                isr.close()
            } catch (ioe: IOException) {
                ioe.printStackTrace()
            }

            if (data.isEmpty()) {
                throw IllegalStateException("Cannot read data from json file")
            }

            doImportDbFromExternal(adapter.fromJson(data.toString()))
        }

    }

    private suspend fun doImportDbFromExternal(tables: DbTablesJsonAdapter?) {
        if (tables == null) {
            throw IllegalStateException("Moshi returned a null object")
        }

        database.withTransaction {
            deleteAllCounties()
            deleteAllWines()
            deleteAllReviews()
            deleteAllFReviews()
            deleteAllGrapes()
            deleteAllBottles()
            deleteAllHistoryEntries()

            with(tables) {
                counties.forEach { insertCounty(it) }
                insertWines(wines)
                reviews.forEach { insertReview(it) }
                grapes.forEach { insertGrape(it) }
                insertBottles(bottles)
                insertFilledReviews(fReviews)
                historyEntries.forEach { insertHistoryEntry(it) }
            }
        }
    }

    class DbTablesJsonAdapter(
        val counties: List<County>,
        val wines: List<Wine>,
        val reviews: List<Review>,
        val fReviews: List<FReview>,
        val grapes: List<Grape>,
        val bottles: List<Bottle>,
        val historyEntries: List<HistoryEntry>
    )
}
