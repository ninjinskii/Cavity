package com.louis.app.cavity.ui.addbottle.steps

import com.louis.app.cavity.model.relation.QuantifiedBottleGrapeXRef
import com.louis.app.cavity.util.L

class QuantifiedGrapeManager {

    companion object {
        const val MAX_PERCENTAGE = 100
    }

    private var total = 0

    private val defaultPercentage: Int
        get() = if (total == 0) 25 else 0

    private val fillValue: Int
        get() = MAX_PERCENTAGE - total


    fun requestAddQGrape(bottleId: Long, grapeId: Long): QuantifiedBottleGrapeXRef {
//        L.v("add")
//        L.v("total: $total, defaultPercentage: $defaultPercentage")
        total += defaultPercentage
        return QuantifiedBottleGrapeXRef(bottleId, grapeId, defaultPercentage)
    }

    fun requestUpdateQGrape(oldValue: Int, newValue: Int): Int {
        val diff = newValue - oldValue

        return if (diff > 0) {
            if (total + diff > MAX_PERCENTAGE) fillValue else newValue
        } else {
            newValue
        }.also { total += diff }
    }

    fun requestRemoveQGrape(qGrape: QuantifiedBottleGrapeXRef) {
        total -= qGrape.percentage
    }

//    fun addGrape(grapeName: String) {
//        if (grapeName.isEmpty()) {
//            _userFeedback.postOnce(R.string.empty_grape_name)
//            return
//        }
//
//        // bottleId will be replaced in saveBottle if we're in editMode
//        val grape = Grape(0, grapeName)
//
//        if (!alreadyContainsGrape(grape.name)) {
//            grapes.add(grape)
//        } else
//            _userFeedback.postOnce(R.string.grape_already_exist)
//    }
//
//    fun addQuantifiedGrape(bottleId: Long, grapeId: Long, percentage: Int) {
//
//    }
//
//    fun updateGrape(grape: Grape) {
//        grapes.updateGrapePercentage(grape)
//    }
//
//    fun removeGrape(grape: Grape) {
//        // Deleted grape might already be in database, we need to remove it
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.deleteGrape(grape)
//        }
//
//        grapes.remove(grape)
//    }
//
//    fun validateGrapes(): Boolean {
//        return if (grapes.any { it.percentage == 0 }) {
//            _userFeedback.postOnce(R.string.empty_grape_percent)
//            false
//        } else {
//            true
//        }
//    }
//
//    fun postValue(value: List<Grape>) {
//        grapes.clear()
//        grapes.addAll(value)
//    }
//
//    fun reset() {
//        grapes.clear()
//    }
//
//    private fun alreadyContainsGrape(grapeName: String): Boolean {
//        val grapeNames = grapes.map { it.name }
//        return grapeName in grapeNames
//    }

}
