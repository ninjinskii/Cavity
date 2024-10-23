package com.louis.app.cavity.domain.repository

import android.app.Application
import androidx.room.withTransaction
import com.louis.app.cavity.db.CavityDatabase
import com.louis.app.cavity.domain.history.HistoryEntryType
import com.louis.app.cavity.domain.history.toInt
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.FReview
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.QGrape
import com.louis.app.cavity.ui.addbottle.viewmodel.FReviewUiModel
import com.louis.app.cavity.ui.addbottle.viewmodel.QGrapeUiModel
import com.louis.app.cavity.util.L


// TODO: need service
class BottleRepository private constructor(app: Application) {
    companion object {
        @Volatile
        var instance: BottleRepository? = null

        fun getInstance(app: Application) =
            instance ?: synchronized(this) {
                instance ?: BottleRepository(app).also { instance = it }
            }
    }

    private val database = CavityDatabase.getInstance(app)
    private val bottleDao = database.bottleDao()
    private val qGrapeDao = database.qGrapeDao()
    private val fReviewDao = database.fReviewDao()
    private val historyDao = database.historyDao()

    suspend fun insertBottle(bottle: Bottle) = bottleDao.insertBottle(bottle)

    suspend fun insertBottles(bottles: List<Bottle>) = bottleDao.insertBottles(bottles)

    suspend fun insertBottles(
        bottle: Bottle,
        uiQGrapes: List<QGrapeUiModel>,
        uiFReviews: List<FReviewUiModel>,
        isAGift: Boolean,
        count: Int
    ) {
        database.withTransaction {
            repeat(count) {
                val bottleId = insertBottle(bottle)
                insertBottleMetadata(bottleId, bottle.buyDate, uiQGrapes, uiFReviews, isAGift)
            }
        }
    }

    suspend fun updateBottle(
        bottle: Bottle,
        uiQGrapes: List<QGrapeUiModel>,
        uiFReviews: List<FReviewUiModel>,
        isAGift: Boolean
    ) {
        database.withTransaction {
            updateBottle(bottle)
            L.v("bottle.id ${bottle.id}")
            insertBottleMetadata(bottle.id, bottle.buyDate, uiQGrapes, uiFReviews, isAGift)
        }
    }

    suspend fun updateBottle(bottle: Bottle) = bottleDao.updateBottle(bottle)

    suspend fun deleteBottles(bottles: List<Bottle>) = bottleDao.deleteBottles(bottles)

    suspend fun deleteBottleById(bottleId: Long) = bottleDao.deleteBottleById(bottleId)

    fun getBottleById(bottleId: Long) = bottleDao.getBottleById(bottleId)

    suspend fun getBottleByIdNotLive(bottleId: Long) = bottleDao.getBottleByIdNotLive(bottleId)

    suspend fun getAllBottlesNotLive() = bottleDao.getAllBottlesNotLive()

    fun getBottlesForWine(wineId: Long) = bottleDao.getBottlesForWine(wineId)

    suspend fun getBottlesForWineNotLive(wineId: Long) = bottleDao.getBottlesForWineNotLive(wineId)

    fun getBoundedBottles() = bottleDao.getBoundedBottles()

    suspend fun getBoundedBottleByIdNotLive(bottleId: Long) =
        bottleDao.getBoundedBottleByIdNotLive(bottleId)

    suspend fun consumeBottle(bottleId: Long) = bottleDao.consumeBottle(bottleId)

    suspend fun fav(bottleId: Long) = bottleDao.fav(bottleId)

    suspend fun unfav(bottleId: Long) = bottleDao.unfav(bottleId)

    suspend fun removeTastingForBottle(bottleId: Long) = bottleDao.removeTastingForBottle(bottleId)

    // TODO: service
    suspend fun revertBottleConsumption(bottleId: Long) {
        database.withTransaction {
            bottleDao.revertBottleConsumption(bottleId)
            historyDao.clearConsumptionsForBottle(bottleId)
        }
    }

    suspend fun getTastingBottleIdsIn(bottles: List<Long>) =
        bottleDao.getTastingBottleIdsIn(bottles)

    suspend fun boundBottlesToTasting(tastingId: Long, bottles: List<Long>) =
        bottleDao.boundBottlesToTasting(tastingId, bottles)

    fun getAllBuyLocations() = bottleDao.getAllBuyLocations()

    suspend fun deleteAllBottles() = bottleDao.deleteAll()

    // TODO: move this method to a service
    private suspend fun insertBottleMetadata(
        bottleId: Long,
        buyDate: Long,
        uiQGrapes: List<QGrapeUiModel>,
        uiFReviews: List<FReviewUiModel>,
        isAGift: Boolean
    ) {
        database.withTransaction {
            val fReviews = uiFReviews.map { FReview(bottleId, it.reviewId, it.value) }
            val qGrapes = uiQGrapes
                .filter { it.percentage > 0 }
                .map { QGrape(bottleId, it.grapeId, it.percentage) }

            qGrapeDao.clearAllQGrapesForBottle(bottleId)
            qGrapeDao.insertQGrapes(qGrapes)
            fReviewDao.clearAllFReviewsForBottle(bottleId)
            fReviewDao.insertFReviews(fReviews)

            val type = if (isAGift) HistoryEntryType.GIVEN_BY else HistoryEntryType.REPLENISHMENT
            val entry = HistoryEntry(0, buyDate, bottleId, null, "", type.toInt(), 0)
            historyDao.clearReplenishmentsForBottle(bottleId)
            historyDao.insertEntry(entry)
        }
    }
}
