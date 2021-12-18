package com.louis.app.cavity.model

import androidx.annotation.ColorRes
import com.louis.app.cavity.R

enum class WineColor(@ColorRes val colorRes: Int, val order: Int) {
    RED(R.color.wine_red, 0),
    WHITE(R.color.wine_white, 1),
    SWEET(R.color.wine_sweet, 2),
    ROSE(R.color.wine_rose, 3)
}
