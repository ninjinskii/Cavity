package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHomeBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.ui.home.widget.ScrollableTabAdapter
import com.louis.app.cavity.util.setupNavigation

class FragmentHome : Fragment(R.layout.fragment_home) {
    private lateinit var tabAdapter: ScrollableTabAdapter<County>
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()
    private var recyclePool: RecyclerView.RecycledViewPool? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        recyclePool = RecyclerView.RecycledViewPool().apply {
            // TODO: Adjust this number based on screen size
            setMaxRecycledViews(R.layout.item_wine, 20) // 10 seems ok
        }

        setupScrollableTab()
        setListeners()
    }

    private fun setupScrollableTab() {
        tabAdapter = ScrollableTabAdapter(
            onTabClick = {
                binding.viewPager.currentItem = it
            },
            onLongTabClick = {
                // TODO: show dialog info for county
            }
        )

        homeViewModel.getAllCounties().observe(viewLifecycleOwner) {
            with(binding) {
                tab.adapter = tabAdapter
                viewPager.adapter = WinesPagerAdapter(this@FragmentHome, it)

                tabAdapter.addAll(it)
                tab.setUpWithViewPager(viewPager)
            }
            // Potential delayed coroutine and offscreen limit upgrade
            /*viewPager.offscreenPageLimit = 5
            tab.setUpWithViewPager(viewPager)*/
        }
    }

    private fun setListeners() {
        var currentCounty = 0L

        binding.tab.addOnPageChangeListener {
            currentCounty = tabAdapter.getItemId(it)
        }

        binding.fab.setOnClickListener {
            val action = FragmentHomeDirections.homeToAddWine(countyId = currentCounty)
            findNavController().navigate(action)
        }
    }

    fun getRecycledViewPool() = recyclePool

    override fun onDestroyView() {
        super.onDestroyView()
        recyclePool = null
        binding.tab.adapter = null
        binding.viewPager.adapter = null
        _binding = null
    }
}
