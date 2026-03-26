package com.louis.app.cavity.ui.navigation.transition

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.louis.app.cavity.ui.navigation.AppRoute
import com.louis.app.cavity.ui.navigationnext.NavTransitionAnimations
import com.louis.app.cavity.util.getParcelableCompat

abstract class FragmentArgumentsTransitionManager : FragmentTransitionManager {
    override var pendingDestinationTransition: TransitionSpec = TransitionSpec.None
    override var pendingSharedElementDestinationTransition: SharedElementTransitionSpec =
        SharedElementTransitionSpec.None

    final override fun configureFragment(source: Fragment, toRoute: AppRoute) {
        pendingDestinationTransition = toRoute.destinationTransition
        pendingSharedElementDestinationTransition = toRoute.sharedElementTransition

        saveState(source, TRANSITION_OUT_KEY, toRoute.transition)
        applyTransition(source, toRoute.transition, navigatingForward = true)
    }

    final override fun configureFragment(source: Fragment, animations: NavTransitionAnimations) {
        pendingDestinationTransition = animations.spec
        pendingSharedElementDestinationTransition = animations.sharedSpec ?: SharedElementTransitionSpec.None

        saveState(source, TRANSITION_OUT_KEY, pendingDestinationTransition)
        applyTransition(source, pendingDestinationTransition, navigatingForward = true)
    }

    final override fun configureDestinationFragment(destination: Fragment) {
        saveState(destination, TRANSITION_IN_KEY, pendingDestinationTransition)
        saveState(destination, SHARED_TRANSITION_IN_KEY, pendingSharedElementDestinationTransition)

        applyTransition(destination, pendingDestinationTransition, navigatingForward = false)
        applySharedTransition(destination, pendingSharedElementDestinationTransition)
    }

    final override fun restoreState(fragment: Fragment) {
        fragment.arguments?.run {
            getParcelableCompat<TransitionSpec>(TRANSITION_IN_KEY)?.let {
                applyTransition(fragment, it, navigatingForward = false)
            }

            getParcelableCompat<SharedElementTransitionSpec>(SHARED_TRANSITION_IN_KEY)?.let {
                applySharedTransition(fragment, it)
            }

            getParcelableCompat<TransitionSpec>(TRANSITION_OUT_KEY)?.let {
                applyTransition(fragment, it, navigatingForward = true)
            }
        }
    }

    final override fun saveState(
        fragment: Fragment,
        key: String,
        spec: Parcelable
    ) {
        fragment.arguments = (fragment.arguments ?: Bundle()).apply {
            putParcelable(key, spec)
        }
    }

    abstract fun applyTransition(
        fragment: Fragment,
        spec: TransitionSpec,
        navigatingForward: Boolean
    )

    abstract fun applySharedTransition(fragment: Fragment, spec: SharedElementTransitionSpec)

    companion object {
        private const val TRANSITION_OUT_KEY = "transition-out"
        private const val TRANSITION_IN_KEY = "transition-in"
        private const val SHARED_TRANSITION_IN_KEY = "shared-transition-in"
    }
}
