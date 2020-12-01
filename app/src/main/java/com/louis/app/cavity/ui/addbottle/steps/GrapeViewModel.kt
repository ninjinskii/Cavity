package com.louis.app.cavity.ui.addbottle.steps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.relation.QuantifiedBottleGrapeXRef
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class GrapeViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = WineRepository.getInstance(app)
    private val qGrapeManager = QuantifiedGrapeManager()

    private val _grapeDialogEvent = MutableLiveData<Event<List<CheckableGrape>>>()
    val grapeDialogEvent: LiveData<Event<List<CheckableGrape>>>
        get() = _grapeDialogEvent

    private var bottleId = 0L

    fun start(bottleId: Long) {
        this.bottleId = bottleId
    }

    fun getQGrapesAndGrapeForBottle(bottleId: Long) =
        repository.getQGrapesAndGrapeForBottle(bottleId)

    fun insertGrape(grape: Grape) {
        viewModelScope.launch(IO) {
            val id = repository.insertGrape(grape)
            insertQuantifiedGrape(bottleId, id)
        }
    }

    private fun insertQuantifiedGrape(bottleId: Long, grapeId: Long) {
        val defaultValue = qGrapeManager.requestAddQGrape()
        val qGrape = QuantifiedBottleGrapeXRef(bottleId, grapeId, defaultValue)

        viewModelScope.launch(IO) {
            repository.insertQuantifiedGrape(qGrape)
        }
    }

    fun updateQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef, newValue: Int) {
        val checkedValue = qGrapeManager.requestUpdateQGrape(qGrape.percentage, newValue)
        val newQGrape = qGrape.copy(percentage = checkedValue)

        viewModelScope.launch(IO) {
            repository.updateQuantifiedGrape(newQGrape)
        }
    }

    // Delete from recycler view (might need to submit a new checkedGList from fragment)
    fun removeQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef) {
        qGrapeManager.requestRemoveQGrape(qGrape)

        viewModelScope.launch(IO) {
            repository.deleteQuantifiedGrape(qGrape)
        }
    }

    // Delete from dialog
    // TODO: use caller grape instead of fetching it again in db
    private fun removeQuantifiedGrape(bottleId: Long, grapeId: Long) {
        viewModelScope.launch(IO) {
            val qGrape = repository.getQGrape(bottleId, grapeId)
            qGrapeManager.requestRemoveQGrape(qGrape)
            repository.deleteQuantifiedGrape(qGrape)
        }
    }

    fun submitCheckedGrapes(newCheckedGrapes: List<CheckableGrape>) {
        for (checkableGrape in newCheckedGrapes) {
            val grapeId = checkableGrape.grape.grapeId
            val oldOne =
                _grapeDialogEvent.value?.peekContent()?.find { it.grape.grapeId == grapeId }

            when {
                checkableGrape.isChecked && oldOne?.isChecked != true ->
                    insertQuantifiedGrape(bottleId, grapeId)
                !checkableGrape.isChecked && oldOne?.isChecked != false ->
                    removeQuantifiedGrape(bottleId, grapeId)
            }

            // Not updating the value of the _grapeDialogEvent LiveData. This will be done
            // when requestGrapeDialog() is called only
        }
    }

    fun requestGrapeDialog() {
        viewModelScope.launch(IO) {
            val grapes = repository.getAllGrapesNotLive()
            val qGrapes = repository.getQGrapesForBottleNotLive(bottleId).map { it.grapeId }
            val currentCheckedGrapes =
                grapes.map { CheckableGrape(it, isChecked = it.grapeId in qGrapes) }

            _grapeDialogEvent.postOnce(currentCheckedGrapes)
        }
    }

    data class CheckableGrape(val grape: Grape, var isChecked: Boolean)
}
