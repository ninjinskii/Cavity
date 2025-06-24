package com.louis.app.cavity.ui.addbottle.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.FriendRepository
import com.louis.app.cavity.domain.repository.GrapeRepository
import com.louis.app.cavity.domain.repository.HistoryRepository
import com.louis.app.cavity.domain.repository.ReviewRepository
import com.louis.app.cavity.model.Bottle
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

    private val bottleRepository = BottleRepository.getInstance(app)
    private val grapeRepository = GrapeRepository.getInstance(app)
    private val reviewRepository = ReviewRepository.getInstance(app)
    private val historyRepository = HistoryRepository.getInstance(app)
    private val friendRepository = FriendRepository.getInstance(app)

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
        historyRepository.getReplenishmentForBottleNotPaged(it?.id ?: 0)
    }

    val buyLocations = bottleRepository.getAllBuyLocations()

    private var wineId = 0L

    fun start(wineId: Long, bottleId: Long) {
        // Already started
        if (this.wineId != 0L) {
            return
        }

        this.wineId = wineId

        if (bottleId != 0L) {
            viewModelScope.launch(IO) {
                val bottle = bottleRepository.getBottleByIdNotLive(bottleId)
                _editedBottle.postValue(bottle)

                dateManager = DateManager(bottle)
                grapeManager = GrapeManager(viewModelScope, grapeRepository, bottle, _userFeedback)
                reviewManager =
                    ReviewManager(viewModelScope, reviewRepository, bottle, _userFeedback)
                otherInfoManager =
                    OtherInfoManager(viewModelScope, friendRepository, historyRepository, bottle)
            }
        } else {
            dateManager = DateManager(null)
            grapeManager = GrapeManager(viewModelScope, grapeRepository, null, _userFeedback)
            reviewManager = ReviewManager(viewModelScope, reviewRepository, null, _userFeedback)
            otherInfoManager =
                OtherInfoManager(viewModelScope, friendRepository, historyRepository, null)
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
            val uiQGrapes = grapeManager.qGrapes.value ?: emptyList()
            val uiFReviews = reviewManager.fReviews.value ?: emptyList()
            val giftedBy = step4Bottle?.giftedBy ?: emptyList()

            if (!isEdit) {
                val count = step1Bottle.count.coerceAtLeast(1)
                val message = if (count > 1) R.string.bottles_added else R.string.bottle_added

                bottleRepository.insertBottles(bottle, uiQGrapes, uiFReviews, giftedBy, count)
                _completedEvent.postOnce(message)
            } else {
                val message = R.string.bottle_updated

                bottleRepository.updateBottle(bottle, uiQGrapes, uiFReviews, giftedBy)
                _completedEvent.postOnce(message)
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
