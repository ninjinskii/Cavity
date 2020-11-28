package com.louis.app.cavity.ui.addbottle

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.ui.addbottle.steps.ReviewManager
import com.louis.app.cavity.util.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val _step = MutableLiveData(0)
    val step: LiveData<Int>
        get() = _step

    val reviewManager = ReviewManager(repository, _userFeedback, viewModelScope)

    private var wineId: Long? = null
    private var bottleId: Long? = null
    private var buyDateTimestamp = -1L
    private var pdfPath: String = ""

    private val isEditMode: Boolean
        get() = _updatedBottle.value != null

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
            bottleId ?: 0,
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
            if (!isEditMode)
                repository.insertBottle(partialBottle).also { bottleId = it }
            else
                repository.updateBottle(partialBottle)
        }
    }

    fun saveBottle(otherInfo: String, addToFavorite: Boolean) {
        if (bottleId == null) {
            _userFeedback.postOnce(R.string.base_error)
            return
        }

        viewModelScope.launch(IO) {
            val step1 = repository.getBottleByIdNotLive(bottleId!!)
            val bottle = mergeStep1Bottle(step1, addToFavorite, otherInfo)
            repository.updateBottle(bottle)

            // Review

            wineId = null
            bottleId = null
            reviewManager.reset()
            _updatedBottle.postValue(null)
            _bottleUpdatedEvent.postOnce(R.string.bottle_added)
        }
    }

    private fun triggerEditMode(bottleId: Long) {
        viewModelScope.launch(IO) {
            val editedBottle = repository.getBottleByIdNotLive(bottleId)
            _updatedBottle.postValue(editedBottle)

            // grape

            // reviews
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
