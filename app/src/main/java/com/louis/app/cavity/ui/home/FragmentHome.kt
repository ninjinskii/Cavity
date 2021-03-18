package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHomeBinding
import com.louis.app.cavity.util.setupNavigation

class FragmentHome : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val recyclePool by lazy {
        RecyclerView.RecycledViewPool().apply {
            // TODO: Adjust this number based on screen size
            setMaxRecycledViews(R.layout.item_wine, 20) // 10 seems ok
            setMaxRecycledViews(R.layout.chip_action, 15) // 10 seems ok
        }
    }

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
            // TODO: show dialog info for county
        }

        homeViewModel.getAllCounties().observe(viewLifecycleOwner) {
            with(binding) {
                tab.addTabs(it)
                viewPager.adapter =
                    WinesPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle, it)

                tab.setUpWithViewPager(viewPager)

                // Potential delayed coroutine and offscreen limit upgrade
                /*viewPager.offscreenPageLimit = 5
                tab.setUpWithViewPager(viewPager)*/
            }
        }
    }

    private fun setListeners() {
        var currentCounty = 0L

        binding.tab.addOnPageChangeListener {
            currentCounty = binding.tab.adapter?.getItemId(it) ?: 0
        }

        binding.fab.setOnClickListener {
            val action = FragmentHomeDirections.homeToAddWine(countyId = currentCounty)
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

    fun getRecycledViewPool() = recyclePool

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
