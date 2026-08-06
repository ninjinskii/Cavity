package com.louis.app.cavity.ui.addbottle.viewmodel

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.error.ErrorReporterFactory
import com.louis.app.cavity.domain.error.SentryErrorReporter
import com.louis.app.cavity.domain.history.HistoryEntryType
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.GrapeRepository
import com.louis.app.cavity.domain.repository.HistoryRepository
import com.louis.app.cavity.domain.repository.ReviewRepository
import com.louis.app.cavity.domain.repository.TagRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.FReview
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.HistoryXFriend
import com.louis.app.cavity.model.QGrape
import com.louis.app.cavity.model.Tag
import com.louis.app.cavity.model.TagXBottle
import com.louis.app.cavity.ui.BaseViewModel
import com.louis.app.cavity.ui.UiEvent
import com.louis.app.cavity.ui.UiEventManager
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed interface AddBottleEvent {
    data class Completed(@StringRes val resId: Int) : AddBottleEvent
    data class UserFeedback(@StringRes val resId: Int) : AddBottleEvent
}

data class AddBottleUiState(
    val editedBottle: Bottle? = null
)

class AddBottleViewModel(app: Application) : BaseViewModel<AddBottleUiState, AddBottleEvent>(app, AddBottleUiState()) {
    lateinit var dateManager: DateManager
    lateinit var grapeManager: GrapeManager
    lateinit var reviewManager: ReviewManager
    lateinit var otherInfoManager: OtherInfoManager

    private val bottleRepository = BottleRepository.getInstance(app)
    private val grapeRepository = GrapeRepository.getInstance(app)
    private val reviewRepository = ReviewRepository.getInstance(app)
    private val historyRepository = HistoryRepository.getInstance(app)
    private val tagRepository = TagRepository.getInstance(app)

    private val errorReporter = ErrorReporterFactory.create(app)

    private val _bottleId = MutableStateFlow(0L)

    val editedBottleHistoryEntry = _bottleId
        .flatMapLatest { historyRepository.getReplenishmentForBottleNotPaged(it) }

    val buyLocations = bottleRepository.getAllBuyLocations()

    private var wineId = 0L

    private fun onFeedback(@StringRes resId: Int) {
        emitEvent(AddBottleEvent.UserFeedback(resId))
    }

    fun start(wineId: Long, bottleId: Long) {
        // Already started
        if (this.wineId > 0L) {
            return
        }

        this.wineId = wineId

        if (bottleId > 0L) {
            viewModelScope.launch(IO) {
                val bottle = bottleRepository.getBottleByIdNotLive(bottleId)
                viewState = viewState.copy(editedBottle = bottle)
                _bottleId.value = bottleId

                dateManager = DateManager(bottle)
                grapeManager = GrapeManager(viewModelScope, grapeRepository, bottle, ::onFeedback)
                reviewManager = ReviewManager(viewModelScope, reviewRepository, bottle, ::onFeedback)
                otherInfoManager = OtherInfoManager(bottle, tagRepository)
            }
        } else {
            dateManager = DateManager(null)
            grapeManager = GrapeManager(viewModelScope, grapeRepository, null, ::onFeedback)
            reviewManager = ReviewManager(viewModelScope, reviewRepository, null, ::onFeedback)
            otherInfoManager = OtherInfoManager(null, tagRepository)
        }
    }

    fun getAllStorageLocations() = bottleRepository.getAllStorageLocations()

    fun submitBottleForm() {
        val step1Bottle = dateManager.partialBottle
        val step4Bottle = otherInfoManager.partialBottle
        val bottle = mergeStep1And4Bottles(step1Bottle, step4Bottle)

        if (bottle == null || step1Bottle == null) {
            emitEvent(AddBottleEvent.UserFeedback(R.string.base_error))
            return
        }

        val isEdit = viewState.editedBottle != null
        val uiQGrapes = grapeManager.qGrapes.value ?: emptyList()
        val uiFReviews = reviewManager.fReviews.value ?: emptyList()
        val giftedBy = step4Bottle?.giftedBy ?: emptyList()
        val tags = step4Bottle?.tags ?: emptyList()

        try {
            if (!isEdit) {
                val count = step1Bottle.count.coerceAtLeast(1)
                insertBottles(bottle, uiQGrapes, uiFReviews, giftedBy, tags, count)
            } else {
                updateBottle(bottle, uiQGrapes, uiFReviews, giftedBy, tags)
            }
        } catch (e: Exception) {
            errorReporter.captureException(e)
            emitEvent(AddBottleEvent.UserFeedback(R.string.base_error))
        }
    }

    private fun insertBottles(
        bottle: Bottle,
        uiQGrapes: List<QGrapeUiModel>,
        uiFReviews: List<FReviewUiModel>,
        givenBy: List<Long>,
        tags: List<Tag>,
        count: Int
    ) {
        val coercedCount = count.coerceIn(1..MAX_BOTTLE_BATCH_SIZE)
        val message = if (coercedCount > 1) R.string.bottles_added else R.string.bottle_added

        viewModelScope.launch(IO) {
            bottleRepository.transaction {
                repeat(coercedCount) {
                    val bottleId = bottleRepository.insertBottle(bottle)
                    insertBottleMetadata(bottleId, bottle.buyDate, uiQGrapes, uiFReviews, givenBy, tags)
                }
            }

            emitEvent(AddBottleEvent.Completed(message))
        }
    }

    private fun updateBottle(
        bottle: Bottle,
        uiQGrapes: List<QGrapeUiModel>,
        uiFReviews: List<FReviewUiModel>,
        givenBy: List<Long>,
        tags: List<Tag>
    ) {
        val message = R.string.bottle_updated

        viewModelScope.launch(IO) {
            bottleRepository.transaction {
                bottleRepository.updateBottle(bottle)
                insertBottleMetadata(bottle.id, bottle.buyDate, uiQGrapes, uiFReviews, givenBy, tags)
            }

            emitEvent(AddBottleEvent.Completed(message))
        }
    }

    private suspend fun insertBottleMetadata(
        bottleId: Long,
        buyDate: Long,
        uiQGrapes: List<QGrapeUiModel>,
        uiFReviews: List<FReviewUiModel>,
        givenBy: List<Long>,
        tags: List<Tag>
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
            historyRepository.insertFriendHistoryXRefs(givenBy.map { HistoryXFriend(entryId, it) })

            tagRepository.clearTagsForBottle(bottleId)
            tagRepository.insertTagBottleXRefs(tags.map { TagXBottle(it.id, bottleId) })
        }
    }

    private fun mergeStep1And4Bottles(
        step1: DateManager.Step1Bottle?,
        step4: OtherInfoManager.Step4Bottle?
    ): Bottle? {
        return if (step1 != null && step4 != null) {
            Bottle(
                id = viewState.editedBottle?.id ?: 0,
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
                consumed = viewState.editedBottle?.consumed ?: false.toInt()
            )
        } else null
    }

    companion object {
        private const val MAX_BOTTLE_BATCH_SIZE = 50
    }
}
