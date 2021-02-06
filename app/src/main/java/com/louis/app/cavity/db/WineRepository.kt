package com.louis.app.cavity.db

import android.app.Application
import com.louis.app.cavity.model.*
import com.louis.app.cavity.model.relation.crossref.FilledBottleReviewXRef
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

    fun insertWine(wine: Wine) = wineDao.insertWine(wine)
    fun updateWine(wine: Wine) = wineDao.updateWine(wine)
    fun deleteWine(wine: Wine) = wineDao.deleteWine(wine)
    fun deleteWineById(wineId: Long) = wineDao.deleteWineById(wineId)

    fun insertCounty(county: County) = countyDao.insertCounty(county)
    suspend fun updateCounty(county: County) = countyDao.updateCounty(county)

    suspend fun insertBottle(bottle: Bottle) = bottleDao.insertBottle(bottle)
    suspend fun deleteBottleById(bottleId: Long) = bottleDao.deleteBottleById(bottleId)

    suspend fun updateGrape(grape: Grape) = grapeDao.updateGrape(grape)
    suspend fun deleteGrape(grape: Grape) = grapeDao.deleteGrape(grape)
    suspend fun insertGrape(grape: Grape) = grapeDao.insertGrape(grape)

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

    suspend fun getQGrape(bottleId: Long, grapeId: Long) = qGrapeDao.getQGrape(bottleId, grapeId)

    suspend fun updateReview(review: Review) = reviewDao.updateReview(review)
    suspend fun deleteReview(review: Review) = reviewDao.deleteReview(review)
    suspend fun insertReview(review: Review) = reviewDao.insertReview(review)

    fun getReviewWithFilledReviews() = reviewDao.getReviewWithFilledReviews()

    suspend fun insertFilledReview(fReview: FilledBottleReviewXRef) =
        fReviewDao.insertFilledReview(fReview)

    suspend fun updateFilledReview(fReview: FilledBottleReviewXRef) =
        fReviewDao.updateFilledReview(fReview)

    suspend fun deleteFilledReview(fReview: FilledBottleReviewXRef) =
        fReviewDao.deleteFilledReview(fReview)

    suspend fun deleteFReviewByPk(bottleId: Long, reviewId: Long) =
        fReviewDao.deleteFReviewByPk(bottleId, reviewId)

    fun getFReviewAndReviewForBottle(bottleId: Long) =
        fReviewDao.getFReviewAndReviewForBottle(bottleId)

    suspend fun getFReviewsForBottleNotLive(bottleId: Long) =
        fReviewDao.getFReviewsForBottleNotLive(bottleId)


    fun getWineById(wineId: Long) = wineDao.getWineById(wineId)
    fun getWineByIdNotLive(wineId: Long) = wineDao.getWineByIdNotLive(wineId)
    fun getWineWithBottlesByCounty(countyId: Long) = wineDao.getWineWithBottlesByCounty(countyId)

    fun getAllCounties() = countyDao.getAllCounties()
    suspend fun getAllCountiesNotLive() = countyDao.getAllCountiesNotLive()

    fun getBottleById(bottleId: Long) = bottleDao.getBottleById(bottleId)
    suspend fun getBottleByIdNotLive(bottleId: Long) = bottleDao.getBottleByIdNotLive(bottleId)
    suspend fun getAllGrapesNotLive() = grapeDao.getAllGrapesNotLive()

    suspend fun updateBottle(bottle: Bottle) = bottleDao.updateBottle(bottle)

    suspend fun getAllReviewsNotLive() = reviewDao.getAllReviewsNotLive()

    fun getCountiesWithWines() = countyDao.getCountiesWithWines()

    suspend fun fav(bottleId: Long) = bottleDao.fav(bottleId)
    suspend fun unfav(bottleId: Long) = bottleDao.unfav(bottleId)

    suspend fun removeBottles(bottleId: Long, count: Int) = bottleDao.removeBottles(bottleId, count)
    suspend fun addBottles(bottleId: Long, count: Int) = bottleDao.addBottles(bottleId, count)

    suspend fun updateCounties(counties: List<County>) = countyDao.updateCounties(counties)

    suspend fun deleteCounty(countyId: Long) = countyDao.deleteCounty(countyId)

    suspend fun getWineAndBottleWithQGrapesAndFReviews() =
        bottleDao.getBottleAndWineWithQGrapesAndFReview()

    fun getAllEntries() = historyDao.getAllEntries()
}
