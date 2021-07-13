package com.louis.app.cavity.ui.stepper

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentStepperBinding

abstract class Stepper : Fragment(R.layout.fragment_stepper) {

    // Sublcasses would be confusing to read
    @Suppress("PropertyName")
    protected var _binding: FragmentStepperBinding? = null
    val binding get() = _binding!!

    abstract val steps: Set<Step>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStepperBinding.bind(view)

        init()
        setupCustomBackNav()
    }

    private fun init() {
        val pagerAdapter = StepperPagerAdapter(this, steps)

        binding.viewPager.apply {
            adapter = pagerAdapter
            isUserInputEnabled = false
        }

    }

    private fun setupCustomBackNav() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding.viewPager.currentItem != 0) {
                requestPreviousPage()
            } else {
                remove()
                requireActivity().onBackPressed()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun requestNextPage(): Int {
        return ++binding.viewPager.currentItem
    }

    fun requestPreviousPage(): Int {
        return --binding.viewPager.currentItem
    }
}
