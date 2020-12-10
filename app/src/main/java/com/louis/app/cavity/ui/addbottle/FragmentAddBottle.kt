package com.louis.app.cavity.ui.addbottle

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentAddBottleBinding
import com.louis.app.cavity.ui.SnackbarProvider
import com.louis.app.cavity.ui.addbottle.stepper.AddBottlesPagerAdapter
import com.louis.app.cavity.ui.addbottle.stepper.Stepper
import com.louis.app.cavity.ui.addbottle.viewmodel.*
import com.louis.app.cavity.util.showSnackbar

class FragmentAddBottle : Fragment(R.layout.fragment_add_bottle), Stepper {
    lateinit var snackbarProvider: SnackbarProvider
    private var _binding: FragmentAddBottleBinding? = null
    private val binding get() = _binding!!
    private val dateViewModel: DateViewModel by viewModels()
    private val grapeViewModel: GrapeViewModel by viewModels()
    private val reviewViewModel: ReviewViewModel by viewModels()
    private val otherInfoViewModel: OtherInfoViewModel by viewModels()
    private val args: FragmentAddBottleArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddBottleBinding.bind(view)

        snackbarProvider = activity as SnackbarProvider

        // editedBottleId is equal to 0 if user is not editing a bottle, but adding a new one
        dateViewModel.start(args.wineId, args.editedBottleId)

        initStepper()
        setupCustomBackNav()
        observe()
    }

    private fun initStepper() {
        binding.viewPager.apply {
            adapter = AddBottlesPagerAdapter(this@FragmentAddBottle)
            isUserInputEnabled = false
        }
    }

    private fun setupCustomBackNav() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding.viewPager.currentItem != 0) {
                binding.viewPager.currentItem = binding.viewPager.currentItem - 1
            } else {
                //addBottleViewModel.onCancel() // TODO find a way to remove uncompleted bottle when straight killing app
                remove()
                requireActivity().onBackPressed()
            }
        }
    }

    private fun observe() {
        dateViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                binding.coordinator.showSnackbar(stringRes)
            }
        }

        grapeViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                binding.coordinator.showSnackbar(stringRes)
            }
        }

        reviewViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                binding.coordinator.showSnackbar(stringRes)
            }
        }

        otherInfoViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                binding.coordinator.showSnackbar(stringRes)
            }
        }

        otherInfoViewModel.bottleUpdatedEvent.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                snackbarProvider.onShowSnackbarRequested(stringRes)
                findNavController().navigateUp()
            }
        }
    }

    override fun requestNextPage() {
        binding.viewPager.currentItem++
    }

    override fun requestPreviousPage() {
        binding.viewPager.currentItem--
    }

    override fun getBottleId(): Long {
        return dateViewModel.bottleId
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
