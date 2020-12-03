package com.louis.app.cavity.ui.addbottle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AddBottleViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _bottleUpdatedEvent = MutableLiveData<Event<Int>>()
    val bottleUpdatedEvent: LiveData<Event<Int>>
        get() = _bottleUpdatedEvent

    private val _updatedBottle = MutableLiveData<Bottle>()
    val updatedBottle: LiveData<Bottle>
        get() = _updatedBottle

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private var wineId: Long? = null
    private var buyDateTimestamp = -1L
    private var pdfPath: String = ""
    private var isBottleFullyRegistered = false

    var bottleId: Long = 0
        private set

    val hasPdf: Boolean
        get() = pdfPath.isNotBlank()

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

    fun setPdfPath(path: String) {
        pdfPath = path
    }

    fun saveStep1(
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

        viewModelScope.launch(IO) {
            if (bottleId == 0L)
                repository.insertBottle(partialBottle).also { bottleId = it }
            else
                repository.updateBottle(partialBottle)
        }
    }

    fun saveBottle(otherInfo: String, addToFavorite: Boolean) {
        if (bottleId == 0L) {
            _userFeedback.postOnce(R.string.base_error)
            return
        }

        viewModelScope.launch(IO) {
            val step1 = repository.getBottleByIdNotLive(bottleId)
            val bottle = mergeStep1Bottle(step1, addToFavorite, otherInfo)
            repository.updateBottle(bottle)

            isBottleFullyRegistered =
                true // remove bottle if it is not the case when leaving addBottleFrag
            wineId = null
            bottleId = 0
            _updatedBottle.postValue(null)
            _bottleUpdatedEvent.postOnce(R.string.bottle_added)
        }
    }

    // Triggered when user quits without having the entire form filled
    // Need to do this when straight killing app too
    fun onCancel() {
        if (!isBottleFullyRegistered) {
            viewModelScope.launch(IO) {
                repository.deleteBottleAndChildsById(bottleId)
            }
        }
    }

    private fun triggerEditMode(bottleId: Long) {
        viewModelScope.launch(IO) {
            val editedBottle = repository.getBottleByIdNotLive(bottleId)
            _updatedBottle.postValue(editedBottle)
        }
    }

    // Hiding boring stuff
    private fun mergeStep1Bottle(step1: Bottle, addToFavorite: Boolean, otherInfo: String): Bottle {
        return Bottle(
            step1.bottleId,
            step1.wineId,
            step1.vintage,
            step1.apogee,
            addToFavorite.toInt(),
            step1.count,
            step1.price,
            step1.currency,
            otherInfo,
            step1.buyLocation,
            step1.buyDate,
            tasteComment = "",
            pdfPath
        )
    }
}
