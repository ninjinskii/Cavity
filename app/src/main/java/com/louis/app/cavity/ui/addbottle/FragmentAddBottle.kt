package com.louis.app.cavity.ui.addbottle

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import androidx.navigation.fragment.navArgs
import com.louis.app.cavity.ui.SnackbarProvider
import com.louis.app.cavity.ui.addbottle.viewmodel.AddBottleEvent
import com.louis.app.cavity.ui.addbottle.viewmodel.AddBottleViewModel
import com.louis.app.cavity.ui.stepper.Stepper
import com.louis.app.cavity.ui.widget.friendpicker.FriendPickerViewModel
import com.louis.app.cavity.util.showSnackbar

class FragmentAddBottle : Stepper() {
    private lateinit var snackbarProvider: SnackbarProvider
    private val addBottleViewModel: AddBottleViewModel by viewModels()
    private val friendPickerViewModel: FriendPickerViewModel by viewModels()
    private val args: FragmentAddBottleArgs by navArgs()

    override val showStepperProgress = true
    override val steps = listOf(
        { FragmentInquireDates() },
        { FragmentInquireGrapes() },
        { FragmentInquireReviews() },
        { FragmentInquireOtherInfo() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        snackbarProvider = activity as SnackbarProvider

        // editedBottleId is equal to 0 if user is not editing a bottle, but adding a new one
        addBottleViewModel.start(args.wineId, args.editedBottleId)

        if (args.editedBottleId > 0L) {
            friendPickerViewModel.fetchFriendsFromEditedBottleId(args.editedBottleId)
        }

        observe()
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                addBottleViewModel.event.collect { event ->
                    when (event) {
                        is AddBottleEvent.UserFeedback ->
                            binding.coordinator.showSnackbar(event.resId)
                        is AddBottleEvent.Completed -> {
                            findNavController().popBackStack()
                            snackbarProvider.onShowSnackbarRequested(event.resId)
                        }
                    }
                }
            }
        }
    }
}
