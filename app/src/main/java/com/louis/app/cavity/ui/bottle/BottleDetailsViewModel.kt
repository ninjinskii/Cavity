package com.louis.app.cavity.ui.bottle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.louis.app.cavity.db.WineRepository

class BottleDetailsViewModel(app: Application) : AndroidViewModel(app) {
    val repository = WineRepository.getInstance(app)

    fun getBottleById(bottleId: Long) = repository.getBottleById(bottleId)

    fun getQGrapesForBottle(bottleId: Long) = repository.getQGrapesAndGrapeForBottle(bottleId)

    fun getFReviewForBottle(bottleId: Long) = repository.getFReviewAndReviewForBottle(bottleId)
}
