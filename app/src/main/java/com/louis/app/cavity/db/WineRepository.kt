package com.louis.app.cavity.db

import android.app.Application
import androidx.room.withTransaction
import com.louis.app.cavity.model.*

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
    private val statsDao = database.statsDao()
    private val tastingDao = database.tastingDao()


    // Wine
    suspend fun insertWine(wine: Wine) = wineDao.insertWine(wine)
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

    // Bottle
    suspend fun insertBottle(bottle: Bottle) = bottleDao.insertBottle(bottle)
    suspend fun updateBottle(bottle: Bottle) = bottleDao.updateBottle(bottle)
    suspend fun deleteBottleById(bottleId: Long) = bottleDao.deleteBottleById(bottleId)
    fun getBottleById(bottleId: Long) = bottleDao.getBottleById(bottleId)
    suspend fun getBottleByIdNotLive(bottleId: Long) = bottleDao.getBottleByIdNotLive(bottleId)
    fun getBottlesForWine(wineId: Long) = bottleDao.getBottlesForWine(wineId)
    suspend fun consumeBottle(bottleId: Long) = bottleDao.consumeBottle(bottleId)
    suspend fun fav(bottleId: Long) = bottleDao.fav(bottleId)
    suspend fun unfav(bottleId: Long) = bottleDao.unfav(bottleId)

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
    fun getAllEntriesNotPagedNotLive() = historyDao.getAllEntriesNotPagedNotLive()

    fun getBoundedEntriesBetween(start: Long, end: Long) =
        historyDao.getBoundedEntriesBetween(start, end)

    suspend fun insertHistoryEntry(entry: HistoryEntry): Long {
        // TODO: handle tasting here
        return database.withTransaction {
            when (entry.type) {
                0 -> consumeBottle(entry.bottleId)
                1 -> {
                    revertBottleConsumption(entry.bottleId)
                    historyDao.clearExistingReplenishments(entry.bottleId)
                }
                2 -> consumeBottle(entry.bottleId)
                3 -> {
                    revertBottleConsumption(entry.bottleId)
                    historyDao.clearExistingReplenishments(entry.bottleId)
                }
                4 -> TODO("Waiting for tasting feature")
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
    fun getFutureTastings() = tastingDao.getFutureTastings()
}
