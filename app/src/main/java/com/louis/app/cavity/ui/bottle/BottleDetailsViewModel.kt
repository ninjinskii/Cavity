package com.louis.app.cavity.ui.bottle

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.repository.WineRepository
import com.louis.app.cavity.db.dao.BoundedBottle
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.GrapeRepository
import com.louis.app.cavity.domain.repository.HistoryRepository
import com.louis.app.cavity.domain.repository.ReviewRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.ui.BaseViewModel
import com.louis.app.cavity.util.toBoolean
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.louis.app.cavity.domain.repository.TagRepository
import com.louis.app.cavity.model.Tag
import com.louis.app.cavity.model.TagXBottle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

sealed interface BottleDetailsEvent {
    data class OpenPdf(val uri: Uri) : BottleDetailsEvent
    data class UserFeedback(val resId: Int) : BottleDetailsEvent
    data class RevertConsumption(val boundedBottle: BoundedBottle) : BottleDetailsEvent
    data class RemoveFromTasting(val bottleId: Long, val tastingId: Long?) : BottleDetailsEvent
    data class RemoveTag(val tagId: Long, val bottleId: Long?) : BottleDetailsEvent
}

data class BottleDetailsUiState(val placeholder: Unit = Unit)

class BottleDetailsViewModel(app: Application) : BaseViewModel<BottleDetailsUiState, BottleDetailsEvent>(app, BottleDetailsUiState()) {
    private val wineRepository = WineRepository.getInstance(app)
    private val bottleRepository = BottleRepository.getInstance(app)
    private val grapeRepository = GrapeRepository.getInstance(app)
    private val reviewRepository = ReviewRepository.getInstance(app)
    private val historyRepository = HistoryRepository.getInstance(app)
    private val tagRepository = TagRepository.getInstance(app)

    private val _bottleId = MutableStateFlow(0L)

    val bottle = _bottleId.flatMapLatest { bottleRepository.getBottleById(it) }

    val grapes = _bottleId.flatMapLatest { grapeRepository.getQGrapesAndGrapeForBottle(it) }

    val reviews = _bottleId.flatMapLatest { reviewRepository.getFReviewAndReviewForBottle(it) }

    val tags = _bottleId.flatMapLatest { tagRepository.getTagsForBottle(it) }

    val replenishmentEntry = _bottleId.flatMapLatest { historyRepository.getReplenishmentForBottleNotPaged(it) }

    fun getWineById(wineId: Long) = wineRepository.getWineById(wineId)

    fun getBottlesForWine(wineId: Long) =
        bottleRepository.getBottlesForWine(wineId).map { bottles ->
            val showTastingLogIsRelevant = bottles.any { it.bottle.consumed.toBoolean() }
            bottles to showTastingLogIsRelevant
        }
            .flowOn(IO)

    fun setBottleId(bottleId: Long) {
        _bottleId.value = bottleId
    }

    fun getBottleId() = _bottleId.value.takeIf { it > 0L }

    fun deleteBottle() {
        val bottleId = _bottleId.value.takeIf { it > 0L } ?: return
        viewModelScope.launch(IO) {
            val wineId = bottleRepository.getBottleByIdNotLive(bottleId).wineId
            maybeDeleteWine(bottleId, wineId)
            bottleRepository.deleteBottleById(bottleId)
        }
    }

    fun toggleFavorite() {
        val bottleId = _bottleId.value.takeIf { it > 0L } ?: return

        viewModelScope.launch(IO) {
            bottleRepository.run {
                val bottle = getBottleByIdNotLive(bottleId)
                if (bottle.isFavorite.toBoolean()) unfav(bottleId) else fav(bottleId)
            }
        }
    }

    fun removeTag(tag: Tag) {
        val bottleId = _bottleId.value.takeIf { it > 0L } ?: run {
            emitEvent(BottleDetailsEvent.UserFeedback(R.string.base_error))
            return
        }

        viewModelScope.launch(IO) {
            tagRepository.deleteTagBottleXRef(TagXBottle(tag.id, bottleId))
            emitEvent(BottleDetailsEvent.RemoveTag(tag.id, bottleId))
        }
    }

    fun preparePdf() {
        val bottleId = _bottleId.value.takeIf { it > 0L } ?: return

        viewModelScope.launch(IO) {
            val bottle = bottleRepository.getBottleByIdNotLive(bottleId)
            val path = bottle.pdfPath

            if (path.isNotBlank()) {
                emitEvent(BottleDetailsEvent.OpenPdf(path.toUri()))
            } else {
                emitEvent(BottleDetailsEvent.UserFeedback(R.string.no_pdf))
            }
        }
    }

    fun clearPdfPath() {
        val bottleId = _bottleId.value.takeIf { it > 0L } ?: return
        viewModelScope.launch(IO) {
            val bottle = bottleRepository.getBottleByIdNotLive(bottleId)
            bottleRepository.updateBottle(bottle.copy(pdfPath = ""))
        }
    }

    fun revertBottleConsumption() {
        val bottleId = _bottleId.value.takeIf { it > 0L } ?: run {
            emitEvent(BottleDetailsEvent.UserFeedback(R.string.base_error))
            return
        }

        viewModelScope.launch(IO) {
            val bottle = bottleRepository.getBottleByIdNotLive(bottleId)
            if (bottle.consumed.toBoolean()) {
                revertBottleConsumptionInternal(bottleId)
            } else {
                removeBottleFromTasting(bottleId)
            }
        }
    }

    private suspend fun revertBottleConsumptionInternal(bottleId: Long) {
        val boundedBottle = bottleRepository.getBoundedBottleByIdNotLive(bottleId)
        val wine = boundedBottle.wine

        wineRepository.transaction {
            wineRepository.updateWine(wine.copy(hidden = false.toInt()))
            bottleRepository.revertBottleConsumption(bottleId)
        }

        emitEvent(BottleDetailsEvent.RevertConsumption(boundedBottle))
    }

    private suspend fun removeBottleFromTasting(bottleId: Long) {
        val bottle = bottleRepository.getBottleByIdNotLive(bottleId)
        val tastingId = bottle.tastingId

        bottle.tastingId = null
        bottleRepository.updateBottle(bottle)

        emitEvent(BottleDetailsEvent.RemoveFromTasting(bottleId, tastingId))
    }

    fun cancelRemoveBottleFromTasting(bottleId: Long, tastingId: Long?) {
        viewModelScope.launch(IO) {
            val bottle = bottleRepository.getBottleByIdNotLive(bottleId)
            bottle.tastingId = tastingId
            bottleRepository.updateBottle(bottle)
        }
    }

    fun cancelRemoveTag(tagId: Long, bottleId: Long?) {
        viewModelScope.launch(IO) {
            bottleId?.let {
                tagRepository.insertTagBottleXRefs(listOf(TagXBottle(tagId, it)))
            }
        }
    }

    private suspend fun maybeDeleteWine(deletedBottleId: Long, wineId: Long) {
        val wine = wineRepository.getWineByIdNotLive(wineId)
        val wineBottles = bottleRepository.getBottlesForWineNotLive(wineId)
        val folder = mutableListOf<Bottle>() to mutableListOf<Bottle>()
        val (consumed, stock) = wineBottles.fold(folder) { pair, bottle ->
            pair.apply {
                when (bottle.consumed.toBoolean()) {
                    true -> first += bottle
                    else -> second += bottle
                }
            }
        }

        val hasOtherConsumedBottle = consumed.size > 1
        val hasStock = stock.isNotEmpty()
        val isSameBottle = consumed.firstOrNull()?.id == deletedBottleId

        if (wine.hidden.toBoolean() && !hasOtherConsumedBottle && !hasStock && isSameBottle) {
            wineRepository.deleteWineById(wineId)
        }
    }
}

