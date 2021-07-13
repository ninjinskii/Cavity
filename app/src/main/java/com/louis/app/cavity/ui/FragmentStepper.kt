package com.louis.app.cavity.ui

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentStepperBinding

abstract class FragmentStepper : Fragment(R.layout.fragment_stepper) {

    // Sublcasses would be confusing to read
    @Suppress("PropertyName")
    protected var _binding: FragmentStepperBinding? = null
    val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStepperBinding.bind(view)

        init()
        setupCustomBackNav()
    }

    private fun init() {
        binding.viewPager.apply {
            adapter = getPagerAdapter()
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

    protected fun registerAdapter(adapter: FragmentStateAdapter) {
        binding.viewPager.adapter = adapter
    }

    protected fun requestNextPage(): Int {
        return ++binding.viewPager.currentItem
    }

    protected fun requestPreviousPage(): Int {
        return --binding.viewPager.currentItem
    }

    abstract fun getPagerAdapter(): FragmentStateAdapter
}
