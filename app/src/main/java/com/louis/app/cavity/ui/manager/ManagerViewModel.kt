package com.louis.app.cavity.ui.manager

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
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

    var friendPickingImage: Friend? = null

    fun getCountiesWithWines() = repository.getCountiesWithWines()

    fun getNamingsWithWines() = repository.getNamingsWithWines()

    fun getGrapeWithQuantifiedGrapes() = repository.getGrapeWithQuantifiedGrapes()

    fun getReviewWithFilledReviews() = repository.getReviewWithFilledReviews()

    fun getAllFriends() = repository.getAllFriends()

    fun insertCounty(countyName: String) {
        viewModelScope.launch(IO) {
            val counties = repository.getAllCountiesNotLive()

            try {
                repository.insertCounty(County(name = countyName, prefOrder = counties.size))
                _userFeedback.postOnce(R.string.county_added)
            } catch (e: IllegalArgumentException) {
                _userFeedback.postOnce(R.string.empty_county_name)
            } catch (e: SQLiteConstraintException) {
                _userFeedback.postOnce(R.string.county_already_exists)
            }
        }
    }

    fun updateCounty(county: County) {
        viewModelScope.launch(IO) {
            try {
                repository.updateCounty(county)
                _userFeedback.postOnce(R.string.county_renamed)
            } catch (e: IllegalArgumentException) {
                _userFeedback.postOnce(R.string.empty_county_name)
            } catch (e: SQLiteConstraintException) {
                // TODO: Log all SQLITEConstraint exception, to ensure that it is a Unique constraint fail and not something else
                _userFeedback.postOnce(R.string.county_already_exists)
            }
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

    fun updateNaming(naming: Naming) {
        viewModelScope.launch(IO) {
            repository.updateNaming(naming)
        }
    }

    fun deleteNaming(naming: Naming) {
        viewModelScope.launch(IO) {
            try {
                repository.deleteNaming(naming)
            } catch (e: SQLiteConstraintException) {
                _userFeedback.postOnce(R.string.cannot_delete_naming)
            }
        }
    }

    fun insertGrape(grapeName: String) {
        viewModelScope.launch(IO) {
            try {
                repository.insertGrape(Grape(0, grapeName))
                _userFeedback.postOnce(R.string.grape_added)
            } catch (e: IllegalArgumentException) {
                _userFeedback.postOnce(R.string.empty_grape_name)
            } catch (e: SQLiteConstraintException) {
                _userFeedback.postOnce(R.string.grape_already_exists)
            }
        }
    }

    fun updateGrape(grape: Grape) {
        viewModelScope.launch(IO) {
            try {
                repository.updateGrape(grape)
                _userFeedback.postOnce(R.string.grape_renamed)
            } catch (e: IllegalArgumentException) {
                _userFeedback.postOnce(R.string.empty_grape_name)
            } catch (e: SQLiteConstraintException) {
                _userFeedback.postOnce(R.string.grape_already_exists)
            }
        }
    }

    fun deleteGrape(grape: Grape) {
        viewModelScope.launch(IO) {
            repository.deleteGrape(grape)
            _userFeedback.postOnce(R.string.grape_deleted)
        }
    }

    fun insertReview(contestName: String, type: Int) {
        viewModelScope.launch(IO) {
            try {
                repository.insertReview(Review(0, contestName, type))
                _userFeedback.postOnce(R.string.review_added)
            } catch (e: IllegalArgumentException) {
                _userFeedback.postOnce(R.string.empty_contest_name)
            } catch (e: SQLiteConstraintException) {
                _userFeedback.postOnce(R.string.contest_name_already_exists)
            }
        }
    }

    fun updateReview(review: Review) {
        viewModelScope.launch(IO) {
            try {
                repository.updateReview(review)
                _userFeedback.postOnce(R.string.review_added)
            } catch (e: IllegalArgumentException) {
                _userFeedback.postOnce(R.string.empty_contest_name)
            } catch (e: SQLiteConstraintException) {
                _userFeedback.postOnce(R.string.contest_name_already_exists)
            }
        }
    }

    fun deleteReview(review: Review) {
        viewModelScope.launch(IO) {
            repository.deleteReview(review)
            _userFeedback.postOnce(R.string.review_deleted)
        }
    }

    fun insertFriend(nameLastName: String) {
        viewModelScope.launch(IO) {
            try {
                repository.insertFriend(Friend(0, nameLastName, ""))
                _userFeedback.postOnce(R.string.friend_added)
            } catch (e: IllegalArgumentException) {
                _userFeedback.postOnce(R.string.input_error)
            } catch (e: SQLiteConstraintException) {
                _userFeedback.postOnce(R.string.friend_already_exists)
            }
        }
    }

    fun updateFriend(friend: Friend, newName: String) {
        viewModelScope.launch(IO) {
            try {
                val newFriend = friend.copy(name = newName)
                repository.updateFriend(newFriend)
                _userFeedback.postOnce(R.string.friend_added)
            } catch (e: IllegalArgumentException) {
                _userFeedback.postOnce(R.string.input_error)
            } catch (e: SQLiteConstraintException) {
                _userFeedback.postOnce(R.string.friend_already_exists)
            }
        }
    }

    fun deleteFriend(friend: Friend) {
        viewModelScope.launch(IO) {
            repository.deleteFriend(friend)
            _userFeedback.postOnce(R.string.friend_deleted)
        }
    }

    fun setImageForCurrentFriend(imagePath: String) {
        viewModelScope.launch(IO) {
            friendPickingImage?.copy(imgPath = imagePath)?.let {
                repository.updateFriend(it)
            }

            friendPickingImage = null
        }
    }
}
