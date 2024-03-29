package com.louis.app.cavity.ui.addbottle.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.FReview
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.QGrape
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AddBottleViewModel(app: Application) : AndroidViewModel(app) {
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

    private val _completedEvent = MutableLiveData<Event<Int>>()
    val completedEvent: LiveData<Event<Int>>
        get() = _completedEvent

    val editedBottleHistoryEntry = _editedBottle.switchMap {
        repository.getReplenishmentForBottleNotPaged(it?.id ?: 0)
    }

    val buyLocations = repository.getAllBuyLocations()

    private var wineId = 0L

    fun start(wineId: Long, bottleId: Long) {
        // Already started
        if (this.wineId != 0L) {
            return
        }

        this.wineId = wineId

        if (bottleId != 0L) {
            viewModelScope.launch(IO) {
                val bottle = repository.getBottleByIdNotLive(bottleId)
                _editedBottle.postValue(bottle)

                dateManager = DateManager(bottle)
                grapeManager = GrapeManager(viewModelScope, repository, bottle, _userFeedback)
                reviewManager = ReviewManager(viewModelScope, repository, bottle, _userFeedback)
                otherInfoManager = OtherInfoManager(repository, bottle)
            }
        } else {
            dateManager = DateManager(null)
            grapeManager = GrapeManager(viewModelScope, repository, null, _userFeedback)
            reviewManager = ReviewManager(viewModelScope, repository, null, _userFeedback)
            otherInfoManager = OtherInfoManager(repository, null)
        }
    }

    fun insertBottle() {
        val step1Bottle = dateManager.partialBottle
        val step4Bottle = otherInfoManager.partialBottle
        val bottle = mergeStep1And4Bottles(step1Bottle, step4Bottle)

        if (bottle == null || step1Bottle == null) {
            _userFeedback.postOnce(R.string.base_error)
            return
        }

        viewModelScope.launch(IO) {
            val isEdit = _editedBottle.value != null

            if (!isEdit) {
                val count = step1Bottle.count.coerceAtLeast(1)
                val message = if (count > 1) R.string.bottles_added else R.string.bottle_added

                repository.transaction {
                    for (i in 1..count) {
                        val bottleId = repository.insertBottle(bottle)

                        insertQGrapes(bottleId)
                        insertFReviews(bottleId)
                        insertHistoryEntry(bottleId, bottle.buyDate, step4Bottle?.giftedBy)
                    }
                }

                _completedEvent.postOnce(message)
            } else {
                val message = R.string.bottle_updated
                val bottleId = _editedBottle.value!!.id

                repository.run {
                    transaction {
                        updateBottle(bottle)
                        insertQGrapes(bottleId)
                        insertFReviews(bottleId)
                        insertHistoryEntry(bottleId, bottle.buyDate, step4Bottle?.giftedBy)
                    }
                }

                _completedEvent.postOnce(message)
            }
        }
    }

    private suspend fun insertQGrapes(bottleId: Long) {
        val uiQGrapes = grapeManager.qGrapes.value ?: emptyList()
        val qGrapes = uiQGrapes
            .filter { it.percentage > 0 }
            .map { QGrape(bottleId, it.grapeId, it.percentage) }

        repository.replaceQGrapesForBottle(bottleId, qGrapes)
    }

    private suspend fun insertFReviews(bottleId: Long) {
        val uiFReviews = reviewManager.fReviews.value ?: emptyList()
        val fReviews = uiFReviews.map {
            FReview(bottleId, it.reviewId, it.value)
        }

        repository.replaceFReviewsForBottle(bottleId, fReviews)
    }

    private suspend fun insertHistoryEntry(bottleId: Long, buyDate: Long, friendId: Long?) {
        val isAGift = friendId != null
        val typeReplenishment = 1
        val typeGiftedBy = 3
        val type = if (isAGift) typeGiftedBy else typeReplenishment
        val historyEntry = HistoryEntry(0, buyDate, bottleId, null, "", type, 0)

        repository.run {
            clearExistingReplenishments(bottleId)

            if (isAGift) {
                declareGiftedBottle(historyEntry, friendId!!)
            } else {
                insertHistoryEntry(historyEntry)
            }
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
                step1.price,
                step1.currency,
                step4.otherInfo,
                step1.location,
                step1.buyDate,
                "",
                step4.size,
                step4.pdfPath,
                consumed = editedBottle.value?.consumed ?: false.toInt()
            )
        } else null
    }
}
