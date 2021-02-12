package com.louis.app.cavity.db

import android.app.Application
import androidx.room.withTransaction
import com.louis.app.cavity.model.*
import com.louis.app.cavity.model.relation.crossref.FilledBottleReviewXRef
import com.louis.app.cavity.model.relation.crossref.FriendHistoryEntryXRef
import com.louis.app.cavity.model.relation.crossref.QuantifiedBottleGrapeXRef

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


    // Wine
    suspend fun insertWine(wine: Wine) = wineDao.insertWine(wine)
    suspend fun updateWine(wine: Wine) = wineDao.updateWine(wine)
    suspend fun deleteWine(wine: Wine) = wineDao.deleteWine(wine)
    suspend fun deleteWineById(wineId: Long) = wineDao.deleteWineById(wineId)
    fun getWineById(wineId: Long) = wineDao.getWineById(wineId)
    suspend fun getWineByIdNotLive(wineId: Long) = wineDao.getWineByIdNotLive(wineId)
    fun getWineWithBottlesByCounty(countyId: Long) = wineDao.getWineWithBottlesByCounty(countyId)
    fun getCountiesWithWines() = countyDao.getCountiesWithWines()

    suspend fun getWineAndBottleWithQGrapesAndFReviews() =
        bottleDao.getBottleAndWineWithQGrapesAndFReview()


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
    suspend fun insertBottles(bottleId: Long, count: Int) = bottleDao.addBottles(bottleId, count)
    suspend fun updateBottle(bottle: Bottle) = bottleDao.updateBottle(bottle)
    suspend fun deleteBottles(bottleId: Long, count: Int) = bottleDao.removeBottles(bottleId, count)
    suspend fun deleteBottleById(bottleId: Long) = bottleDao.deleteBottleById(bottleId)
    fun getBottleById(bottleId: Long) = bottleDao.getBottleById(bottleId)
    suspend fun getBottleByIdNotLive(bottleId: Long) = bottleDao.getBottleByIdNotLive(bottleId)
    suspend fun consumeBottle(bottleId: Long) = bottleDao.consumeBottle(bottleId)
    suspend fun fav(bottleId: Long) = bottleDao.fav(bottleId)
    suspend fun unfav(bottleId: Long) = bottleDao.unfav(bottleId)

    fun getFReviewAndReviewForBottle(bottleId: Long) =
        fReviewDao.getFReviewAndReviewForBottle(bottleId)

    suspend fun getFReviewsForBottleNotLive(bottleId: Long) =
        fReviewDao.getFReviewsForBottleNotLive(bottleId)

    suspend fun revertBottleConsumption(bottleId: Long) {
        database.withTransaction {
            bottleDao.revertBottleConsumption(bottleId)
            historyDao.deleteEntriesForBottle(bottleId)
        }
    }

    suspend fun declareGiftedBottle(entry: HistoryEntry, friendId: Long) {
        database.withTransaction {
            val entryId = historyDao.insertEntry(entry)
            historyXFriendDao.insertFriendHistoryEntryXRef(
                FriendHistoryEntryXRef(
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
    suspend fun getQGrape(bottleId: Long, grapeId: Long) = qGrapeDao.getQGrape(bottleId, grapeId)
    suspend fun getAllGrapesNotLive() = grapeDao.getAllGrapesNotLive()
    fun getGrapeWithQuantifiedGrapes() = grapeDao.getGrapeWithQuantifiedGrapes()

    suspend fun insertQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef) =
        qGrapeDao.insertQuantifiedGrape(qGrape)

    suspend fun updateQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef) =
        qGrapeDao.updateQuantifiedGrape(qGrape)

    suspend fun deleteQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef) =
        qGrapeDao.deleteQuantifiedGrape(qGrape)

    fun getQGrapesAndGrapeForBottle(bottleId: Long) =
        qGrapeDao.getQGrapesAndGrapeForBottle(bottleId)

    suspend fun getQGrapesForBottleNotLive(bottleId: Long) =
        qGrapeDao.getQGrapesForBottleNotLive(bottleId)

    suspend fun insertGrapeAndQGrape(bottleId: Long, grape: Grape, qGrapeValue: Int) {
        database.withTransaction {
            val grapeId = insertGrape(grape)
            insertQuantifiedGrape(QuantifiedBottleGrapeXRef(bottleId, grapeId, qGrapeValue))
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
    suspend fun getAllReviewsNotLive() = reviewDao.getAllReviewsNotLive()
    fun getReviewWithFilledReviews() = reviewDao.getReviewWithFilledReviews()

    suspend fun insertFilledReview(fReview: FilledBottleReviewXRef) =
        fReviewDao.insertFilledReview(fReview)

    suspend fun updateFilledReview(fReview: FilledBottleReviewXRef) =
        fReviewDao.updateFilledReview(fReview)

    suspend fun deleteFilledReview(fReview: FilledBottleReviewXRef) =
        fReviewDao.deleteFilledReview(fReview)

    suspend fun deleteFReviewByPk(bottleId: Long, reviewId: Long) =
        fReviewDao.deleteFReviewByPk(bottleId, reviewId)

    suspend fun insertReviewAndFReview(bottleId: Long, review: Review, fReviewValue: Int) {
        database.withTransaction {
            val reviewId = insertReview(review)
            insertFilledReview(FilledBottleReviewXRef(bottleId, reviewId, fReviewValue))
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

    suspend fun insertFriendHistoryXRef(fxh: List<FriendHistoryEntryXRef>) =
        historyXFriendDao.insertFriendHistoryEntryXRef(fxh)

    suspend fun insertFriendHistoryXRef(fxh: FriendHistoryEntryXRef) =
        historyXFriendDao.insertFriendHistoryEntryXRef(fxh)



    // History
    fun getAllEntries() = historyDao.getAllEntries()

    suspend fun insertHistoryEntry(entry: HistoryEntry): Long {
        // TODO: handle tasting here
        return database.withTransaction {
            when(entry.type) {
                0 or 2 -> consumeBottle(entry.bottleId)
                1 or 3 -> revertBottleConsumption(entry.bottleId)
            }

            historyDao.insertEntry(entry)
        }
    }

    suspend fun insertHistoryEntryAndFriends(entry: HistoryEntry, friends: List<Long>) {
        database.withTransaction {
            val historyId = insertHistoryEntry(entry)
            val historyXFriends = friends.map { FriendHistoryEntryXRef(historyId, it) }
            insertFriendHistoryXRef(historyXFriends)
        }
    }
}
