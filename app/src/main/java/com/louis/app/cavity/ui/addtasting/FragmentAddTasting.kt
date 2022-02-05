package com.louis.app.cavity.ui.addtasting

import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.ui.search.FragmentSearch
import com.louis.app.cavity.ui.search.FragmentSearch.Companion.PICK_MODE
import com.louis.app.cavity.ui.stepper.Stepper
import com.louis.app.cavity.util.TransitionHelper

class FragmentAddTasting : Stepper() {
    override val showStepperProgress = false
    override val steps = setOf(
        FragmentInquireTastingInfo(),
        FragmentSearch().apply { arguments = bundleOf(PICK_MODE to true) },
        FragmentInquireSchedule()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TransitionHelper(this).apply {
            setFadeThrough(navigatingForward = true)
            setSharedAxisTransition(MaterialSharedAxis.Z, navigatingForward = false)
        }
    }
}
