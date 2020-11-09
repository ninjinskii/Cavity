package com.louis.app.cavity.ui.addbottle.steps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.minusAssign
import com.louis.app.cavity.util.plusAssign
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GrapeManager(
    private val repository: WineRepository,
    private val _userFeedback: MutableLiveData<Event<Int>>,
    private val viewModelScope: CoroutineScope
) {
    private val _grapes = MutableLiveData<MutableList<Grape>>()
    val grapes: LiveData<MutableList<Grape>>
        get() = _grapes

    private val defaultPercentage: Int
        get() = if (_grapes.value?.isEmpty() == true) 25 else 0

    fun addGrape(grapeName: String) {
        if (grapeName.isEmpty()) {
            _userFeedback.postOnce(R.string.empty_grape_name)
            return
        }

        // bottleId will be replaced in saveBottle if we're in editeMode
        val grape = Grape(0, grapeName, defaultPercentage, -1)

        if (!alreadyContainsGrape(grape.name))
            _grapes += grape
        else
            _userFeedback.postOnce(R.string.grape_already_exist)
    }

    fun updateGrape(grape: Grape) {
        grapes.value?.first { it.name == grape.name }?.percentage = grape.percentage
    }

    fun removeGrape(grape: Grape) {
        // Deleted grape might already be in database, we need to remove it
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGrape(grape)
        }

        _grapes -= grape
    }

    fun validateGrapes(): Boolean {
        return if (_grapes.value?.any { it.percentage == 0 } == true) {
            _userFeedback.postOnce(R.string.empty_grape_percent)
            false
        } else {
            true
        }
    }

    fun postValue(value: MutableList<Grape>) {
        _grapes.postValue(value)
    }

    fun reset() {
        _grapes.postValue(mutableListOf())
    }

    private fun alreadyContainsGrape(grapeName: String): Boolean {
        val grapeNames = _grapes.value?.map { it.name } ?: return false
        return grapeName in grapeNames
    }

}
