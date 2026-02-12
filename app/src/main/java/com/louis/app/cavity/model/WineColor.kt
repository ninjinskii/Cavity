package com.louis.app.cavity.model

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.louis.app.cavity.R

enum class WineColor(@param:ColorRes val colorRes: Int, @param:StringRes val stringRes: Int) {
    RED(R.color.wine_red, R.string.wine_color_red),
    WHITE(R.color.wine_white, R.string.wine_color_white),
    SWEET(R.color.wine_sweet, R.string.wine_color_sweet),
    ROSE(R.color.wine_rose, R.string.wine_color_rose)
}
