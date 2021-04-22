package com.louis.app.cavity.ui.stats

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

data class PieSlice(val name: String, val angle: Float, @ColorInt val color: Int?)

/**
 * This class is used by ViewModels since they should not resolve resources themselves
 */
data class UnresolvedPieSlice(
    @StringRes val name: Int,
    val angle: Float,
    @ColorRes val color: Int?
) :
    Resolver {

    override fun resolve(context: Context) = PieSlice(
        context.getString(name),
        angle,
        color?.let { ContextCompat.getColor(context, it) }
    )
}

data class UnresolvedColorPieSlice(
    val name: String,
    val angle: Float,
    @ColorRes val color: Int?
) :
    Resolver {

    override fun resolve(context: Context) = PieSlice(
        name,
        angle,
        color?.let { ContextCompat.getColor(context, it) }
    )
}

interface Resolver {
    fun resolve(context: Context): PieSlice
}
