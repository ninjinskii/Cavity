package com.louis.app.cavity.ui.addbottle.viewmodel

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.minusAssign
import com.louis.app.cavity.util.plusAssign
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class GrapeManager(
    private val viewModelScope: CoroutineScope,
    private val repository: WineRepository,
    private val editedBottle: Bottle?,
    private val postFeedback: (Int) -> Unit
) {
    private val qGrapeManager = QuantifiedGrapeManager()

    private val _grapeDialogEvent = MutableLiveData<Event<List<GrapeUiModel>>>()
    val grapeDialogEvent: LiveData<Event<List<GrapeUiModel>>>
        get() = _grapeDialogEvent

    private val _qGrapes = MutableLiveData<MutableList<QGrapeUiModel>>()
    val qGrapes: LiveData<MutableList<QGrapeUiModel>>
        get() = _qGrapes

    init {
        if (editedBottle != null) {
            viewModelScope.launch(IO) {
                val qGrapes = repository.getQGrapesAndGrapeForBottleNotLive(editedBottle.id)
                val uiGrapes = qGrapes.map { QGrapeUiModel.fromQGrape(it) }.toMutableList()
                qGrapeManager.submitQGrapes(qGrapes)
                _qGrapes.postValue(uiGrapes)
            }
        }
    }

    fun addGrapeAndQGrape(grapeName: String) {
        viewModelScope.launch(IO) {
            try {
                val grape = Grape(0, grapeName)
                repository.insertGrape(grape)

                val defaultValue = qGrapeManager.requestAddQGrape()
                _qGrapes += QGrapeUiModel(grapeName, defaultValue)
            } catch (e: IllegalArgumentException) {
                postFeedback(R.string.empty_grape_name)
            } catch (e: SQLiteConstraintException) {
                postFeedback(R.string.grape_already_exists)
            }
        }
    }

    private fun addQuantifiedGrape(grapeName: String) {
        val defaultValue = qGrapeManager.requestAddQGrape()
        _qGrapes += QGrapeUiModel(grapeName, defaultValue)
    }

    // Return true if the value requested is accepted
    fun updateQuantifiedGrape(qGrape: QGrapeUiModel, newValue: Int): Int {
        val checkedValue = qGrapeManager.requestUpdateQGrape(qGrape.percentage, newValue)
        //val newQGrape = qGrape.copy(percentage = checkedValue)

        // Might just change percentage value of the qgrape ??
        _qGrapes.value?.find { it.name == qGrape.name }?.percentage = checkedValue

        // trigger observers ?

        return checkedValue
    }

    // Delete from recycler view
    fun removeQuantifiedGrape(qGrape: QGrapeUiModel) {
        qGrapeManager.requestRemoveQGrape(qGrape)
        _qGrapes -= qGrape
    }

    // Delete from dialog
    private fun removeQuantifiedGrape(grapeName: String) {
        val qGrape = _qGrapes.value?.find { it.name == grapeName } ?: return
        removeQuantifiedGrape(qGrape)
    }

    fun submitCheckedGrapes(checkableGrapes: List<GrapeUiModel>) {
        for (checkableGrape in checkableGrapes) {
            val grapeName = checkableGrape.name
            val oldOne =
                _grapeDialogEvent.value?.peekContent()?.find { it.name == grapeName }

            when {
                checkableGrape.isChecked && oldOne?.isChecked != true ->
                    addQuantifiedGrape(grapeName)
                !checkableGrape.isChecked && oldOne?.isChecked != false ->
                    removeQuantifiedGrape(grapeName)
            }

            // Not updating the value of the _grapeDialogEvent LiveData. This will be done
            // when requestGrapeDialog() is called only
        }
    }

    fun requestGrapeDialog() {
        viewModelScope.launch(IO) {
            val grapes = repository.getAllGrapesNotLive()
            val qGrapes = _qGrapes.value?.map { it.name } ?: emptyList<QGrapeUiModel>()
            val currentCheckedGrapes =
                grapes.map { GrapeUiModel(it.name, isChecked = it.name in qGrapes) }

            _grapeDialogEvent.postOnce(currentCheckedGrapes)
        }
    }
}
