package com.louis.app.cavity.ui.addbottle.viewmodel

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.relation.crossref.FilledBottleReviewXRef
import com.louis.app.cavity.model.relation.crossref.QuantifiedBottleGrapeXRef
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AddBottleViewModel(app: Application) : AndroidViewModel(app) {
    // lazy ?
    lateinit var dateManager: DateManager
    lateinit var grapeManager: GrapeManager
    lateinit var reviewManager: ReviewManager
    lateinit var otherInfoManager: OtherInfoManager

    private val repository = WineRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private val _editedBottle = MutableLiveData<Bottle>()
    val editedBottle: LiveData<Bottle>
        get() = _editedBottle

    private var wineId = 0L

    fun start(wineId: Long, bottleId: Long) {
        // Already started
        if (this.wineId != 0L) return

        this.wineId = wineId

        viewModelScope.launch(IO) {
            val bottle: Bottle? =
                if (bottleId > 0) repository.getBottleByIdNotLive(bottleId) else null

            _editedBottle.postValue(bottle)

            dateManager = DateManager(viewModelScope, repository, bottle)
            grapeManager = GrapeManager(viewModelScope, repository, bottle, _userFeedback)
            reviewManager = ReviewManager(viewModelScope, repository, bottle, _userFeedback)
            otherInfoManager = OtherInfoManager(viewModelScope, repository, bottle, _userFeedback)
        }
    }

    fun insertBottle() {
        val step1Bottle = dateManager.partialBottle
        val step4Bottle = otherInfoManager.partialBottle
        val bottle = mergeStep1And4Bottles(step1Bottle, step4Bottle)

        if (bottle == null) {
            _userFeedback.postOnce(R.string.base_error)
            return
        }

        viewModelScope.launch(IO) {
            val bottleId: Long

            if (_editedBottle.value == null) {
                bottleId = repository.insertBottle(bottle)
            } else {
                bottleId = _editedBottle.value!!.id
                repository.updateBottle(_editedBottle.value!!)
            }

            insertQGrapes(bottleId)
            insertFReviews(bottleId)
            insertHistoryEntry(bottleId, bottle.buyDate, step4Bottle?.giftedBy)
        }

    }

    private suspend fun insertQGrapes(bottleId: Long) {
        val uiQGrapes = grapeManager.qGrapes.value ?: emptyList()
        val qGrapes = uiQGrapes.map {
            QuantifiedBottleGrapeXRef(bottleId, it.grapeId, it.percentage)
        }

        repository.replaceQGrapesForBottle(bottleId, qGrapes)
    }

    private suspend fun insertFReviews(bottleId: Long) {
        val uiFReviews = reviewManager.fReviews.value ?: emptyList()
        val fReviews = uiFReviews.map {
            FilledBottleReviewXRef(bottleId, it.reviewId, it.value)
        }

        repository.replaceFReviewsForBottle(bottleId, fReviews)
    }

    private suspend fun insertHistoryEntry(bottleId: Long, buyDate: Long, friendId: Long?) {
        if (friendId == null) {
            val typeReplenishment = 1
            val historyEntry =
                HistoryEntry(0, buyDate, bottleId, null, "", typeReplenishment, 0)
            repository.insertHistoryEntry(historyEntry)
        } else {
            val typeGiftedBy = 3
            val historyEntry =
                HistoryEntry(0, buyDate, bottleId, null, "", typeGiftedBy, 0)
            repository.declareGiftedBottle(historyEntry, friendId)
        }
    }

    private fun mergeStep1And4Bottles(
        step1: DateManager.Step1Bottle?,
        step4: OtherInfoManager.Step4Bottle?
    ): Bottle? {
        return if (step1 != null && step4 != null) {
            Bottle(
                id = _editedBottle.value?.id ?: 0,
                wineId,
                step1.vintage,
                step1.apogee,
                step4.isFavorite,
                1,
                step1.price,
                step1.currency,
                step4.otherInfo,
                step1.location,
                step1.buyDate,
                "",
                step4.pdfPath,
                consumed = editedBottle.value?.consumed ?: false.toInt()
            )
        } else null
    }

    private fun postFeedback(@StringRes stringRes: Int) {
        _userFeedback.postOnce(stringRes)
    }
}
