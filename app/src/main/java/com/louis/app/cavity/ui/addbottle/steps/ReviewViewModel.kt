package com.louis.app.cavity.ui.addbottle.steps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.model.relation.FilledBottleReviewXRef
import com.louis.app.cavity.util.Event
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ReviewViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)
    private val fReviewManager = FilledReviewManager()

    private val _reviewDialogEvent = MutableLiveData<Event<List<String>>>()
    val reviewDialogEvent: LiveData<Event<List<String>>>
        get() = _reviewDialogEvent

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private var bottleId = 0L

    fun start(bottleId: Long) {
        this.bottleId = bottleId
    }

    fun getFReviewAndReview() = repository.getFReviewAndReviewForBottle(bottleId)

    fun insertFReview(bottleId: Long, reviewId: Long, value: Int) {
        viewModelScope.launch(IO) {
            repository.insertFilledReview(FilledBottleReviewXRef(bottleId, reviewId, value))
        }
    }

    data class CheckableReview(val review: Review, var isChecked: Boolean)
}
