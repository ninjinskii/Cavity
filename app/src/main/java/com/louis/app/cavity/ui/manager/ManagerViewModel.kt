package com.louis.app.cavity.ui.manager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ManagerViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    fun getCountiesWithWines() = repository.getCountiesWithWines()

    fun getGrapeWithQuantifiedGrapes() = repository.getGrapeWithQuantifiedGrapes()

    fun getReviewWithFilledReviews() = repository.getReviewWithFilledReviews()

    fun addCounty(countyName: String) {
        viewModelScope.launch(IO) {
            if (countyName.isNotEmpty()) {
                val counties = repository.getAllCountiesNotLive().map { it.name }

                if (countyName !in counties) {
                    repository.insertCounty(County(name = countyName, prefOrder = counties.size))
                    _userFeedback.postOnce(R.string.county_added)
                } else {
                    _userFeedback.postOnce(R.string.county_already_exist)
                }
            } else {
                _userFeedback.postOnce(R.string.empty_county_name)
            }
        }
    }

    fun updateCounty(county: County) {
        viewModelScope.launch(IO) {
            repository.updateCounty(county)
            _userFeedback.postOnce(R.string.county_renamed)
        }
    }

    fun updateCounties(counties: List<County>) {
        viewModelScope.launch(IO) {
            val currentCounties = repository.getAllCountiesNotLive()

            // Don't trigger observers for nothing
            if (counties != currentCounties) {
                repository.updateCounties(counties)
            }
        }
    }

    fun deleteCounty(countyId: Long) {
        viewModelScope.launch(IO) {
            repository.deleteCounty(countyId)
            _userFeedback.postOnce(R.string.county_deleted)
        }
    }

    fun addGrape(grapeName: String) {
        viewModelScope.launch(IO) {
            val grapes = repository.getAllGrapesNotLive().map { it.name }

            if (grapeName !in grapes) {
                repository.insertGrape(Grape(grapeId = 0, grapeName))
                _userFeedback.postOnce(R.string.grape_added)
            } else {
                _userFeedback.postOnce(R.string.grape_already_exist)
            }
        }
    }

    fun updateGrape(grape: Grape) {
        viewModelScope.launch(IO) {
            repository.updateGrape(grape)
            _userFeedback.postOnce(R.string.grape_renamed)
        }
    }

    fun deleteGrape(grape: Grape) {
        viewModelScope.launch(IO) {
            repository.deleteGrape(grape)
            _userFeedback.postOnce(R.string.grape_deleted)
        }
    }

    fun addReview(contestName: String, type: Int) {
        viewModelScope.launch(IO) {
            val reviews = repository.getAllReviewsNotLive().map { it.contestName }

            if (contestName !in reviews) {
                repository.insertReview(Review(reviewId = 0, contestName, type))
            } else {
                _userFeedback.postOnce(R.string.contest_name_already_exist)
            }
        }
    }

    fun updateReview(review: Review) {
        viewModelScope.launch(IO) {
            repository.updateReview(review)
            _userFeedback.postOnce(R.string.review_renamed)
        }
    }

    fun deleteReview(review: Review) {
        viewModelScope.launch(IO) {
            repository.deleteReview(review)
            _userFeedback.postOnce(R.string.review_deleted)
        }
    }
}
