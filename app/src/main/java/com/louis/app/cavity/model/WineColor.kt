package com.louis.app.cavity.model

import androidx.annotation.ColorRes
import com.louis.app.cavity.R

enum class WineColor(@ColorRes val colorRes: Int) {
    RED(R.color.wine_red),
    WHITE(R.color.wine_white),
    SWEET(R.color.wine_sweet),
    ROSE(R.color.wine_rose)
}
