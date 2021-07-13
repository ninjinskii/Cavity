package com.louis.app.cavity.ui.addtasting

import android.os.Bundle
import android.view.View
import com.louis.app.cavity.ui.SnackbarProvider
import com.louis.app.cavity.ui.stepper.Stepper

class FragmentAddTasting : Stepper() {
    lateinit var snackbarProvider: SnackbarProvider

    override val steps = setOf(FragmentInquireTastingInfo())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        snackbarProvider = activity as SnackbarProvider
    }
}
