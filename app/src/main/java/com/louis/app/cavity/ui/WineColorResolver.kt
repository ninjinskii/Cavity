package com.louis.app.cavity.ui

import android.content.Context
import com.louis.app.cavity.R

interface WineColorResolver {
    private val wineColors
        get() = listOf(R.color.wine_red, R.color.wine_white, R.color.wine_sweet, R.color.wine_rose)
            .map { getOverallContext().getColor(it) }

    private val fallback
        get() = getOverallContext().getColor(R.color.cavity_gold)

    fun getOverallContext(): Context

    fun resolveColor(colorNumber: Int): Int {
        return try {
            wineColors[colorNumber]
        } catch (e: IndexOutOfBoundsException) {
            fallback
        }
    }
}
