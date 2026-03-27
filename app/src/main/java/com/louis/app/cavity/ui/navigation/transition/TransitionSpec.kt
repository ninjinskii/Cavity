package com.louis.app.cavity.ui.navigation.transition

import android.os.Parcelable
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface TransitionSpec : Parcelable {
    @Parcelize
    data object None : TransitionSpec, Parcelable

    @Parcelize
    data object FadeThrough : TransitionSpec, Parcelable

    @Parcelize
    data object ElevationScale : TransitionSpec, Parcelable

    @Parcelize
    data class SharedAxis(val axis: Axis) : TransitionSpec, Parcelable
}

@Parcelize
sealed interface SharedElementTransitionSpec : Parcelable {
    @Parcelize
    data object None : SharedElementTransitionSpec, Parcelable

    @Parcelize
    data class ContainerTransform(
        @param:AttrRes @param:ColorRes val startContainerColor: Int? = null,
        @param:AttrRes @param:ColorRes val endContainerColor: Int? = null,
        @param:DimenRes val startElevation: Int? = null,
        @param:DimenRes val endElevation: Int? = null
    ) :
        SharedElementTransitionSpec, Parcelable
}

enum class Axis {
    X, Y, Z
}

