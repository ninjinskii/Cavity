package com.louis.app.cavity.ui.addbottle

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentAddBottleBinding
import com.louis.app.cavity.ui.SnackbarProvider
import com.louis.app.cavity.ui.addbottle.stepper.AddBottlesPagerAdapter
import com.louis.app.cavity.ui.addbottle.stepper.Step
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.showSnackbar

class FragmentAddBottle : Fragment(R.layout.fragment_add_bottle) {
    lateinit var snackbarProvider: SnackbarProvider
    private var _binding: FragmentAddBottleBinding? = null
    private val binding get() = _binding!!
    private val addBottleViewModel: AddBottleViewModel by activityViewModels()
    private val args: FragmentAddBottleArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddBottleBinding.bind(view)

        snackbarProvider = activity as SnackbarProvider

        // editedBottleId is equal to 0 if user is not editing a bottle, but adding a new one
        addBottleViewModel.start(args.wineId, args.editedBottleId)

        initStepper()
        setupCustomBackNav()
        observe()
    }

    private fun initStepper() {
        binding.viewPager.adapter = AddBottlesPagerAdapter(this)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                val stepToCheck = childFragmentManager.findFragmentByTag("f$position") as? Step
                if (stepToCheck?.validate() != true) binding.viewPager.currentItem = position
            }
        })
    }

    private fun setupCustomBackNav() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding.viewPager.currentItem != 0) {
                binding.viewPager.currentItem = binding.viewPager.currentItem - 1
            } else {
                remove()
                requireActivity().onBackPressed()
            }
        }
    }

    private fun observe() {
        addBottleViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                binding.coordinator.showSnackbar(stringRes)
            }
        }

        addBottleViewModel.bottleUpdatedEvent.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                snackbarProvider.onShowSnackbarRequested(stringRes)
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
