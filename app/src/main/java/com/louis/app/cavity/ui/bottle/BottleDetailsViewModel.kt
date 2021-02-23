package com.louis.app.cavity.ui.bottle

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import androidx.paging.*
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.ui.history.HistoryUiModel
import com.louis.app.cavity.util.DateFormatter
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.toBoolean
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class BottleDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    // TODO: consider removing public part if not necessary in future
    private val _bottleId = MutableLiveData<Long>()
    val bottleId: LiveData<Long>
        get() = _bottleId

    private val _pdfEvent = MutableLiveData<Event<Uri>>()
    val pdfEvent: LiveData<Event<Uri>>
        get() = _pdfEvent

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    fun setBottle(bottleId: Long) {
        _bottleId.postValue(bottleId)
    }

    fun getWineById(wineId: Long) = repository.getWineById(wineId)

    fun getBottleById() = bottleId.switchMap { repository.getBottleById(it) }

    fun getQGrapesForBottle() = bottleId.switchMap { repository.getQGrapesAndGrapeForBottle(it) }

    fun getFReviewForBottle() = bottleId.switchMap { repository.getFReviewAndReviewForBottle(it) }

    fun addBottles(bottleId: Long, count: Int) {
        viewModelScope.launch(IO) {
            repository.insertBottles(bottleId, count)
        }
    }

    fun removeBottles(bottleId: Long, count: Int) {
        viewModelScope.launch(IO) {
            repository.deleteBottles(bottleId, count)
        }
    }

    fun toggleFavorite(bottleId: Long) {
        viewModelScope.launch(IO) {
            repository.run {
                val bottle = getBottleByIdNotLive(bottleId)
                if (bottle.isFavorite.toBoolean()) unfav(bottleId) else fav(bottleId)
            }
        }
    }

    fun preparePdf(bottleId: Long) {
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

    fun revertBottleConsumption(bottleId: Long) {
        viewModelScope.launch(IO) {
            repository.revertBottleConsumption(bottleId)
        }
    }
}
