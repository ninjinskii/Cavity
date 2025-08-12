package com.louis.app.cavity.ui.addbottle.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.error.SentryErrorReporter
import com.louis.app.cavity.domain.history.HistoryEntryType
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.GrapeRepository
import com.louis.app.cavity.domain.repository.HistoryRepository
import com.louis.app.cavity.domain.repository.ReviewRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.FReview
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.HistoryXFriend
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

    private val bottleRepository = BottleRepository.getInstance(app)
    private val grapeRepository = GrapeRepository.getInstance(app)
    private val reviewRepository = ReviewRepository.getInstance(app)
    private val historyRepository = HistoryRepository.getInstance(app)

    private val errorReporter = SentryErrorReporter.getInstance(app)

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
        if (this.wineId > 0L) {
            return
        }

        this.wineId = wineId

        if (bottleId > 0L) {
            viewModelScope.launch(IO) {
                val bottle = bottleRepository.getBottleByIdNotLive(bottleId)
                _editedBottle.postValue(bottle)

                dateManager = DateManager(bottle)
                grapeManager = GrapeManager(viewModelScope, grapeRepository, bottle, _userFeedback)
                reviewManager =
                    ReviewManager(viewModelScope, reviewRepository, bottle, _userFeedback)
                otherInfoManager =
                    OtherInfoManager(bottle)
            }
        } else {
            dateManager = DateManager(null)
            grapeManager = GrapeManager(viewModelScope, grapeRepository, null, _userFeedback)
            reviewManager = ReviewManager(viewModelScope, reviewRepository, null, _userFeedback)
            otherInfoManager =
                OtherInfoManager(null)
        }
    }

    fun getAllStorageLocations() = bottleRepository.getAllStorageLocations()

    fun submitBottleForm() {
        val step1Bottle = dateManager.partialBottle
        val step4Bottle = otherInfoManager.partialBottle
        val bottle = mergeStep1And4Bottles(step1Bottle, step4Bottle)

        if (bottle == null || step1Bottle == null) {
            _userFeedback.postOnce(R.string.base_error)
            return
        }

        val isEdit = _editedBottle.value != null
        val uiQGrapes = grapeManager.qGrapes.value ?: emptyList()
        val uiFReviews = reviewManager.fReviews.value ?: emptyList()
        val giftedBy = step4Bottle?.giftedBy ?: emptyList()

        try {
            if (!isEdit) {
                val count = step1Bottle.count.coerceAtLeast(1)
                insertBottles(bottle, uiQGrapes, uiFReviews, giftedBy, count)
            } else {
                updateBottle(bottle, uiQGrapes, uiFReviews, giftedBy)
            }
        } catch (e: Exception) {
            errorReporter.captureException(e)
            _userFeedback.postOnce(R.string.base_error)
        }
    }

    private fun insertBottles(
        bottle: Bottle,
        uiQGrapes: List<QGrapeUiModel>,
        uiFReviews: List<FReviewUiModel>,
        givenBy: List<Long>,
        count: Int,
    ) {
        val coercedCount = count.coerceIn(1..MAX_BOTTLE_BATCH_SIZE)
        val message = if (coercedCount > 1) R.string.bottles_added else R.string.bottle_added

        viewModelScope.launch(IO) {
            bottleRepository.transaction {
                repeat(coercedCount) {
                    val bottleId = bottleRepository.insertBottle(bottle)
                    insertBottleMetadata(bottleId, bottle.buyDate, uiQGrapes, uiFReviews, givenBy)
                }
            }

            _completedEvent.postOnce(message)
        }
    }

    private fun updateBottle(
        bottle: Bottle,
        uiQGrapes: List<QGrapeUiModel>,
        uiFReviews: List<FReviewUiModel>,
        givenBy: List<Long>
    ) {
        val message = R.string.bottle_updated

        viewModelScope.launch(IO) {
            bottleRepository.transaction {
                bottleRepository.updateBottle(bottle)
                insertBottleMetadata(bottle.id, bottle.buyDate, uiQGrapes, uiFReviews, givenBy)
            }

            _completedEvent.postOnce(message)
        }
    }

    private suspend fun insertBottleMetadata(
        bottleId: Long,
        buyDate: Long,
        uiQGrapes: List<QGrapeUiModel>,
        uiFReviews: List<FReviewUiModel>,
        givenBy: List<Long>
    ) {
        bottleRepository.transaction {
            val fReviews = uiFReviews.map { FReview(bottleId, it.reviewId, it.value) }
            val qGrapes = uiQGrapes
                .filter { it.percentage > 0 }
                .map { QGrape(bottleId, it.grapeId, it.percentage) }

            bottleRepository.clearAllQGrapesForBottle(bottleId)
            grapeRepository.insertQGrapes(qGrapes)
            reviewRepository.clearAllFReviewsForBottle(bottleId)
            reviewRepository.insertFReviews(fReviews)


            val type = if (givenBy.isNotEmpty()) HistoryEntryType.GIVEN_BY else HistoryEntryType.ADD
            val entry = HistoryEntry(0, buyDate, bottleId, null, "", type, 0)
            historyRepository.clearReplenishmentsForBottle(bottleId)
            val entryId = historyRepository.insertHistoryEntry(entry)

            givenBy.forEach {
                val historyXFriend = HistoryXFriend(entryId, it)
                historyRepository.insertHistoryXFriend(historyXFriend)
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
                step4.storageLocation,
                step4.alcohol,
                consumed = editedBottle.value?.consumed ?: false.toInt()
            )
        } else null
    }

    companion object {
        private const val MAX_BOTTLE_BATCH_SIZE = 50
    }
}
