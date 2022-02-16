package com.louis.app.cavity.ui.bottle

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.toBoolean
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class BottleDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val bottleId = MutableLiveData<Long>()

    private val _pdfEvent = MutableLiveData<Event<Uri>>()
    val pdfEvent: LiveData<Event<Uri>>
        get() = _pdfEvent

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    val bottle = bottleId.switchMap { repository.getBottleById(it) }

    val grapes = bottleId.switchMap { repository.getQGrapesAndGrapeForBottle(it) }

    val reviews = bottleId.switchMap { repository.getFReviewAndReviewForBottle(it) }

    fun getWineById(wineId: Long) = repository.getWineById(wineId)

    fun getBottlesForWine(wineId: Long) = repository.getBottlesForWine(wineId)

    fun setBottleId(bottleId: Long) {
        this.bottleId.value = bottleId
    }

    fun getBottleId() = this.bottleId.value

    fun deleteBottle() {
        val bottleId = bottleId.value ?: return
        val wineId = bottle.value?.wineId ?: return

        viewModelScope.launch(IO) {
            maybeDeleteWine(bottleId, wineId)
            repository.deleteBottleById(bottleId)
        }
    }

    fun toggleFavorite() {
        val bottleId = bottleId.value ?: return

        viewModelScope.launch(IO) {
            repository.run {
                val bottle = getBottleByIdNotLive(bottleId)
                if (bottle.isFavorite.toBoolean()) unfav(bottleId) else fav(bottleId)
            }
        }
    }

    fun preparePdf() {
        val bottleId = bottleId.value ?: return

        viewModelScope.launch(IO) {
            val bottle = repository.getBottleByIdNotLive(bottleId)
            val path = bottle.pdfPath

            if (path.isNotBlank()) {
                _pdfEvent.postOnce(Uri.parse(path))
            } else {
                _userFeedback.postOnce(R.string.no_pdf)
            }
        }
    }

    fun revertBottleConsumption() {
        val bottleId = bottleId.value ?: return
        val wineId = bottle.value?.wineId ?: return

        viewModelScope.launch(IO) {
            val wine = repository.getWineByIdNotLive(wineId)
            repository.updateWine(wine.copy(hidden = false.toInt()))

            repository.revertBottleConsumption(bottleId)
        }
    }

    fun removeBottleFromTasting() {
        val bottleId = bottleId.value ?: return

        viewModelScope.launch(IO) {
            val bottle = repository.getBottleByIdNotLive(bottleId)
            bottle.tastingId = null
            repository.updateBottle(bottle)
        }
    }

    private suspend fun maybeDeleteWine(deletedBottleId: Long, wineId: Long) {
        val wine = repository.getWineByIdNotLive(wineId)
        val wineBottles = repository.getBottlesForWineNotLive(wineId)
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
        val hasStock = stock.size > 0
        val isSameBottle = consumed.firstOrNull()?.id == deletedBottleId

        if (wine.hidden.toBoolean() && !hasOtherConsumedBottle && !hasStock && isSameBottle) {
            repository.deleteWineById(wineId)
        }
    }
}
