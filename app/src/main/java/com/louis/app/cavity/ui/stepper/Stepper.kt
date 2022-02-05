package com.louis.app.cavity.ui.stepper

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentStepperBinding
import com.louis.app.cavity.util.setVisible

abstract class Stepper : Fragment(R.layout.fragment_stepper) {

    // Sublcasses would be confusing to read
    @Suppress("PropertyName")
    protected var _binding: FragmentStepperBinding? = null
    val binding get() = _binding!!

    abstract val showStepperProgress: Boolean
    abstract val steps: Set<Step>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStepperBinding.bind(view)

        init()
        setupCustomBackNav()
        setupStepper()
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
                goToPreviousPage()
            } else {
                remove()
                requireActivity().onBackPressed()
            }
        }
    }

    private fun setupStepper() {
        if (showStepperProgress) {
            with(binding.stepper) {
                previous.setOnClickListener { goToPreviousPage() }
                next.setOnClickListener { goToNextPage() }
            }
        } else {
            binding.stepper.root.setVisible(false)
        }
    }

    private fun updateIcons(pagerPosition: Int) {
        val isLastPage = pagerPosition == steps.size - 1
        binding.stepper.next.isActivated = isLastPage

        val isFirstPage = pagerPosition == 0
        binding.stepper.previous.isEnabled = !isFirstPage
    }

    override fun onResume() {
        super.onResume()
        updateIcons(binding.viewPager.currentItem)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun goToNextPage(): Int {
        val currentPage = binding.viewPager.currentItem
        val ok = steps.elementAt(currentPage).requestNextPage()

        return if (ok) {
            val nextPage = ++binding.viewPager.currentItem
            updateIcons(nextPage)

            nextPage
        } else {
            currentPage
        }

    }

    fun goToPreviousPage(): Int {
        val previousPage = --binding.viewPager.currentItem
        updateIcons(previousPage)

        return previousPage
    }
}
