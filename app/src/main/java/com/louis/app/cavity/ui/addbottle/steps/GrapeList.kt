package com.louis.app.cavity.ui.addbottle.steps

import com.louis.app.cavity.model.Grape

class GrapeList(
    private val grapes: MutableList<Grape> = ArrayList()
) : MutableList<Grape> by grapes {

    companion object {
        const val MAX_PERCENTAGE = 100
    }

    private val total: Int
        get() = grapes.map { it.percentage }.sum()

    private val fillValue: Int
        get() = MAX_PERCENTAGE - total

    override fun add(element: Grape): Boolean {
        return if (total + element.percentage <= MAX_PERCENTAGE) {
            grapes.add(element)
            true
        } else {
            element.percentage = fillValue
            grapes.add(element)
        }
    }
}
