package com.louis.app.cavity.ui.addbottle.steps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

    var currentCheckedGrapes = mutableListOf<CheckableGrape>()
        private set

    init {
        viewModelScope.launch(IO) {
            val grapes = repository.getAllGrapesNotLive()
            currentCheckedGrapes = grapes.map { CheckableGrape(it, isChecked = false) } as MutableList
        }
    }

    fun getAllGrapes() = repository.getAllGrapes()

    fun getQGrapesAndGrapeForBottle(bottleId: Long) =
        repository.getQGrapesAndGrapeForBottle(bottleId)

    fun insertGrape(grape: Grape) = viewModelScope.launch(IO) {
        val id = repository.insertGrape(grape)
        addCheckedGrapes(grape)
        insertQuantifiedGrape(1, id)
    }

    fun insertQuantifiedGrape(bottleId: Long, grapeId: Long) {
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
        currentCheckedGrapes.find { it.isChecked && it.grape.grapeId == qGrape.grapeId }?.isChecked =
            false

        viewModelScope.launch(IO) {
            repository.deleteQuantifiedGrape(qGrape)
        }
    }

    // Delete from dialog
    fun removeQuantifiedGrape(bottleId: Long, grapeId: Long) {
        viewModelScope.launch(IO) {
            val qGrape = repository.getQGrape(bottleId, grapeId)
            qGrapeManager.requestRemoveQGrape(qGrape)
            //checkedGrapes.find { it.isChecked && it.grape.grapeId == grapeId }?.isChecked = false
            repository.deleteQuantifiedGrape(qGrape)
        }
    }

    fun getGrapeToStringArray() = currentCheckedGrapes.map { it.grape.name }.toTypedArray()

    fun getGrapeToBooleanArray() = currentCheckedGrapes.map { it.isChecked }.toBooleanArray()

    fun submitCheckedGrapes(newCheckedGrapes: List<CheckableGrape>) {
        for (checkableGrape in newCheckedGrapes) {
            val grapeId = checkableGrape.grape.grapeId
            val oldOne = currentCheckedGrapes.find { it.grape.grapeId == grapeId }

            if (checkableGrape.isChecked && oldOne?.isChecked != true) {
                insertQuantifiedGrape(1, grapeId)
            }

            if (!checkableGrape.isChecked && oldOne?.isChecked != false)
                removeQuantifiedGrape(1, grapeId)
        }

        currentCheckedGrapes = newCheckedGrapes as MutableList
    }

    private fun addCheckedGrapes(grape: Grape) {
        currentCheckedGrapes.add(CheckableGrape(grape, isChecked = true))
    }

    data class CheckableGrape(val grape: Grape, var isChecked: Boolean)
}
