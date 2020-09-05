package com.louis.app.cavity.ui.bottle

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentAddBottleBinding
import com.louis.app.cavity.ui.bottle.stepper.AddBottlesPagerAdapter
import com.louis.app.cavity.ui.bottle.stepper.FragmentStepper

class FragmentAddBottle : Fragment(R.layout.fragment_add_bottle) {
    private lateinit var binding: FragmentAddBottleBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddBottleBinding.bind(view)

        val stepperFragment = childFragmentManager.findFragmentById(R.id.stepper) as FragmentStepper
        stepperFragment.resetState()

        binding.viewPager
            .apply { adapter = AddBottlesPagerAdapter(this@FragmentAddBottle) }
            .also { stepperFragment.setupWithViewPager(it) }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding.viewPager.currentItem != 0) {
                binding.viewPager.currentItem = binding.viewPager.currentItem - 1
            } else {
                remove()
                requireActivity().onBackPressed()
            }
        }

    }
}
