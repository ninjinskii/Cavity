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
    private val tastingXFriendDao = database.tastingXFriendDao()
    private val statsDao = database.statsDao()
    private val tastingDao = database.tastingDao()
    private val tastingActionDao = database.tastingActionDao()

    suspend fun <T> transaction(databaseQueries: suspend () -> T) = database.withTransaction {
        databaseQueries()
    }

    // Wine
    suspend fun insertWine(wine: Wine) = wineDao.insertWine(wine)
    suspend fun insertWines(wines: List<Wine>) = wineDao.insertWines(wines)
    suspend fun updateWine(wine: Wine) = wineDao.updateWine(wine)
    suspend fun hideWineById(wineId: Long) = wineDao.hideWineById(wineId)
    suspend fun deleteWineById(wineId: Long) = wineDao.deleteWineById(wineId)
    fun getWineById(wineId: Long) = wineDao.getWineById(wineId)
    suspend fun getWineByIdNotLive(wineId: Long) = wineDao.getWineByIdNotLive(wineId)
    suspend fun getAllWinesNotLive() = wineDao.getAllWinesNotLive()
    suspend fun getWineByAttributes(color: WineColor, isOrganic: Int, cuvee: String) =
        wineDao.getWineByAttributes(color, isOrganic, cuvee)

    fun getNamingsForCounty(countyId: Long) = wineDao.getNamingsForCounty(countyId)
    fun getWineWithBottlesByCounty(countyId: Long) = wineDao.getWineWithBottlesByCounty(countyId)

    fun getCountiesWithWines() = countyDao.getCountiesWithWines()

    fun getBoundedBottles() = bottleDao.getBoundedBottles()
    suspend fun getBoundedBottleByIdNotLive(bottleId: Long) =
        bottleDao.getBoundedBottleByIdNotLive(bottleId)

    suspend fun deleteAllWines() = wineDao.deleteAll()


    // County
    suspend fun insertCounty(county: County) {
        if (county.name.isBlank()) {
            throw IllegalArgumentException("County name is blank.")
        }

        countyDao.insertCounties(county)
    }

    suspend fun insertCounties(counties: List<County>) {
        if (counties.any { it.name.isBlank() }) {
            throw IllegalArgumentException("County name is blank.")
        }

        countyDao.insertCounties(counties)
    }

    suspend fun updateCounty(county: County) {
        if (county.name.isBlank()) {
            throw IllegalArgumentException("County name is blank.")
        }

        countyDao.updateCounty(county)
    }

    fun getAllCounties() = countyDao.getAllCounties()
    fun getNonEmptyCounties() = countyDao.getNonEmptyCounties()
    suspend fun getAllCountiesNotLive() = countyDao.getAllCountiesNotLive()
    suspend fun updateCounties(counties: List<County>) = countyDao.updateCounties(counties)
    suspend fun deleteCounty(countyId: Long) = countyDao.deleteCounty(countyId)
    suspend fun deleteAllCounties() = countyDao.deleteAll()

    // Bottle
    suspend fun insertBottle(bottle: Bottle) = bottleDao.insertBottle(bottle)
    suspend fun insertBottles(bottles: List<Bottle>) = bottleDao.insertBottles(bottles)
    suspend fun updateBottle(bottle: Bottle) = bottleDao.updateBottle(bottle)
    suspend fun deleteBottles(bottles: List<Bottle>) = bottleDao.deleteBottles(bottles)
    suspend fun deleteBottleById(bottleId: Long) = bottleDao.deleteBottleById(bottleId)
    fun getBottleById(bottleId: Long) = bottleDao.getBottleById(bottleId)
    suspend fun getBottleByIdNotLive(bottleId: Long) = bottleDao.getBottleByIdNotLive(bottleId)
    suspend fun getAllBottlesNotLive() = bottleDao.getAllBottlesNotLive()
    fun getBottlesForWine(wineId: Long) = bottleDao.getBottlesForWine(wineId)
    suspend fun getBottlesForWineNotLive(wineId: Long) = bottleDao.getBottlesForWineNotLive(wineId)
    suspend fun consumeBottle(bottleId: Long) = bottleDao.consumeBottle(bottleId)
    suspend fun fav(bottleId: Long) = bottleDao.fav(bottleId)
    suspend fun unfav(bottleId: Long) = bottleDao.unfav(bottleId)
    suspend fun removeTastingForBottle(bottleId: Long) = bottleDao.removeTastingForBottle(bottleId)

    fun getFReviewAndReviewForBottle(bottleId: Long) =
        fReviewDao.getFReviewAndReviewForBottle(bottleId)

    suspend fun getFReviewAndReviewForBottleNotLive(bottleId: Long) =
        fReviewDao.getFReviewAndReviewForBottleNotLive(bottleId)

    suspend fun revertBottleConsumption(bottleId: Long) {
        database.withTransaction {
            bottleDao.revertBottleConsumption(bottleId)
            historyDao.onBottleConsumptionReverted(bottleId)
        }
    }

    suspend fun declareGiftedBottle(entry: HistoryEntry, friendId: Long) {
        if (!database.inTransaction()) {
            throw IllegalStateException("This method should be called inside a transaction")
        }

        val entryId = historyDao.insertEntry(entry)

        historyXFriendDao.insertHistoryXFriend(
            HistoryXFriend(
                entryId,
                friendId
            )
        )
    }

    suspend fun getTastingBottleIdsIn(bottles: List<Long>) =
        bottleDao.getTastingBottleIdsIn(bottles)

    suspend fun boundBottlesToTasting(tastingId: Long, bottles: List<Long>) =
        bottleDao.boundBottlesToTasting(tastingId, bottles)

    fun getAllBuyLocations() = bottleDao.getAllBuyLocations()

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

    suspend fun insertGrapes(grapes: List<Grape>) = grapeDao.insertGrapes(grapes)
    suspend fun deleteGrape(grape: Grape) = grapeDao.deleteGrape(grape)
    fun getAllGrapes() = grapeDao.getAllGrapes()
    suspend fun getAllGrapesNotLive() = grapeDao.getAllGrapesNotLive()
    fun getGrapeWithQuantifiedGrapes() = grapeDao.getGrapeWithQuantifiedGrapes()
    suspend fun getAllQGrapesNotLive() = qGrapeDao.getAllQGrapesNotLive()
    suspend fun insertQGrapes(grapes: List<QGrape>) = qGrapeDao.insertQGrapes(grapes)

    fun getQGrapesAndGrapeForBottle(bottleId: Long) =
        qGrapeDao.getQGrapesAndGrapeForBottle(bottleId)

    suspend fun getQGrapesAndGrapeForBottleNotLive(bottleId: Long) =
        qGrapeDao.getQGrapesAndGrapeForBottleNotLive(bottleId)

    suspend fun replaceQGrapesForBottle(bottleId: Long, qGrapes: List<QGrape>) {
        if (!database.inTransaction()) {
            throw IllegalStateException("This method should be called inside a transaction")
        }

        qGrapeDao.clearAllQGrapesForBottle(bottleId)
        qGrapeDao.insertQGrapes(qGrapes)
    }

    suspend fun deleteAllGrapes() = grapeDao.deleteAll()
    suspend fun deleteAllQGrapes() = qGrapeDao.deleteAll()


    // Review
    suspend fun insertReview(review: Review): Long {
        if (review.contestName.isBlank()) {
            throw IllegalArgumentException("Review contestName is blank.")
        }

        return reviewDao.insertReview(review)
    }

    suspend fun insertReviews(reviews: List<Review>) = reviewDao.insertReviews(reviews)

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
    suspend fun getAllFReviewsNotLive() = fReviewDao.getAllFReviewsNotLive()
    suspend fun insertFilledReviews(fReviews: List<FReview>) = fReviewDao.insertFReviews(fReviews)

    suspend fun replaceFReviewsForBottle(bottleId: Long, fReviews: List<FReview>) {
        if (!database.inTransaction()) {
            throw IllegalStateException("This method should be called inside a transaction")
        }

        fReviewDao.clearAllFReviewsForBottle(bottleId)
        fReviewDao.insertFReviews(fReviews)
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

    suspend fun insertFriends(friends: List<Friend>) = friendDao.insertFriends(friends)

    suspend fun updateFriend(friend: Friend) {
        if (friend.name.isBlank()) {
            throw IllegalArgumentException("Friend name is blank.")
        }

        friendDao.updateFriend(friend)
    }

    suspend fun deleteFriend(friend: Friend) = friendDao.deleteFriend(friend)
    suspend fun deleteAllFriends() = friendDao.deleteAll()
    fun getAllFriends() = friendDao.getAllFriends()
    suspend fun getFriendByIdNotLive(friendId: Long) = friendDao.getFriendByIdNotLive(friendId)
    suspend fun getAllFriendsNotLive() = friendDao.getAllFriendsNotLive()

    suspend fun insertFriendHistoryXRefs(fxh: List<HistoryXFriend>) =
        historyXFriendDao.insertHistoryXFriends(fxh)

    suspend fun getAllHistoryXFriendsNotLive() = historyXFriendDao.getAllHistoryXFriendsNotLive()
    suspend fun deleteAllFriendHistoryXRefs() = historyXFriendDao.deleteAll()


    // History
    suspend fun updateEntry(entry: HistoryEntry) = historyDao.updateEntry(entry)
    fun getAllEntries() = historyDao.getAllEntries()
    suspend fun getAllEntriesNotPagedNotLive() = historyDao.getAllEntriesNotPagedNotLive()
    fun getYears() = historyDao.getYears()
    fun getEntriesByType(type1: Int, type2: Int) = historyDao.getEntriesByType(type1, type2)
    fun getEntriesForWine(wineId: Long) = historyDao.getEntriesForWine(wineId)
    fun getEntriesForBottle(bottleId: Long) = historyDao.getEntriesForBottle(bottleId)
    fun getReplenishmentForBottleNotPaged(bottleId: Long) =
        historyDao.getReplenishmentForBottleNotPaged(bottleId)

    fun getEntriesForDate(date: Long) = historyDao.getEntriesForDate(date)
    fun getFavoriteEntries() = historyDao.getFavoriteEntries()

    suspend fun clearExistingReplenishments(bottleId: Long) =
        historyDao.clearExistingReplenishments(bottleId)

    suspend fun insertHistoryEntry(entry: HistoryEntry) = historyDao.insertEntry(entry)
    suspend fun insertHistoryEntries(entries: List<HistoryEntry>) =
        historyDao.insertEntries(entries)

    suspend fun insertHistoryEntryAndFriends(entry: HistoryEntry, friends: List<Long>) {
        if (!database.inTransaction()) {
            throw IllegalStateException("This method should be called inside a transaction")
        }

        val historyId = insertHistoryEntry(entry)
        val historyXFriends = friends.map { HistoryXFriend(historyId, it) }
        insertFriendHistoryXRefs(historyXFriends)
    }

    suspend fun deleteAllHistoryEntries() = historyDao.deleteAll()


    // Stats
    fun getBottleCountForCounty(countyId: Long) = statsDao.getBottleCountForCounty(countyId)
    fun getPriceByCurrencyForCounty(countyId: Long) = statsDao.getPriceByCurrencyForCounty(countyId)
    fun getNamingsStatsForCounty(countyId: Long) = statsDao.getNamingsForCounty(countyId)
    fun getVintagesStatsForCounty(countyId: Long) = statsDao.getVintagesForCounty(countyId)
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

    fun getTotalPriceByCurrency() = statsDao.getTotalPriceByCurrency()
    fun getTotalConsumedBottles() = statsDao.getTotalConsumedBottles()
    fun getTotalStockBottles() = statsDao.getTotalStockBottles()
    fun getBottlesByIds(ids: List<Long>) = statsDao.getBottlesByIds(ids)


    // Tastings
    suspend fun insertTasting(tasting: Tasting) = tastingDao.insertTasting(tasting)
    suspend fun insertTastings(tastings: List<Tasting>) = tastingDao.insertTastings(tastings)
    suspend fun updateTasting(tasting: Tasting) = tastingDao.updateTasting(tasting)
    suspend fun deleteTastings(tastings: List<Tasting>) = tastingDao.deleteTastings(tastings)
    suspend fun deleteAllTastings() = tastingDao.deleteAll()
    suspend fun getAllTastingsNotLive() = tastingDao.getAllTastingsNotLive()
    suspend fun getEmptyTastings() = tastingDao.getEmptyTastings()
    fun getUndoneTastings() = tastingDao.getUndoneTastings()
    suspend fun getTastingById(tastingId: Long) = tastingDao.getTastingById(tastingId)
    suspend fun getBoundedTastingById(tastingId: Long) =
        tastingDao.getTastingWithFriendsById(tastingId)

    suspend fun getAllTastingXFriendsNotLive() = tastingXFriendDao.getAllTastingXFriendsNotLive()

    suspend fun insertTastingFriendXRefs(tastings: List<TastingXFriend>) =
        tastingXFriendDao.insertTastingXFriends(tastings)

    suspend fun insertTastingFriendXRef(tastingId: Long, friends: List<Long>) {
        if (!database.inTransaction()) {
            throw IllegalStateException("This method should be called inside a transaction")
        }

        friends.forEach {
            tastingXFriendDao.insertTastingXFriend(TastingXFriend(tastingId, it))
        }
    }

    fun getBottlesWithTastingActionsForTasting(tastingId: Long) =
        tastingDao.getBottlesWithTastingActionsForTasting(tastingId)

    suspend fun getBottlesWithTastingActionsForTastingNotLive(tastingId: Long) =
        tastingDao.getBottlesWithTastingActionsForTastingNotLive(tastingId)

    suspend fun deleteAllTastingFriendXRefs() = tastingXFriendDao.deleteAll()


    // Tasting actions
    suspend fun insertTastingActions(tastingActions: List<TastingAction>) =
        tastingActionDao.insertTastingActions(tastingActions)

    suspend fun updateTastingAction(tastingAction: TastingAction) =
        tastingActionDao.updateTastingAction(tastingAction)

    suspend fun getTastingActionById(tastingActionId: Long) =
        tastingActionDao.getTastingActionById(tastingActionId)

    suspend fun getAllTastingActionsNotLive() = tastingActionDao.getAllTastingActionsNotLive()

    suspend fun deleteTastingActionsForBottle(bottleId: Long) =
        tastingActionDao.deleteTastingActionsForBottle(bottleId)

    suspend fun deleteAllTastingActions() = tastingActionDao.deleteAll()

}
