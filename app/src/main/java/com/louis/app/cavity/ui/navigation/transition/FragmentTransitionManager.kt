package com.louis.app.cavity.ui.navigation.transition

import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.louis.app.cavity.ui.navigation.AppRoute

interface FragmentTransitionManager {
    var pendingDestinationTransition: TransitionSpec
    var pendingSharedElementDestinationTransition: SharedElementTransitionSpec
    fun configureFragment(source: Fragment, toRoute: AppRoute)
    fun configureDestinationFragment(destination: Fragment)
    fun restoreState(fragment: Fragment)
    fun saveState(fragment: Fragment, key: String, spec: Parcelable)
}
