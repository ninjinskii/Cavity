package com.louis.app.cavity.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHomeBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.ui.home.widget.ScrollableTabAdapter
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.setupNavigation
import com.louis.app.cavity.util.themeColor
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class FragmentHome : Fragment(R.layout.fragment_home) {
    private lateinit var tabAdapter: ScrollableTabAdapter<County>
    private lateinit var transitionHelper: TransitionHelper
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

        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.countyDetailsScrim.isVisible) {
                        hideCountyDetails()
                    } else {
                        remove()
                        requireActivity().onBackPressed()
                    }
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // We need to do this in onViewCreated to ensure the right transition is selected when returning to this fragment
        transitionHelper = TransitionHelper(this).apply {
            setFadeThroughOnEnterAndExit()
        }
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
            onLongTabClick = { _, position ->
                showCountyDetails(position)
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
            transitionHelper.setSharedAxisTransition(MaterialSharedAxis.Z, navigatingForward = true)

            val extra = FragmentNavigatorExtras(binding.appBar.root to "appbar")
            val action = FragmentHomeDirections.homeToAddWine(countyId = currentCounty)
            findNavController().navigate(action, extra)
        }

        binding.countyDetailsScrim.setOnClickListener {
            hideCountyDetails()
        }
    }

    private fun showCountyDetails(itemPosition: Int) {
        binding.viewPager.currentItem = itemPosition

        val transform = MaterialContainerTransform().apply {
            startView = binding.tab
            endView = binding.countyDetails.root
            startElevation = resources.getDimension(R.dimen.app_bar_elevation)
            endElevation = resources.getDimension(R.dimen.app_bar_elevation)
            scrimColor = Color.TRANSPARENT
            addTarget(binding.countyDetails.root)
        }

        TransitionManager.beginDelayedTransition(binding.constraint, transform)

        with(binding) {
            tab.setVisible(false, invisible = true)
            fab.hide()
            countyDetails.root.setVisible(true)
            countyDetailsScrim.setVisible(true)
        }
    }

    private fun hideCountyDetails() {
        val transform = MaterialContainerTransform().apply {
            startView = binding.countyDetails.root
            endView = binding.tab
            startElevation = resources.getDimension(R.dimen.app_bar_elevation)
            endElevation = resources.getDimension(R.dimen.app_bar_elevation)
            endContainerColor = requireContext().themeColor(R.attr.colorSurface)
            scrimColor = Color.TRANSPARENT
            addTarget(binding.tab)
        }

        TransitionManager.beginDelayedTransition(binding.constraint, transform)

        with(binding) {
            tab.setVisible(true)
            fab.show()
            countyDetails.root.setVisible(false, invisible = true)
            countyDetailsScrim.setVisible(false)
        }
    }

    fun getRecycledViewPool() = recyclePool

    override fun onDestroyView() {
        super.onDestroyView()
        binding.tab.adapter = null
        _binding = null
    }
}
