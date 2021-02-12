package com.louis.app.cavity.ui.addbottle.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.relation.crossref.QuantifiedBottleGrapeXRef
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

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private var bottleId = 0L

    fun start(editedBottleId: Long) {
        bottleId = editedBottleId
    }

    fun getQGrapesAndGrapeForBottle(bottleId: Long) =
        repository.getQGrapesAndGrapeForBottle(bottleId)

    fun insertGrape(grapeName: String) {
        viewModelScope.launch(IO) {
            try {
                // TODO: transaction
                val grapeId = repository.insertGrape(Grape(0, grapeName))
                insertQuantifiedGrape(grapeId)
            } catch (e: IllegalArgumentException) {
                _userFeedback.postOnce(R.string.empty_grape_name)
            } catch (e: SQLiteConstraintException) {
                _userFeedback.postOnce(R.string.grape_already_exists)
            }
        }
    }

    private fun insertQuantifiedGrape(grapeId: Long) {
        val defaultValue = qGrapeManager.requestAddQGrape()
        val qGrape = QuantifiedBottleGrapeXRef(bottleId, grapeId, defaultValue)

        viewModelScope.launch(IO) {
            repository.insertQuantifiedGrape(qGrape)
        }
    }

    // Return true if the value requested is accepted
    fun updateQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef, newValue: Int): Int {
        val checkedValue = qGrapeManager.requestUpdateQGrape(qGrape.percentage, newValue)
        val newQGrape = qGrape.copy(percentage = checkedValue) // need copy to avoid false positive

        viewModelScope.launch(IO) {
            repository.updateQuantifiedGrape(newQGrape)
        }

        return checkedValue
    }

    // Delete from recycler view
    fun removeQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef) {
        qGrapeManager.requestRemoveQGrape(qGrape)

        viewModelScope.launch(IO) {
            repository.deleteQuantifiedGrape(qGrape)
        }
    }

    // Delete from dialog
    private fun removeQuantifiedGrape(grapeId: Long) {
        viewModelScope.launch(IO) {
            val qGrape = repository.getQGrape(bottleId, grapeId)
            qGrapeManager.requestRemoveQGrape(qGrape)
            repository.deleteQuantifiedGrape(qGrape)
        }
    }

    fun submitCheckedGrapes(newCheckedGrapes: List<CheckableGrape>) {
        for (checkableGrape in newCheckedGrapes) {
            val grapeId = checkableGrape.grape.id
            val oldOne =
                _grapeDialogEvent.value?.peekContent()?.find { it.grape.id == grapeId }

            when {
                checkableGrape.isChecked && oldOne?.isChecked != true ->
                    insertQuantifiedGrape(grapeId)
                !checkableGrape.isChecked && oldOne?.isChecked != false ->
                    removeQuantifiedGrape(grapeId)
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
                grapes.map { CheckableGrape(it, isChecked = it.id in qGrapes) }

            _grapeDialogEvent.postOnce(currentCheckedGrapes)
        }
    }

    data class CheckableGrape(val grape: Grape, var isChecked: Boolean)
}
