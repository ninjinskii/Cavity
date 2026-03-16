package com.louis.app.cavity.ui.addtasting

import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.ui.navigation.NavigationDestination
import com.louis.app.cavity.ui.search.FragmentSearch
import com.louis.app.cavity.ui.search.FragmentSearch.Companion.PICK_MODE
import com.louis.app.cavity.ui.stepper.Stepper
import com.louis.app.cavity.ui.navigation.transition.MaterialTransitionHelper

class FragmentAddTasting : Stepper(), NavigationDestination {
    override val menuDestinationId = R.id.tasting_dest
    override val showStepperProgress = false
    override val steps = listOf(
        { FragmentInquireTastingInfo() },
        { FragmentSearch().apply { arguments = bundleOf(PICK_MODE to true) } },
        { FragmentInquireSchedule() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MaterialTransitionHelper(this).apply {
            setFadeThrough(navigatingForward = true)
            setSharedAxisTransition(MaterialSharedAxis.Z, navigatingForward = false)
        }
    }
}
