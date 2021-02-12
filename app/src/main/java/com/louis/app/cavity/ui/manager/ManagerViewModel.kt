package com.louis.app.cavity.ui.manager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.*
import com.louis.app.cavity.util.Event
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

    fun getAllFriends() = repository.getAllFriends()

    fun addCounty(countyName: String) {
        if (countyName.isBlank()) {
            _userFeedback.postOnce(R.string.empty_county_name)
            return
        }

        viewModelScope.launch(IO) {
            val counties = repository.getAllCountiesNotLive().map { it.name }

            if (countyName !in counties) {
                repository.insertCounty(County(name = countyName, prefOrder = counties.size))
                _userFeedback.postOnce(R.string.county_added)
            } else {
                _userFeedback.postOnce(R.string.county_already_exist)
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
        if (grapeName.isBlank()) {
            _userFeedback.postOnce(R.string.empty_grape_name)
            return
        }

        viewModelScope.launch(IO) {
            if (isGrapeNameUnique(grapeName)) {
                repository.insertGrape(Grape(0, grapeName))
                _userFeedback.postOnce(R.string.grape_added)

            } else {
                _userFeedback.postOnce(R.string.grape_already_exist)
            }
        }
    }

    fun updateGrape(grape: Grape) {
        if (grape.name.isBlank()) {
            _userFeedback.postOnce(R.string.empty_grape_name)
            return
        }

        viewModelScope.launch(IO) {
            if (isGrapeNameUnique(grape.name)) {
                repository.updateGrape(grape)
                _userFeedback.postOnce(R.string.grape_renamed)
            } else {
                _userFeedback.postOnce(R.string.grape_already_exist)
            }
        }
    }

    fun deleteGrape(grape: Grape) {
        viewModelScope.launch(IO) {
            repository.deleteGrape(grape)
            _userFeedback.postOnce(R.string.grape_deleted)
        }
    }

    fun addReview(contestName: String, type: Int) {
        if (contestName.isBlank()) {
            _userFeedback.postOnce(R.string.empty_contest_name)
            return
        }

        viewModelScope.launch(IO) {
            val reviews = repository.getAllReviewsNotLive().map { it.contestName }

            if (contestName !in reviews) {
                repository.insertReview(Review(0, contestName, type))
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

    fun insertFriend(nameLastName: String) {
        if (nameLastName.isBlank()) {
            _userFeedback.postOnce(R.string.input_error)
            return
        }

        val name = Friend.parseName(nameLastName)

        viewModelScope.launch(IO) {
            repository.insertFriend(Friend(0, name.first, name.second, ""))
            _userFeedback.postOnce(R.string.friend_added)
        }
    }

    fun updateFriend(friend: Friend, input: String) {
        val name = Friend.parseName(input)
        val updatedFriend = friend.copy(firstName = name.first, lastName = name.second)

        viewModelScope.launch(IO) {
            repository.updateFriend(updatedFriend)
            _userFeedback.postOnce(R.string.friend_renamed)
        }
    }

    fun deleteFriend(friend: Friend) {
        viewModelScope.launch(IO) {
            repository.deleteFriend(friend)
            _userFeedback.postOnce(R.string.friend_deleted)
        }
    }

    private suspend fun isGrapeNameUnique(grapeName: String): Boolean {
        val grapes = repository.getAllGrapesNotLive().map { it.name }
        return grapeName !in grapes
    }

    private fun handleGrape(grape: Grape) {
        if (grape.name.isBlank()) {
            _userFeedback.postOnce(R.string.empty_grape_name)
            return
        }

        viewModelScope.launch(IO) {
            val grapes = repository.getAllGrapesNotLive().map { it.name }

            if (grape.name in grapes) {
                _userFeedback.postOnce(R.string.grape_already_exist)
            } else {
                if (grape.id == 0L) {
                    repository.insertGrape(grape)
                    _userFeedback.postOnce(R.string.grape_added)
                } else {
                    repository.updateGrape(grape)
                    _userFeedback.postOnce(R.string.grape_renamed)
                }
            }

        }
    }

    private suspend fun handleGrape(item: Chipable): Int {
        if (item.getChipText().isBlank()) {
            return -1
        }

        val items = when(item) {
            is County -> repository.getAllCountiesNotLive().map { it.name }
            is Grape -> repository.getAllGrapesNotLive().map { it.name }
            is Review -> repository.getAllReviewsNotLive().map { it.contestName }
            else -> repository.getAllFriendsNotLive().map { it.lastName + it.firstName }
        }

        return if (item.getChipText() in items) {
            0
        } else {
            if (item.getItemId() == 0L) {
                1
            } else {
                2
            }
        }
    }
}
