package com.louis.app.cavity.ui.manager

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.repository.CountyRepository
import com.louis.app.cavity.domain.repository.FriendRepository
import com.louis.app.cavity.domain.repository.GrapeRepository
import com.louis.app.cavity.domain.repository.RepositoryUpsertResult.*
import com.louis.app.cavity.domain.repository.ReviewRepository
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

sealed interface ManagerEvent {
    data class UserFeedback(@StringRes val resId: Int) : ManagerEvent
}

data class ManagerUiState(val placeholder: Unit = Unit)

class ManagerViewModel(app: Application) : BaseViewModel<ManagerUiState, ManagerEvent>(app, ManagerUiState()) {
    private val countyRepository = CountyRepository.getInstance(app)
    private val grapeRepository = GrapeRepository.getInstance(app)
    private val reviewRepository = ReviewRepository.getInstance(app)
    private val friendRepository = FriendRepository.getInstance(app)

    var friendPickingImage: Friend? = null

    fun getCountiesWithWines() = countyRepository.getCountiesWithWines()

    fun getGrapeWithQuantifiedGrapes() = grapeRepository.getGrapeWithQuantifiedGrapes()

    fun getReviewWithFilledReviews() = reviewRepository.getReviewWithFilledReviews()

    fun getAllFriends() = friendRepository.getAllFriends()

    fun updateCounty(county: County) {
        viewModelScope.launch(IO) {
            val result = countyRepository.updateCounty(county)
            val message = when (result) {
                is Success -> R.string.county_renamed
                is AlreadyExists -> R.string.county_already_exists
                is InvalidName -> R.string.empty_county_name
                else -> R.string.base_error
            }

            emitEvent(ManagerEvent.UserFeedback(message))
        }
    }

    fun updateCounties(counties: List<County>) {
        viewModelScope.launch(IO) {
            val currentCounties = countyRepository.getAllCountiesNotLive()

            // Don't trigger observers for nothing
            if (counties != currentCounties) {
                countyRepository.updateCounties(counties)
            }
        }
    }

    fun deleteCounty(countyId: Long) {
        viewModelScope.launch(IO) {
            countyRepository.deleteCounty(countyId)
            emitEvent(ManagerEvent.UserFeedback(R.string.county_deleted))
        }
    }

    fun updateGrape(grape: Grape) {
        viewModelScope.launch(IO) {
            val result = grapeRepository.updateGrape(grape)
            val message = when (result) {
                is Success -> R.string.grape_renamed
                is AlreadyExists -> R.string.grape_already_exists
                is InvalidName -> R.string.empty_grape_name
                else -> R.string.base_error
            }

            emitEvent(ManagerEvent.UserFeedback(message))
        }
    }

    fun deleteGrape(grape: Grape) {
        viewModelScope.launch(IO) {
            grapeRepository.deleteGrape(grape)
            emitEvent(ManagerEvent.UserFeedback(R.string.grape_deleted))
        }
    }

    fun updateReview(review: Review) {
        viewModelScope.launch(IO) {
            try {
                reviewRepository.updateReview(review)
                emitEvent(ManagerEvent.UserFeedback(R.string.review_renamed))
            } catch (e: IllegalArgumentException) {
                emitEvent(ManagerEvent.UserFeedback(R.string.empty_contest_name))
            } catch (e: SQLiteConstraintException) {
                emitEvent(ManagerEvent.UserFeedback(R.string.contest_name_already_exists))
            }
        }
    }

    fun deleteReview(review: Review) {
        viewModelScope.launch(IO) {
            reviewRepository.deleteReview(review)
            emitEvent(ManagerEvent.UserFeedback(R.string.review_deleted))
        }
    }

    fun updateFriend(friend: Friend, newName: String) {
        viewModelScope.launch(IO) {
            try {
                val newFriend = friend.copy(name = newName)
                friendRepository.updateFriend(newFriend)
                emitEvent(ManagerEvent.UserFeedback(R.string.friend_renamed))
            } catch (e: IllegalArgumentException) {
                emitEvent(ManagerEvent.UserFeedback(R.string.input_error))
            } catch (e: SQLiteConstraintException) {
                emitEvent(ManagerEvent.UserFeedback(R.string.friend_already_exists))
            }
        }
    }

    fun deleteFriend(friend: Friend) {
        viewModelScope.launch(IO) {
            friendRepository.deleteFriend(friend)
            emitEvent(ManagerEvent.UserFeedback(R.string.friend_deleted))
        }
    }

    fun setImageForCurrentFriend(imagePath: String) {
        viewModelScope.launch(IO) {
            friendPickingImage?.copy(imgPath = imagePath)?.let {
                friendRepository.updateFriend(it)
            }

            friendPickingImage = null
        }
    }
}
