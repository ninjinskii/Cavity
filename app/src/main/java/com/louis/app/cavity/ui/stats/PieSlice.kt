package com.louis.app.cavity.ui.stats

import androidx.annotation.ColorRes
import androidx.annotation.StringRes


interface PieSlice {
    val angle: Float
    val color: Int?
}

data class StringPieSlice(
    val name: String,
    override val angle: Float,
    @ColorRes override val color: Int?
) : PieSlice

data class ResPieSlice(
    @StringRes val name: Int,
    override val angle: Float,
    @ColorRes override val color: Int?
) : PieSlice


