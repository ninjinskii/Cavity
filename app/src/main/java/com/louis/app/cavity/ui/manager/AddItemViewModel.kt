package com.louis.app.cavity.ui.manager

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.annotation.StringRes
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
import com.louis.app.cavity.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

sealed interface AddItemEvent {
    data class UserFeedback(@StringRes val resId: Int) : AddItemEvent
}

data class AddItemUiState(val placeholder: Unit = Unit)

class AddItemViewModel(app: Application) : BaseViewModel<AddItemUiState, AddItemEvent>(app, AddItemUiState()) {
    private val countyRepository = CountyRepository.getInstance(app)
    private val grapeRepository = GrapeRepository.getInstance(app)
    private val reviewRepository = ReviewRepository.getInstance(app)
    private val friendRepository = FriendRepository.getInstance(app)
    private val tagRepository = TagRepository.getInstance(app)

    fun insertCounty(countyName: String) {
        viewModelScope.launch(IO) {
            val counties = countyRepository.getAllCountiesNotLive()

            if (checkCountyAlredyExists(counties, countyName)) {
                emitEvent(AddItemEvent.UserFeedback(R.string.county_already_exists))
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

            emitEvent(AddItemEvent.UserFeedback(message))
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

            emitEvent(AddItemEvent.UserFeedback(message))
        }
    }

    fun insertReview(contestName: String, type: Int) {
        viewModelScope.launch(IO) {
            try {
                reviewRepository.insertReview(Review(0, contestName, type))
                emitEvent(AddItemEvent.UserFeedback(R.string.review_added))
            } catch (_: IllegalArgumentException) {
                emitEvent(AddItemEvent.UserFeedback(R.string.empty_contest_name))
            } catch (_: SQLiteConstraintException) {
                emitEvent(AddItemEvent.UserFeedback(R.string.contest_name_already_exists))
            }
        }
    }

    fun insertFriend(nameLastName: String) {
        viewModelScope.launch(IO) {
            try {
                friendRepository.insertFriend(Friend(0, nameLastName, ""))
                emitEvent(AddItemEvent.UserFeedback(R.string.friend_added))
            } catch (_: IllegalArgumentException) {
                emitEvent(AddItemEvent.UserFeedback(R.string.input_error))
            } catch (_: SQLiteConstraintException) {
                emitEvent(AddItemEvent.UserFeedback(R.string.friend_already_exists))
            }
        }
    }

    fun insertTag(tagName: String) {
        viewModelScope.launch(IO) {
            try {
                tagRepository.insertTag(Tag(0, tagName))
            } catch (_: IllegalArgumentException) {
                emitEvent(AddItemEvent.UserFeedback(R.string.empty_tag_name))
            } catch (_: SQLiteConstraintException) {
                emitEvent(AddItemEvent.UserFeedback(R.string.tag_already_exists))
            }
        }
    }

    fun updateTag(tag: Tag) {
        viewModelScope.launch(IO) {
            tagRepository.updateTag(tag)
            emitEvent(AddItemEvent.UserFeedback(R.string.tag_updated))
        }
    }

    private fun checkCountyAlredyExists(counties: List<County>, countyName: String): Boolean {
        val names = counties.map { it.name.lowercase() }
        return countyName.lowercase() in names
    }
}
