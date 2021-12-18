package com.louis.app.cavity.ui.addtasting

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.louis.app.cavity.ui.SnackbarProvider
import com.louis.app.cavity.ui.search.FragmentSearch
import com.louis.app.cavity.ui.search.FragmentSearch.Companion.PICK_MODE
import com.louis.app.cavity.ui.stepper.Stepper

class FragmentAddTasting : Stepper() {
    lateinit var snackbarProvider: SnackbarProvider

    override val steps = setOf(
        FragmentInquireTastingInfo(),
        FragmentSearch().apply { arguments = bundleOf(PICK_MODE to true) },
        FragmentInquireSchedule()
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        snackbarProvider = activity as SnackbarProvider
    }
}
