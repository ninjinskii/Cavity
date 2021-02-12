package com.louis.app.cavity.ui.bottle

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.toBoolean
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class BottleDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _pdfEvent = MutableLiveData<Event<Uri>>()
    val pdfEvent: LiveData<Event<Uri>>
        get() = _pdfEvent

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    fun getWineById(wineId: Long) = repository.getWineById(wineId)

    fun getBottleById(bottleId: Long) = repository.getBottleById(bottleId)

    fun getQGrapesForBottle(bottleId: Long) = repository.getQGrapesAndGrapeForBottle(bottleId)

    fun getFReviewForBottle(bottleId: Long) = repository.getFReviewAndReviewForBottle(bottleId)

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
