package com.louis.app.cavity.ui.home

import com.louis.app.cavity.model.relation.WineWithBottles
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.toBoolean

class WineWithBottlesModel(wineWithBottles: WineWithBottles) {
    private val wine = wineWithBottles.wine
    private val bottles = wineWithBottles.bottles

    val vintages = bottles.map { it.vintage }
    val wineId
        get() = wine.idWine
    val wineName
        get() = wine.name
    val wineNaming
        get() = wine.naming
    val wineImageUri
        get() = wine.imgPath
    val county
        get() = wine.idCounty
    val color
        get() = getColorString(wine.color)
    val isBio
        get() = wine.isBio.toBoolean()

    init {
        L.v(vintages.toString(), "BINDING LIST VINTAGES")
    }

    companion object {
        fun getColorString(color: Int) = when (color) {
            0 -> "#fff176"
            1 -> "#a60000"
            2 -> "#ffa727"
            3 -> "#f48fb1"
            else -> "#000000"
        }
    }
}