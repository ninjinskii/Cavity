package com.louis.app.cavity.ui.addtasting

import android.os.Bundle
import android.view.View
import com.louis.app.cavity.databinding.FragmentStepperBinding
import com.louis.app.cavity.ui.FragmentStepper
import com.louis.app.cavity.ui.SnackbarProvider

class FragmentAddTasting : FragmentStepper() {
    lateinit var snackbarProvider: SnackbarProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStepperBinding.bind(view)

        snackbarProvider = activity as SnackbarProvider
    }

    override fun getPagerAdapter() = AddTastingPagerAdapter(this)
}
