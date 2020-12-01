package com.louis.app.cavity.db

import android.app.Application
import com.louis.app.cavity.model.*
import com.louis.app.cavity.model.relation.QuantifiedBottleGrapeXRef

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

    fun insertWine(wine: Wine) = wineDao.insertWine(wine)
    fun updateWine(wine: Wine) = wineDao.updateWine(wine)
    fun deleteWine(wine: Wine) = wineDao.deleteWine(wine)
    fun deleteWineById(wineId: Long) = wineDao.deleteWineById(wineId)

    fun insertCounty(county: County) = countyDao.insertCounty(county)

    suspend fun insertBottle(bottle: Bottle) = bottleDao.insertBottle(bottle)

    suspend fun updateGrape(grape: Grape) = grapeDao.updateGrape(grape)
    suspend fun deleteGrape(grape: Grape) = grapeDao.deleteGrape(grape)
    suspend fun insertGrape(grape: Grape) = grapeDao.insertGrape(grape)

    suspend fun insertQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef) =
        qGrapeDao.insertQuantifiedGrape(qGrape)
    suspend fun updateQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef) =
        qGrapeDao.updateQuantifiedGrape(qGrape)
    suspend fun deleteQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef) =
        qGrapeDao.deleteQuantifiedGrape(qGrape)

    fun getQGrapesForBottle(bottleId: Long) = qGrapeDao.getQGrapesForBottle(bottleId)
    fun getQGrapesAndGrapeForBottle(bottleId: Long) = qGrapeDao.getQGrapesAndGrapeForBottle(bottleId)
    suspend fun getQGrapesForBottleNotLive(bottleId: Long) = qGrapeDao.getQGrapesForBottleNotLive(bottleId)
    suspend fun getQGrape(bottleId: Long, grapeId: Long) = qGrapeDao.getQGrape(bottleId, grapeId)

    suspend fun updateReview(review: Review) = reviewDao.updateReview(review)
    suspend fun deleteReview(review: Review) = reviewDao.deleteReview(review)
    suspend fun insertReview(review: Review) = reviewDao.insertReview(review)

    fun getAllWines() = wineDao.getAllWines()
    fun getWineByIdNotLive(wineId: Long) = wineDao.getWineByIdNotLive(wineId)
    fun getWineWithBottles() = wineDao.getWineWithBottles()
    fun getWineWithBottlesNotLive() = wineDao.getWineWithBottlesNotLive()
    fun getWineWithBottlesByCounty(countyId: Long) = wineDao.getWineWithBottlesByCounty(countyId)

    fun getAllCounties() = countyDao.getAllCounties()
    fun getAllCountiesNotLive() = countyDao.getAllCountiesNotLive()

    fun getAllBottles() = bottleDao.getAllBottles()
    suspend fun getBottleByIdNotLive(bottleId: Long) = bottleDao.getBottleByIdNotLive(bottleId)
    suspend fun getAllGrapesNotLive() = bottleDao.getAllGrapesNotLive()

    suspend fun updateBottle(bottle: Bottle) = bottleDao.updateBottle(bottle)
    suspend fun deleteBottleAndChildsById(bottleId: Long) {
        bottleDao.deleteBottleById(bottleId)
        qGrapeDao.deleteQGrapeForBottle(bottleId)
        // TODO: Tasting entry

        // TODO: Reviews
    }

    suspend fun getBottlesAndWineNotLive() = bottleDao.getBottlesAndWineNotLive()
    suspend fun getBottleWithQGrapesNotLive() = bottleDao.getBottleWithQGrapesNotLive()

    fun getAllGrapes() = grapeDao.getAllGrapes()

    fun getAllReviews() = reviewDao.getAllReviews()

    fun getCountiesWithWinesNotLive() = countyDao.getCountiesWithWinesNotLive()

}
