package com.louis.app.cavity.ui.addbottle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DateViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _updatedBottle = MutableLiveData<Bottle>()
    val updatedBottle: LiveData<Bottle>
        get() = _updatedBottle

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private var wineId: Long? = null
    private var buyDateTimestamp = -1L

    var bottleId = 0L
        private set

    fun start(bottleWineId: Long, editedBottleId: Long) {
        wineId = bottleWineId

        if (editedBottleId != 0L) {
            bottleId = editedBottleId
            triggerEditMode(editedBottleId)
        } else {
            _updatedBottle.postValue(null)
        }
    }

    fun setTimestamp(timestamp: Long) {
        buyDateTimestamp = timestamp
    }

    fun submitDates(
        vintage: Int,
        apogee: Int,
        count: Int,
        price: Float,
        currency: String,
        location: String
    ) {
        val partialBottle = Bottle(
            bottleId,
            wineId ?: return,
            vintage,
            apogee,
            0,
            count,
            price,
            currency,
            "",
            location,
            buyDateTimestamp,
            "",
            ""
        )

        viewModelScope.launch(Dispatchers.IO) {
            if (bottleId == 0L)
                repository.insertBottle(partialBottle).also { bottleId = it }
            else
                repository.updateBottle(partialBottle)
        }
    }

    private fun triggerEditMode(bottleId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val editedBottle = repository.getBottleByIdNotLive(bottleId)
            _updatedBottle.postValue(editedBottle)
        }
    }
}
