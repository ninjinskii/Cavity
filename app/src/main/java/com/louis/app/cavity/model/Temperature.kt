package com.louis.app.cavity.model

import android.content.Context
import com.louis.app.cavity.R

sealed class Temperature(val value: Int) {
    companion object {
        // CELSIUS
        const val DEFAULT_CELLAR_TEMP = 15
        const val DEFAULT_FRIDGE_TEMP = 5
        const val DEFAULT_FREEZER_TEMP = -18
        const val DEFAULT_DAY_TEMP = 20
        const val MAX_CELLAR_TEMP = 25
        const val MAX_FRIDGE_TEMP = 10
        const val MAX_FREEZER_TEMP = -10
        const val MAX_DAY_TEMP = 45
        const val MIN_CELLAR_TEMP = 5
        const val MIN_FRIDGE_TEMP = 1
        const val MIN_FREEZER_TEMP = -35
        const val MIN_DAY_TEMP = -15
        const val DEFAULT_RED_TEMP = 15
        const val DEFAULT_WHITE_TEMP = 10
        const val DEFAULT_SWEET_TEMP = 5
        const val DEFAULT_ROSE_TEMP = 6
    }

    abstract fun getUnitString(context: Context): String

    // abstract fun getDefaultCellarTemp()
    // ...

    // Celsius is considered as base unit. Every other unit should be translated from Celsius.
    class Celsius(value: Int) : Temperature(value) {
        override fun getUnitString(context: Context) =
            context.getString(R.string.temp_celsius, value)
    }

    class Fahrenheit(celsius: Int) : Temperature((celsius * (9 / 5)) + 32) {
        override fun getUnitString(context: Context) =
            context.getString(R.string.temp_fahrenheit, value)
    }
}
