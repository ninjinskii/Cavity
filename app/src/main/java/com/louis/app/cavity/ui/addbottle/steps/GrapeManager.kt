package com.louis.app.cavity.ui.addbottle.steps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GrapeManager(
    private val repository: WineRepository,
    private val _userFeedback: MutableLiveData<Event<Int>>,
    private val viewModelScope: CoroutineScope
) {

    val grapes = GrapeList()

    private val defaultPercentage: Int
        get() = if (grapes.isEmpty()) 25 else 0

    fun addGrape(grapeName: String) {
        if (grapeName.isEmpty()) {
            _userFeedback.postOnce(R.string.empty_grape_name)
            return
        }

        // bottleId will be replaced in saveBottle if we're in editMode
        val grape = Grape(0, grapeName, defaultPercentage, -1)

        if (!alreadyContainsGrape(grape.name)) {
            grapes.add(grape)
        } else
            _userFeedback.postOnce(R.string.grape_already_exist)
    }

    fun updateGrape(grape: Grape) {
        grapes.updateGrapePercentage(grape)
    }

    fun removeGrape(grape: Grape) {
        // Deleted grape might already be in database, we need to remove it
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGrape(grape)
        }

        grapes.remove(grape)
    }

    fun validateGrapes(): Boolean {
        return if (grapes.any { it.percentage == 0 }) {
            _userFeedback.postOnce(R.string.empty_grape_percent)
            false
        } else {
            true
        }
    }

    fun postValue(value: List<Grape>) {
        grapes.clear()
        grapes.addAll(value)
    }

    fun reset() {
        grapes.clear()
    }

    private fun alreadyContainsGrape(grapeName: String): Boolean {
        val grapeNames = grapes.map { it.name }
        return grapeName in grapeNames
    }

}
