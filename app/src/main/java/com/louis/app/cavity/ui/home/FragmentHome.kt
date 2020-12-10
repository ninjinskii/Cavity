package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHomeBinding
import com.louis.app.cavity.util.setupNavigation

class FragmentHome : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        setupScrollableTab()
        setListeners()
        observe()
    }

    private fun setupScrollableTab() {
        binding.tab.addOnLongClickListener {
            // show dialog info for county
        }

        homeViewModel.getAllCounties().observe(viewLifecycleOwner) {
            // Potential coroutine
            with(binding) {
                tab.addTabs(it)
                // viewPager.isSaveEnabled = false // might correct the crash when getting back to home sometimes, but reduce apps perfs a lot
                viewPager.adapter = WinesPagerAdapter(requireActivity(), it)
                viewPager.offscreenPageLimit = 1
                tab.setUpWithViewPager(viewPager)

                // Here it seems possible to delay the coroutine a couple of seconds
                // and then set a higher offscreenPageLimit
                // This doesnt feel great, but does not create a memory overhead also.
                /* tab.setUpWithViewPager(viewPager)
                delay(2000)
                viewPager.offscreenPageLimit = 10 // was 5
                tab.setUpWithViewPager(viewPager) */
            }
        }
    }

    private fun setListeners() {
        binding.fab.setOnClickListener {
            val action = FragmentHomeDirections.homeToAddWine()
            findNavController().navigate(action)
        }
    }

    private fun observe() {
        homeViewModel.isScrollingToTop.observe(viewLifecycleOwner) {
            with(binding) {
                if (it) fab.run { if (!isShown) show() }
                else fab.run { if (isShown) hide() }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
