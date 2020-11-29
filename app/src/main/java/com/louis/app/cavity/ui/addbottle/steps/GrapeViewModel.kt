package com.louis.app.cavity.ui.addbottle.steps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.relation.QuantifiedBottleGrapeXRef
import com.louis.app.cavity.util.L
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class GrapeViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)
    private val qGrapeManager = QuantifiedGrapeManager()

    // A list to preserve checked state for dialog, should not be used for any other purpose
    var checkedGrapes = mutableListOf<CheckedGrape>()
        private set

    init {
        viewModelScope.launch(IO) {
            val grapes = repository.getAllGrapesNotLive()
            checkedGrapes = grapes.map { CheckedGrape(it, isChecked = false) }.toMutableList()
        }
    }

    fun getAllGrapes() = repository.getAllGrapes()

    fun getQGrapesForBottle(bottleId: Long) = repository.getQGrapesForBottle(bottleId)

    fun insertGrape(grape: Grape) = viewModelScope.launch(IO) {
        val id = repository.insertGrape(grape)
        addCheckedGrapes(grape)
        insertQuantifiedGrape(1, id)
    }

    fun insertQuantifiedGrape(bottleId: Long, grapeId: Long) {
        val checkedQGrape = qGrapeManager.requestAddQGrape(bottleId, grapeId)
        viewModelScope.launch(IO) { repository.insertQuantifiedGrape(checkedQGrape) }
    }

    fun updateQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef, newValue: Int) {
        L.v("update $newValue")
        val checkedQGrape = qGrapeManager.requestUpdateQGrape(qGrape, newValue)
        L.v("${checkedQGrape.percentage}")
        viewModelScope.launch(IO) { repository.updateQuantifiedGrape(checkedQGrape) }
    }

    // Delete from recycler view todo: consider remove ability to do this via RV ?
    fun removeQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef) {
        qGrapeManager.requestRemoveQGrape(qGrape)
        checkedGrapes.find { it.isChecked && it.grape.grapeId == qGrape.grapeId }?.isChecked = false
    }

    // Delete from dialog
    fun removeQuantifiedGrape(bottleId: Long, grapeId: Long) {
        viewModelScope.launch(IO) {
            val qGrape = repository.getQGrape(bottleId, grapeId)
            qGrapeManager.requestRemoveQGrape(qGrape)
            checkedGrapes.find { it.isChecked && it.grape.grapeId == grapeId }?.isChecked = false
        }
    }

    fun getGrapeIdForPosition(pos: Int) = checkedGrapes[pos].grape.grapeId

    fun getGrapeToStringArray() = checkedGrapes.map { it.grape.name }.toTypedArray()

    fun getGrapeToBooleanArray() = checkedGrapes.map { it.isChecked }.toBooleanArray()

    private fun addCheckedGrapes(grape: Grape) {
        checkedGrapes.add(CheckedGrape(grape, isChecked = false))
    }

    data class CheckedGrape(val grape: Grape, var isChecked: Boolean)
}
