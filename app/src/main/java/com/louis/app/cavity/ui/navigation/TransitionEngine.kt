package com.louis.app.cavity.ui.navigation

import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.ui.navigation.TransitionHelper

interface TransitionEngine {
    fun apply(fromFragment: Fragment?, toRoute: AppRoute)
}

class MaterialTransitionEngine : TransitionEngine {
    override fun apply(fromFragment: Fragment?, toRoute: AppRoute) {
        val transitionHelper = TransitionHelper(fromFragment!!)

        when (toRoute) {
            is HomeRoute.AddWine -> transitionHelper.setSharedAxisTransition(
                MaterialSharedAxis.Z,
                true
            )

            is HomeRoute.BottleDetails -> Unit
            is HomeRoute.Settings -> Unit
            else -> Unit
        }
    }

}
