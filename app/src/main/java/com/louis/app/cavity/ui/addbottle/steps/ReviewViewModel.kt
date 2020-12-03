package com.louis.app.cavity.ui.addbottle.steps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.relation.FilledBottleReviewXRef
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ReviewViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    fun getFReviewAndReview() = repository.getFReviewAndReviewForBottle(bottleId = 1)
    fun insertFReview(bottleId: Long, reviewId: Long, value: Int) {
        viewModelScope.launch(IO) {
            repository.insertFilledReview(FilledBottleReviewXRef(bottleId, reviewId, value))
        }
    }

}
