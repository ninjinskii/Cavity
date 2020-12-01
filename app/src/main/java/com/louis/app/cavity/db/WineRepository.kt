package com.louis.app.cavity.db

import android.app.Application
import com.louis.app.cavity.model.*
import com.louis.app.cavity.model.relation.QuantifiedBottleGrapeXRef
import com.louis.app.cavity.util.L

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

    fun insertWine(wine: Wine) = wineDao.insertWine(wine)
    fun updateWine(wine: Wine) = wineDao.updateWine(wine)
    fun deleteWine(wine: Wine) = wineDao.deleteWine(wine)
    fun deleteWineById(wineId: Long) = wineDao.deleteWineById(wineId)

    fun insertCounty(county: County) = countyDao.insertCounty(county)

    suspend fun insertBottle(bottle: Bottle) = bottleDao.insertBottle(bottle)

    suspend fun updateGrape(grape: Grape) = bottleDao.updateGrape(grape)
    suspend fun deleteGrape(grape: Grape) = bottleDao.deleteGrape(grape)
    suspend fun insertGrape(grape: Grape) = bottleDao.insertGrape(grape)

    suspend fun insertQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef) =
        bottleDao.insertQuantifiedGrape(qGrape)
    suspend fun updateQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef) =
        bottleDao.updateQuantifiedGrape(qGrape)
    suspend fun deleteQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef) =
        bottleDao.deleteQuantifiedGrape(qGrape)

    fun getQGrapesForBottle(bottleId: Long) = bottleDao.getQGrapesForBottle(bottleId)
    fun getQGrapesAndGrapeForBottle(bottleId: Long) = bottleDao.getQGrapesAndGrapeForBottle(bottleId)
    suspend fun getQGrapesForBottleNotLive(bottleId: Long) = bottleDao.getQGrapesForBottleNotLive(bottleId)
    suspend fun getQGrape(bottleId: Long, grapeId: Long) = bottleDao.getQGrape(bottleId, grapeId)

    suspend fun updateReview(review: Review) = bottleDao.updateReview(review)
    suspend fun deleteReview(review: Review) = bottleDao.deleteReview(review)
    suspend fun insertReview(review: Review) = bottleDao.insertReview(review)

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
    suspend fun deleteBottleById(bottleId: Long) = bottleDao.deleteBottleById(bottleId) // TODO: remove orphans and separate daos
    suspend fun getBottlesAndWineNotLive() = bottleDao.getBottlesAndWineNotLive()
    suspend fun getBottleWithQGrapesNotLive() = bottleDao.getBottleWithQGrapesNotLive()

    fun getAllGrapes() = bottleDao.getAllGrapes()

    fun getAllReviews() = bottleDao.getAllReviews()

    fun getCountiesWithWinesNotLive() = countyDao.getCountiesWithWinesNotLive()

}
