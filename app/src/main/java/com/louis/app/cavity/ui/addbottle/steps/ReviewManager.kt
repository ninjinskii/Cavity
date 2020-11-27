package com.louis.app.cavity.ui.addbottle.steps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReviewManager (
    private val repository: WineRepository,
    private val _userFeedback: MutableLiveData<Event<Int>>,
    private val viewModelScope: CoroutineScope
) {
    private val _reviews = MutableLiveData<MutableList<Review>>()
    val reviews: LiveData<MutableList<Review>>
        get() = _reviews

    fun addReview(contestName: String, typeToVal: Pair<ReviewType, Int>) {
        if (contestName.isEmpty()) {
            _userFeedback.postOnce(R.string.empty_contest_name)
            return
        }

        if (alreadyContainsReview(contestName)) {
            _userFeedback.postOnce(R.string.contest_name_already_exist)
            return
        }

        val review: Review? = when (typeToVal.first) {
            ReviewType.RATE_20 -> {
                if (checkRateInBounds(typeToVal.second, 20)) {
                    Review(0, contestName, 0, 0, 1, 0, typeToVal.second, -1)
                } else {
                    _userFeedback.postOnce(R.string.rate_out_of_bounds)
                    null
                }
            }
            ReviewType.RATE_100 -> {
                if (checkRateInBounds(typeToVal.second, 100)) {
                    Review(0, contestName, 0, 0, 0, 1, typeToVal.second, -1)
                } else {
                    _userFeedback.postOnce(R.string.rate_out_of_bounds)
                    null
                }
            }
            ReviewType.MEDAL -> Review(0, contestName, 1, 0, 0, 0, typeToVal.second, -1)
            else -> Review(0, contestName, 0, 1, 0, 0, typeToVal.second, -1)
        }

        review?.let { adv -> _reviews += adv }
    }

    fun removeReview(review: Review) {
        // Deleted review might already be in database, need to remove it
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteReview(review)
        }

        _reviews -= review
    }

    fun postValue(value: MutableList<Review>) {
        _reviews.postValue(value)
    }

    fun reset() {
        _reviews.postValue(mutableListOf())
    }

    private fun checkRateInBounds(rate: Int, max: Int) = rate in 0..max

    private fun alreadyContainsReview(contestName: String): Boolean {
        val reviewsName = _reviews.value?.map { it.contestName } ?: return false
        return contestName in reviewsName
    }
}
