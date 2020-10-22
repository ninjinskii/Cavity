package com.louis.app.cavity.ui.addbottle

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentAddBottleBinding
import com.louis.app.cavity.ui.ActivityMain
import com.louis.app.cavity.ui.addbottle.stepper.AddBottlesPagerAdapter
import com.louis.app.cavity.ui.addbottle.stepper.FragmentStepper
import com.louis.app.cavity.ui.home.FragmentWines.Companion.WINE_ID
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.setupDefaultToolbar

class FragmentAddBottle : Fragment(R.layout.fragment_add_bottle) {
    private lateinit var activity: ActivityMain
    private var _binding: FragmentAddBottleBinding? = null
    private val binding get() = _binding!!
    private val addBottleViewModel: AddBottleViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddBottleBinding.bind(view)

        setupDefaultToolbar(activity, binding.appBarDefault.toolbar)

        if (arguments?.getLong(WINE_ID) == null) {
            activity.onShowSnackbarRequested(R.string.base_error)
            findNavController().popBackStack()
            return
        }

        arguments?.let {
            L.v("Start viewModel")
            addBottleViewModel.start(it.getLong(WINE_ID), it.getLong(EDIT_BOTTLE_ID))
        }

        val stepperFragment = childFragmentManager.findFragmentById(R.id.stepper) as FragmentStepper

        binding.viewPager
            .apply { adapter = AddBottlesPagerAdapter(this@FragmentAddBottle) }
            .also { stepperFragment.setupWithViewPager(it) }

        activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding.viewPager.currentItem != 0) {
                binding.viewPager.currentItem = binding.viewPager.currentItem - 1
            } else {
                remove()
                activity.onBackPressed()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val EDIT_BOTTLE_ID =
            "com.louis.app.cavity.ui.addbottle.FragmentAddBottle.ARG_EDIT_BOTTLE_ID"
    }
}
