package com.louis.app.cavity.ui.addbottle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.model.relation.FilledBottleReviewXRef
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ReviewViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _reviewDialogEvent = MutableLiveData<Event<List<CheckableReview>>>()
    val reviewDialogEvent: LiveData<Event<List<CheckableReview>>>
        get() = _reviewDialogEvent

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private var bottleId = 0L

    fun start(bottleId: Long) {
        this.bottleId = bottleId
    }

    fun getFReviewAndReview() = repository.getFReviewAndReviewForBottle(bottleId)

    fun insertReview(contestName: String, type: Int) {
        viewModelScope.launch(IO) {
            val reviews = repository.getAllReviewsNotLive().map { it.contestName }

            if (contestName !in reviews) {
                val id = repository.insertReview(Review(reviewId = 0, contestName, type))
                insertFilledReview(id, getDefaultValue(type))
            } else {
                _userFeedback.postOnce(R.string.contest_name_already_exist)
            }
        }
    }

    private fun insertFilledReview(reviewId: Long, contestValue: Int) {
        val fReview = FilledBottleReviewXRef(bottleId, reviewId, contestValue)

        viewModelScope.launch(IO) {
            repository.insertFilledReview(fReview)
        }
    }

    fun updateFilledReview(fReview: FilledBottleReviewXRef, contestValue: Int) {
        fReview.value = contestValue

        viewModelScope.launch(IO) {
            repository.updateFilledReview(fReview)
        }
    }

    // Delete from recycler view
    fun removeFilledReview(fReview: FilledBottleReviewXRef) {
        viewModelScope.launch(IO) {
            repository.deleteFilledReview(fReview)
        }
    }

    // Delete from dialog
    private fun removeFilledReview(reviewId: Long) {
        viewModelScope.launch(IO) {
            repository.deleteFReviewByPk(bottleId, reviewId)
        }
    }

    fun insertFReview(bottleId: Long, reviewId: Long, value: Int) {
        viewModelScope.launch(IO) {
            repository.insertFilledReview(FilledBottleReviewXRef(bottleId, reviewId, value))
        }
    }

    fun submitCheckedReviews(newCheckedReviews: List<CheckableReview>) {
        for (checkableReview in newCheckedReviews) {
            val (reviewId, _, type) = checkableReview.review
            val oldOne =
                _reviewDialogEvent.value?.peekContent()?.find { it.review.reviewId == reviewId }

            when {
                checkableReview.isChecked && oldOne?.isChecked != true ->
                    insertFilledReview(reviewId, getDefaultValue(type))
                !checkableReview.isChecked && oldOne?.isChecked != false ->
                    removeFilledReview(reviewId)
            }

            // Not updating the value of the _grapeDialogEvent LiveData. This will be done
            // when requestGrapeDialog() is called only
        }
    }

    fun requestReviewDialog() {
        viewModelScope.launch(IO) {
            val reviews = repository.getAllReviewsNotLive()
            val fReviews = repository.getFReviewsForBottleNotLive(bottleId).map { it.reviewId }
            val currentCheckedReviews =
                reviews.map { CheckableReview(it, isChecked = it.reviewId in fReviews) }

            _reviewDialogEvent.postOnce(currentCheckedReviews)
        }
    }

    private fun getDefaultValue(type: Int) = when (type) {
        0 -> 1
        1 -> 15
        2 -> 80
        3 -> 1
        else -> 0
    }

    data class CheckableReview(val review: Review, var isChecked: Boolean)
}
