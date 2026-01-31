package com.louis.app.cavity.ui.bottle

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.repository.WineRepository
import com.louis.app.cavity.db.dao.BoundedBottle
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.GrapeRepository
import com.louis.app.cavity.domain.repository.HistoryRepository
import com.louis.app.cavity.domain.repository.ReviewRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.toBoolean
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.louis.app.cavity.domain.history.isConsumption
import com.louis.app.cavity.domain.repository.TagRepository
import com.louis.app.cavity.model.Tag
import com.louis.app.cavity.model.TagXBottle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class BottleDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val wineRepository = WineRepository.getInstance(app)
    private val bottleRepository = BottleRepository.getInstance(app)
    private val grapeRepository = GrapeRepository.getInstance(app)
    private val reviewRepository = ReviewRepository.getInstance(app)
    private val historyRepository = HistoryRepository.getInstance(app)
    private val tagRepository = TagRepository.getInstance(app)

    private val bottleId = MutableLiveData<Long>()

    private val _pdfEvent = MutableLiveData<Event<Uri>>()
    val pdfEvent: LiveData<Event<Uri>>
        get() = _pdfEvent

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private val _revertConsumptionEvent = MutableLiveData<Event<BoundedBottle>>()
    val revertConsumptionEvent: LiveData<Event<BoundedBottle>>
        get() = _revertConsumptionEvent

    private val _removeFromTastingEvent = MutableLiveData<Event<Pair<Long, Long?>>>()
    val removeFromTastingEvent: LiveData<Event<Pair<Long, Long?>>>
        get() = _removeFromTastingEvent

    private val _removeTagEvent = MutableLiveData<Event<Pair<Long, Long?>>>()
    val removeTagEvent: LiveData<Event<Pair<Long, Long?>>>
        get() = _removeTagEvent

    val bottle = bottleId.switchMap { bottleRepository.getBottleById(it) }

    val grapes = bottleId.switchMap { grapeRepository.getQGrapesAndGrapeForBottle(it) }

    val reviews = bottleId.switchMap { reviewRepository.getFReviewAndReviewForBottle(it) }

    val tags = bottleId.switchMap { tagRepository.getTagsForBottle(it) }

    val replenishmentEntry =
        bottleId.switchMap { historyRepository.getReplenishmentForBottleNotPaged(it) }

    fun getWineById(wineId: Long) = wineRepository.getWineById(wineId)

    fun getConsumedBottlesWithHistoryForWine(wineId: Long) =
        bottleRepository.getBottlesForWine(wineId).map { bottlesWithHistoryEntries ->
            bottlesWithHistoryEntries.filter { (bottle, historyEntries) ->
                bottle.consumed.toBoolean() &&
                        historyEntries.any { it.type.isConsumption() && it.comment.isNotBlank() }
            }
        }
            .flowOn(Dispatchers.Default)
            .asLiveData()

    fun getBottlesForWine(wineId: Long) = bottleRepository.getBottlesForWine(wineId).asLiveData()

    fun setBottleId(bottleId: Long) {
        this.bottleId.value = bottleId
    }

    fun getBottleId() = this.bottleId.value

    fun deleteBottle() {
        val bottleId = bottleId.value ?: return
        val wineId = bottle.value?.wineId ?: return

        viewModelScope.launch(IO) {
            maybeDeleteWine(bottleId, wineId)
            bottleRepository.deleteBottleById(bottleId)
        }
    }

    fun toggleFavorite() {
        val bottleId = bottleId.value ?: return

        viewModelScope.launch(IO) {
            bottleRepository.run {
                val bottle = getBottleByIdNotLive(bottleId)
                if (bottle.isFavorite.toBoolean()) unfav(bottleId) else fav(bottleId)
            }
        }
    }

    fun removeTag(tag: Tag) {
        if (bottleId.value == null) {
            _userFeedback.postOnce(R.string.base_error)
            return
        }

        viewModelScope.launch(IO) {
            tagRepository.deleteTagBottleXRef(TagXBottle(tag.id, bottleId.value ?: -1))
            _removeTagEvent.postOnce(tag.id to (bottleId.value ?: -1))
        }
    }

    fun preparePdf() {
        val bottleId = bottleId.value ?: return

        viewModelScope.launch(IO) {
            val bottle = bottleRepository.getBottleByIdNotLive(bottleId)
            val path = bottle.pdfPath

            if (path.isNotBlank()) {
                _pdfEvent.postOnce(path.toUri())
            } else {
                _userFeedback.postOnce(R.string.no_pdf)
            }
        }
    }

    fun clearPdfPath() {
        bottle.value?.let {
            viewModelScope.launch(IO) {
                bottleRepository.updateBottle(it.copy(pdfPath = ""))
            }
        }
    }

    fun revertBottleConsumption() {
        val bottleId = bottleId.value ?: return

        viewModelScope.launch(IO) {
            val boundedBottle = bottleRepository.getBoundedBottleByIdNotLive(bottleId)
            val wine = boundedBottle.wine

            wineRepository.transaction {
                wineRepository.updateWine(wine.copy(hidden = false.toInt()))
                bottleRepository.revertBottleConsumption(bottleId)
            }

            _revertConsumptionEvent.postOnce(boundedBottle)
        }
    }

    fun removeBottleFromTasting() {
        val bottleId = bottleId.value ?: return

        viewModelScope.launch(IO) {
            val bottle = bottleRepository.getBottleByIdNotLive(bottleId)
            val tastingId = bottle.tastingId

            bottle.tastingId = null
            bottleRepository.updateBottle(bottle)

            _removeFromTastingEvent.postOnce(bottleId to tastingId)
        }
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
