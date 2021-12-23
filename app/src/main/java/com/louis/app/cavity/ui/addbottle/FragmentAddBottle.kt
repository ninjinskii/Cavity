package com.louis.app.cavity.ui.addbottle

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.ui.SnackbarProvider
import com.louis.app.cavity.ui.addbottle.viewmodel.AddBottleViewModel
import com.louis.app.cavity.ui.stepper.Stepper
import com.louis.app.cavity.util.showSnackbar

class FragmentAddBottle : Stepper() {
    lateinit var snackbarProvider: SnackbarProvider
    private val addBottleViewModel: AddBottleViewModel by viewModels()
    private val args: FragmentAddBottleArgs by navArgs()

    override val steps = setOf(
        FragmentInquireDates(),
        FragmentInquireGrapes(),
        FragmentInquireReviews(),
        FragmentInquireOtherInfo()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.cavity_motion_long).toLong()
        }

        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.cavity_motion_long).toLong()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        snackbarProvider = activity as SnackbarProvider

        // editedBottleId is equal to 0 if user is not editing a bottle, but adding a new one
        addBottleViewModel.start(args.wineId, args.editedBottleId)

        view.doOnPreDraw { startPostponedEnterTransition() }
        observe()
    }

    private fun observe() {
        addBottleViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                binding.coordinator.showSnackbar(stringRes)
            }
        }

        addBottleViewModel.completedEvent.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                findNavController().popBackStack()
                // Using snackbar provider since we are quitting this fragment
                snackbarProvider.onShowSnackbarRequested(stringRes)
            }
        }
    }
}
