package com.louis.app.cavity.util

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.louis.app.cavity.R
import com.louis.app.cavity.model.FReview
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.ui.addbottle.viewmodel.FReviewUiModel

class ColorUtil(context: Context) {
    enum class ColorCategory {
        WINES,
        MEDALS,
        PRIMARY,
        OTHER,
    }

    companion object {
        @ColorRes
        fun getColorResForWineColor(wineColor: Int) = when (wineColor) {
            0 -> R.color.wine_red
            1 -> R.color.wine_white
            2 -> R.color.wine_sweet
            3 -> R.color.wine_rose
            else -> throw IllegalArgumentException("Wine color $wineColor does not exists")
        }
    }

    private val wineColors by lazy {
        listOf(
            R.color.wine_red,
            R.color.wine_white,
            R.color.wine_sweet,
            R.color.wine_rose
        ).map { it to ContextCompat.getColor(context, it) }
    }

    private val medalColors by lazy {
        listOf(
            R.color.medal_bronze,
            R.color.medal_silver,
            R.color.medal_gold
        ).map { it to ContextCompat.getColor(context, it) }
    }

    private val colors by lazy {
        listOf(
            R.color.cavity_red,
            R.color.cavity_brown,
            R.color.cavity_light_green,
            R.color.cavity_indigo,
            R.color.cavity_purple,
            R.color.cavity_yellow
        ).map { it to ContextCompat.getColor(context, it) }
    }

    private val colorPrimary by lazy {
        ContextCompat.getColor(context, R.color.cavity_gold)
    }

    @ColorInt
    fun getWineColor(wine: Wine) = try {
        wineColors.map { it.second }[wine.color]
    } catch (e: IndexOutOfBoundsException) {
        colorPrimary
    }

    @ColorInt
    fun getWineColor(colorInt: Int) = try {
        wineColors.map { it.second }[colorInt]
    } catch (e: IndexOutOfBoundsException) {
        colorPrimary
    }

    @ColorInt
    fun getMedalColor(fReview: FReviewUiModel) = try {
        medalColors.map { it.second }[fReview.value]
    } catch (e: IndexOutOfBoundsException) {
        colorPrimary
    }

    @ColorInt
    fun getMedalColor(fReview: FReview) = try {
        medalColors.map { it.second }[fReview.value]
    } catch (e: IndexOutOfBoundsException) {
        colorPrimary
    }

    @ColorInt
    fun getColor(@ColorRes colorRes: Int, cat: ColorCategory): Int {
        val color: Int? = when (cat) {
            ColorCategory.WINES -> wineColors.find { it.first == colorRes }?.second
            ColorCategory.MEDALS -> medalColors.find { it.first == colorRes }?.second
            ColorCategory.PRIMARY -> colorPrimary
            ColorCategory.OTHER -> colors.find { it.first == colorRes }?.second
        }

        return color ?: throw IllegalArgumentException("Color $colorRes absent of $cat")
    }

    fun randomSet() = colors.map { it.second }.shuffled()
}
