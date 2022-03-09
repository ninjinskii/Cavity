package com.louis.app.cavity.model

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.louis.app.cavity.R

enum class WineColor(@ColorRes val colorRes: Int, @StringRes val stringRes: Int, val order: Int) {
    RED(R.color.wine_red, R.string.wine_color_red, 0),
    WHITE(R.color.wine_white, R.string.wine_color_white, 1),
    SWEET(R.color.wine_sweet, R.string.wine_color_sweet, 2),
    ROSE(R.color.wine_rose, R.string.wine_color_rose, 3)
}
