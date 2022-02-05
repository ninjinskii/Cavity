package com.louis.app.cavity.model

import androidx.annotation.StringRes
import com.louis.app.cavity.R

enum class BottleSize(@StringRes val stringRes: Int) {
    SLIM(R.string.size_slim),
    NORMAL(R.string.size_normal),
    MAGNUM(R.string.size_magnum)
}
