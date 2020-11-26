package com.louis.app.cavity.ui.addbottle

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.ExpertAdvice
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.ui.addbottle.steps.ExpertAdviceManager
import com.louis.app.cavity.ui.addbottle.steps.GrapeManager
import com.louis.app.cavity.util.*
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

    private val _step = MutableLiveData(0)
    val step: LiveData<Int>
        get() = _step

    val grapeManager = GrapeManager(repository, _userFeedback, viewModelScope)
    val expertAdviceManager = ExpertAdviceManager(repository, _userFeedback, viewModelScope)

    private var wineId: Long? = null
    private var partialBottle: PartialBottle? = null
    private var buyDateTimestamp = -1L
    private var pdfPath: String = ""

    private val isEditMode: Boolean
        get() = _updatedBottle.value != null

    val hasPdf: Boolean
        get() = pdfPath.isNotBlank()

    val grapes: LiveData<MutableList<Grape>>
        get() = grapeManager.grapes.liveData

    fun start(bottleWineId: Long, editedBottleId: Long) {
        L.v("$bottleWineId, $editedBottleId")
        wineId = bottleWineId

        if (editedBottleId != 0L)
            triggerEditMode(editedBottleId)
        else
            _updatedBottle.postValue(null)
    }

    fun setTimestamp(timestamp: Long) {
        buyDateTimestamp = timestamp
    }

    fun setPdfPath(path: String) {
        pdfPath = path
    }

    fun setPartialBottle(
        vintage: Int,
        apogee: Int,
        count: Int,
        price: Float,
        currency: String,
        location: String
    ) {
        val editBottleId = _updatedBottle.value?.bottleId

        partialBottle =
            PartialBottle(
                editBottleId ?: 0,
                vintage,
                apogee,
                count,
                price,
                currency,
                location,
                buyDateTimestamp
            )
    }

    fun saveBottle(otherInfo: String, addToFavorite: Boolean) {
        if (wineId == null) _userFeedback.postOnce(R.string.base_error)

        partialBottle?.let {
            val bottle = Bottle(
                it.bottleId,
                wineId!!,
                it.vintage,
                it.apogee,
                addToFavorite.toInt(),
                it.count,
                it.price,
                it.currency,
                otherInfo,
                it.location,
                it.date,
                "",
                pdfPath
            )

            viewModelScope.launch(IO) {
                L.v("$isEditMode")
                val insertedBottleId =
                    if (isEditMode) {
                        repository.updateBottle(bottle)
                        bottle.bottleId
                    } else {
                        repository.insertBottle(bottle)
                    }

                expertAdviceManager.expertAdvices.value?.forEach { advice ->
                    advice.bottleId = insertedBottleId
                    repository.insertAdvice(advice)
                }

                grapeManager.grapes.content.forEach { grape ->
                    grape.bottleId = insertedBottleId
                    repository.insertGrape(grape)
                }

                wineId = null
                partialBottle = null
                grapeManager.reset()
                expertAdviceManager.reset()
                _updatedBottle.postValue(null)
                _bottleUpdatedEvent.postOnce(R.string.bottle_added)
            }
        }
    }

    private fun triggerEditMode(bottleId: Long) {
        viewModelScope.launch(IO) {
            val editedBottle = repository.getBottleByIdNotLive(bottleId)
//            with(editedBottle) {
//                setPartialBottle(
//                    vintage,
//                    apogee,
//                    count.toString(),
//                    price.toString(),
//                    currency,
//                    buyLocation,
//                    buyDate
//                )
//            }

            L.v("editedWineId: $wineId")
            L.v("editedBottle: $editedBottle")
            _updatedBottle.postValue(editedBottle)

            val grapesForBottle = repository.getGrapesForBottleNotLive(bottleId)
            grapeManager.postValue(grapesForBottle)

            val expertAdviceForBottle = repository.getExpertAdvicesForBottleNotLive(bottleId)
            expertAdviceManager.postValue(expertAdviceForBottle as MutableList<ExpertAdvice>)
        }
    }

    data class PartialBottle(
        val bottleId: Long,
        val vintage: Int,
        val apogee: Int,
        val count: Int,
        val price: Float,
        val currency: String,
        val location: String,
        val date: Long
    )
}
