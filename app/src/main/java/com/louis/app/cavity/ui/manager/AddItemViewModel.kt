package com.louis.app.cavity.ui.manager

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.repository.CountyRepository
import com.louis.app.cavity.domain.repository.FriendRepository
import com.louis.app.cavity.domain.repository.GrapeRepository
import com.louis.app.cavity.domain.repository.ReviewRepository
import com.louis.app.cavity.domain.repository.RepositoryUpsertResult.*
import com.louis.app.cavity.domain.repository.TagRepository
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.model.Tag
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

/**
 * This ViewModel is used to insert simple items, since we can do that from multiple screens across
 * the app.
 */
class AddItemViewModel(app: Application) : AndroidViewModel(app) {
    private val countyRepository = CountyRepository.getInstance(app)
    private val grapeRepository = GrapeRepository.getInstance(app)
    private val reviewRepository = ReviewRepository.getInstance(app)
    private val friendRepository = FriendRepository.getInstance(app)
    private val tagRepository = TagRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    fun insertCounty(countyName: String) {
        viewModelScope.launch(IO) {
            val counties = countyRepository.getAllCountiesNotLive()

            if (checkCountyAlredyExists(counties, countyName)) {
                _userFeedback.postOnce(R.string.county_already_exists)
                return@launch
            }

            val county = County(name = countyName, prefOrder = counties.size)
            val result = countyRepository.insertCounty(county)
            val message = when (result) {
                is Success -> R.string.county_added
                is AlreadyExists -> R.string.county_already_exists
                is InvalidName -> R.string.empty_county_name
                else -> R.string.base_error
            }

            _userFeedback.postOnce(message)
        }
    }

    fun insertGrape(grapeName: String) {
        viewModelScope.launch(IO) {
            val result = grapeRepository.insertGrape(Grape(0, grapeName))
            val message = when (result) {
                is Success -> R.string.grape_added
                is AlreadyExists -> R.string.grape_already_exists
                is InvalidName -> R.string.empty_grape_name
                else -> R.string.base_error
            }

            _userFeedback.postOnce(message)
        }
    }

    fun insertReview(contestName: String, type: Int) {
        viewModelScope.launch(IO) {
            try {
                reviewRepository.insertReview(Review(0, contestName, type))
                _userFeedback.postOnce(R.string.review_added)
            } catch (_: IllegalArgumentException) {
                _userFeedback.postOnce(R.string.empty_contest_name)
            } catch (_: SQLiteConstraintException) {
                _userFeedback.postOnce(R.string.contest_name_already_exists)
            }
        }
    }

    fun insertFriend(nameLastName: String) {
        viewModelScope.launch(IO) {
            try {
                friendRepository.insertFriend(Friend(0, nameLastName, ""))
                _userFeedback.postOnce(R.string.friend_added)
            } catch (_: IllegalArgumentException) {
                _userFeedback.postOnce(R.string.input_error)
            } catch (_: SQLiteConstraintException) {
                _userFeedback.postOnce(R.string.friend_already_exists)
            }
        }
    }

    fun insertTag(tagName: String) {
        viewModelScope.launch(IO) {
            try {
                tagRepository.insertTag(Tag(0, tagName))
            } catch (_: IllegalArgumentException) {
                _userFeedback.postOnce(R.string.empty_tag_name)
            } catch (_: SQLiteConstraintException) {
                _userFeedback.postOnce(R.string.tag_already_exists)
            }
        }
    }

    fun updateTag(tag: Tag) {
        viewModelScope.launch(IO) {
            tagRepository.updateTag(tag)
            _userFeedback.postOnce(R.string.tag_updated)
        }
    }

    private fun checkCountyAlredyExists(counties: List<County>, countyName: String): Boolean {
        val names = counties.map { it.name.lowercase() }
        return countyName.lowercase() in names
    }
}
