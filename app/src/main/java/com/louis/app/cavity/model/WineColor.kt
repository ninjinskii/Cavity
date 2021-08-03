package com.louis.app.cavity.model

import androidx.annotation.ColorRes
import com.louis.app.cavity.R

enum class WineColor(@ColorRes val colorRes: Int, val defaultTemperature: Temperature) {
    RED(R.color.wine_red, Temperature.Celsius(15)),
    WHITE(R.color.wine_white, Temperature.Celsius(10)),
    SWEET(R.color.wine_sweet, Temperature.Celsius(5)),
    ROSE(R.color.wine_rose, Temperature.Celsius(6))
}
