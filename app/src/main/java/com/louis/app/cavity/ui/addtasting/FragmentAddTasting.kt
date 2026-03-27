package com.louis.app.cavity.ui.addtasting

import androidx.core.os.bundleOf
import com.louis.app.cavity.ui.search.FragmentSearch
import com.louis.app.cavity.ui.search.FragmentSearch.Companion.PICK_MODE
import com.louis.app.cavity.ui.stepper.Stepper

class FragmentAddTasting : Stepper() {
    override val showStepperProgress = false
    override val steps = listOf(
        { FragmentInquireTastingInfo() },
        { FragmentSearch().apply { arguments = bundleOf(PICK_MODE to true) } },
        { FragmentInquireSchedule() }
    )
}
