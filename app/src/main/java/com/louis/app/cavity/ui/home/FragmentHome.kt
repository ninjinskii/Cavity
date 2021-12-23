package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHomeBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.ui.home.widget.ScrollableTabAdapter
import com.louis.app.cavity.util.setupNavigation
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class FragmentHome : Fragment(R.layout.fragment_home) {
    private lateinit var tabAdapter: ScrollableTabAdapter<County>
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val recyclePool by lazy {
        RecyclerView.RecycledViewPool().apply {
            // TODO: Adjust this number based on screen size
            setMaxRecycledViews(R.layout.item_wine, 15)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        reenterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.cavity_motion_long).toLong()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        _binding = FragmentHomeBinding.bind(view)

        // Hack. On app launch, top bar is not bounded if not doing this
        lifecycleScope.launch(Main) {
            setupNavigation(binding.appBar.toolbar)
        }

        setupScrollableTab()
        setListeners()
    }

    private fun setupScrollableTab() {
        tabAdapter = ScrollableTabAdapter(
            onTabClick = { _, position ->
                binding.viewPager.currentItem = position
            },
            onLongTabClick = {
                // TODO: show dialog info for county
            }
        )

        homeViewModel.getAllCounties().observe(viewLifecycleOwner) {
            with(binding) {
                tab.adapter = tabAdapter
                viewPager.adapter =
                    WinesPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle, it)

                tabAdapter.addAll(it)
                tab.setUpWithViewPager(viewPager)

                (view?.parent as? ViewGroup)?.doOnPreDraw {
                    startPostponedEnterTransition()
                }
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
            exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
                duration = resources.getInteger(R.integer.cavity_motion_long).toLong()
                excludeTarget(R.id.appBar, true)
            }

            reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
                duration = resources.getInteger(R.integer.cavity_motion_long).toLong()
                excludeTarget(R.id.appBar, true)
            }

            val extra = FragmentNavigatorExtras(binding.appBar.root to "appbar")
            val action = FragmentHomeDirections.homeToAddWine(countyId = currentCounty)
            findNavController().navigate(action, extra)
        }
    }

    fun getRecycledViewPool() = recyclePool

    override fun onDestroyView() {
        super.onDestroyView()
        binding.tab.adapter = null
        _binding = null
    }
}
