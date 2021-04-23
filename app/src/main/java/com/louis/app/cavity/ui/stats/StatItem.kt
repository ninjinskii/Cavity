package com.louis.app.cavity.ui.stats

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

interface StatItem {
    val name: Any
    val count: Int
    val color: Int?
    val icon: Int?

    fun resolveIfNotDone(context: Context): StringStatItem
}

data class StringStatItem(
    override val name: String,
    override val count: Int,
    @ColorRes override val color: Int?,
    @DrawableRes override val icon: Int?
) :
    StatItem {

    override fun resolveIfNotDone(context: Context) = this
}

data class StringResStatItem(
    @StringRes override val name: Int,
    override val count: Int,
    @ColorRes override val color: Int?,
    @DrawableRes override val icon: Int?
) :
    StatItem {

    override fun resolveIfNotDone(context: Context): StringStatItem {
        return StringStatItem(
            context.getString(name),
            count,
            color,
            icon // Doesn't need to be resolved rn
        )
    }
}

data class Stat(val statItems: List<StatItem>) {
    val total get() = statItems.sumBy { it.count }
}
