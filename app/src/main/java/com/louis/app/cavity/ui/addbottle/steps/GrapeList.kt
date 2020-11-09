package com.louis.app.cavity.ui.addbottle.steps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.model.Grape

class GrapeList(
    private val grapes: MutableList<Grape> = ArrayList()
) : MutableList<Grape> by grapes {

    companion object {
        const val MAX_PERCENTAGE = 100
    }

    private val _liveData = MutableLiveData(grapes)
    val liveData: LiveData<MutableList<Grape>>
        get() = _liveData

    val content: List<Grape>
        get() = grapes

    private val total: Int
        get() = grapes.map { it.percentage }.sum()

    private val fillValue: Int
        get() = MAX_PERCENTAGE - total

    override fun add(element: Grape): Boolean {
        if (total + element.percentage > MAX_PERCENTAGE) {
            element.percentage = fillValue
        }

        grapes += element
        _liveData.postValue(grapes)

        return true
    }

    override fun remove(element: Grape): Boolean {
        grapes -= element
        _liveData.postValue(grapes)

        return true
    }

    fun updateGrapePercentage(update: Grape) {
        val target = grapes.first { it.name == update.name }

        if (update.percentage > target.percentage) {
            if (update.percentage - target.percentage + total < MAX_PERCENTAGE) {
                target.percentage = update.percentage
            } else {
                target.percentage = fillValue + target.percentage
            }
        }
    }
}
