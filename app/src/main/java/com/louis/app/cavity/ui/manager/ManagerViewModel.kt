package com.louis.app.cavity.ui.manager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.Review
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ManagerViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    fun getCountiesWithWines() = repository.getCountiesWithWines()

    fun getGrapeWithQuantifiedGrapes() = repository.getGrapeWithQuantifiedGrapes()

    fun getReviewWithFilledReviews() = repository.getReviewWithFilledReviews()

    fun updateCounty(county: County) {
        viewModelScope.launch(IO) {
            repository.updateCounty(county)
        }
    }

    fun deleteCounty(countyId: Long) {
        viewModelScope.launch(IO) {
            repository.deleteCounty(countyId)
        }
    }

    fun updateCounties(counties: List<County>) {
        viewModelScope.launch(IO) {
            repository.updateCounties(counties)
        }
    }

    fun updateGrape(grape: Grape) {
        viewModelScope.launch(IO) {
            repository.updateGrape(grape)
        }
    }

    fun deleteGrape(grape: Grape) {
        viewModelScope.launch(IO) {
            repository.deleteGrape(grape)
        }
    }

    fun updateReview(review: Review) {
        viewModelScope.launch(IO) {
            repository.updateReview(review)
        }
    }

    fun deleteReview(review: Review) {
        viewModelScope.launch(IO) {
            repository.deleteReview(review)
        }
    }
}
