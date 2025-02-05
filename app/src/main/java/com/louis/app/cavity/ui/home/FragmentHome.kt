package com.louis.app.cavity.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.marginRight
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigationrail.NavigationRailView
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHomeBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.ui.home.widget.ScrollableTabAdapter
import com.louis.app.cavity.util.*

class FragmentHome : Fragment(R.layout.fragment_home) {

    companion object {
        const val VIEW_POOL_SIZE = 25
    }

    private lateinit var transitionHelper: TransitionHelper
    private var tabAdapter: ScrollableTabAdapter<County>? = null
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val recyclePool by lazy {
        RecyclerView.RecycledViewPool().apply {
            setMaxRecycledViews(R.layout.item_wine, VIEW_POOL_SIZE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            this
        ) {
            if (binding.countyDetailsScrim.isVisible) {
                hideCountyDetails()
            } else {
                remove()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // We need to do this in onViewCreated to ensure the right transition is selected when returning to this fragment
        transitionHelper = TransitionHelper(this).apply {
            setFadeThroughOnEnterAndExit()
        }
        postponeEnterTransition()

        _binding = FragmentHomeBinding.bind(view)

        binding.appBar.toolbar.doOnLayout {
            val hasNavigationRail =
                activity?.findViewById<NavigationRailView>(R.id.navigationRail) != null

            setupNavigation(binding.appBar.toolbar, hasNavigationRail)
        }

        applyInsets()
        setupScrollableTab()
        setViewPagerOrientation()
        observe()
        setListeners()
    }

    private fun applyInsets() {
        val scrollableTabPadding = binding.tab.paddingBottom
        binding.tab.prepareWindowInsets { view, windowInsets, _, _, _, bottom ->
            view.updatePadding(bottom = bottom + scrollableTabPadding)
            windowInsets
        }

        val root = binding.countyDetails.constraint
        val rootPadding = root.paddingBottom

        root.prepareWindowInsets { view, windowInsets, _, _, _, bottom ->
            view.updatePadding(bottom = bottom + rootPadding)
            windowInsets
        }

        (binding.viewPager.getChildAt(0) as? RecyclerView)?.let {
            it.clipToPadding = false

            // Force symetrical horizontal insets
            it.prepareWindowInsets(true) { view, windowInsets, left, top, right, _ ->
                val isTabletLayout = resources.getBoolean(R.bool.flat_hexagones)

                view.updatePadding(
                    left = left,
                    right = right,
                    top = if (isTabletLayout) top else view.paddingTop,
                    bottom = if (isTabletLayout) 0 else view.paddingBottom
                )

                if (isTabletLayout) WindowInsetsCompat.CONSUMED else windowInsets
            }
        }

        val toolbar = binding.appBar.toolbarLayout
        toolbar.prepareWindowInsets(true) { view, windowInsets, left, top, right, _ ->
            view.updatePadding(left = left, right = right, top = top)
            windowInsets
        }

        val fabMargin = binding.fab.marginRight
        binding.fab.prepareWindowInsets (true) { view, windowInsets, _, _, right, _ ->
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.updateMargins(right = fabMargin + right)
            windowInsets
        }
    }

    private fun setupScrollableTab() {
        tabAdapter = ScrollableTabAdapter(
            onTabClick = { _, position ->
                binding.viewPager.currentItem = position
            },
            onLongTabClick = { county, position ->
                showCountyDetails(position, county)
            }
        )

        homeViewModel.getNonEmptyCounties().observe(viewLifecycleOwner) {
            binding.emptyState.setVisible(it.isEmpty())

            with(binding) {
                if (tabAdapter?.itemCount != it.size) {
                    tab.adapter = tabAdapter
                    viewPager.adapter =
                        WinesPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle, it)
                }

                tabAdapter?.submitList(it)
                tab.setUpWithViewPager(viewPager)

                (view?.parent as? ViewGroup)?.doOnPreDraw {
                    startPostponedEnterTransition()
                }

//                viewPager.offscreenPageLimit = 1
            }
        }
    }

    private fun setViewPagerOrientation() {
        val flat = resources.getBoolean(R.bool.flat_hexagones)
        val orientation =
            if (flat) ViewPager2.ORIENTATION_VERTICAL else ViewPager2.ORIENTATION_HORIZONTAL

        binding.viewPager.orientation = orientation
    }

    private fun observe() {
        homeViewModel.bottleCount.observe(viewLifecycleOwner) {
            binding.countyDetails.bottles.text =
                resources.getQuantityString(R.plurals.bottles, it, it)
        }

        homeViewModel.bottlePrice.observe(viewLifecycleOwner) {
            binding.countyDetails.price.text = it.join()
        }

        homeViewModel.namingCount.observe(viewLifecycleOwner) {
            binding.countyDetails.namings.setSlices(it, anim = true)
        }

        homeViewModel.vintagesCount.observe(viewLifecycleOwner) {
            binding.countyDetails.vintages.setSlices(it, anim = true)
        }
    }

    private fun setListeners() {
        var currentCounty = 0L

        binding.tab.addOnPageChangeListener {
            currentCounty = tabAdapter?.getItem(it)?.getItemId() ?: 0
        }

        binding.emptyState.setOnActionClickListener {
            navigateToAddWine(currentCounty)
        }

        binding.fab.setOnClickListener {
            navigateToAddWine(currentCounty)
        }

        binding.countyDetailsScrim.setOnClickListener {
            hideCountyDetails()
        }
    }

    private fun showCountyDetails(itemPosition: Int, county: County) {
        with(binding) {
            viewPager.currentItem = itemPosition
            countyDetails.county.text = county.name
            countyDetails.namings.triggerAnimation()
            countyDetails.vintages.triggerAnimation()
        }

        homeViewModel.setObservedCounty(county.id)

        val transform = MaterialContainerTransform().apply {
            duration = resources.getInteger(R.integer.cavity_motion_xlong).toLong()
            startView = binding.tab
            endView = binding.countyDetails.root
            startElevation = resources.getDimension(R.dimen.app_bar_elevation)
            endElevation = binding.countyDetails.root.cardElevation
            scrimColor = Color.TRANSPARENT
            addTarget(binding.countyDetails.root)
        }

        val transformFab = Slide(Gravity.BOTTOM).apply {
            duration = resources.getInteger(R.integer.cavity_motion_medium).toLong()
            addTarget(binding.fab)
        }

        TransitionManager.beginDelayedTransition(binding.constraint, transform)
        TransitionManager.beginDelayedTransition(binding.fab, transformFab)

        with(binding) {
            tab.setVisible(false, invisible = true)
            fab.setVisible(false)
            countyDetails.root.setVisible(true)
            countyDetailsScrim.setVisible(true)
        }
    }

    private fun hideCountyDetails() {
        val transform = MaterialContainerTransform().apply {
            startView = binding.countyDetails.root
            endView = binding.tab
            startElevation = binding.countyDetails.root.cardElevation
            endElevation = resources.getDimension(R.dimen.app_bar_elevation)
            endContainerColor =
                requireContext().themeColor(com.google.android.material.R.attr.colorSurface)
            scrimColor = Color.TRANSPARENT
            addTarget(binding.tab)
        }

        val transformFab = Slide(Gravity.BOTTOM).apply {
            duration = resources.getInteger(R.integer.cavity_motion_long).toLong()
            addTarget(binding.fab)
        }

        TransitionManager.beginDelayedTransition(binding.constraint, transform)
        TransitionManager.beginDelayedTransition(binding.fab, transformFab)

        with(binding) {
            tab.setVisible(true)
            fab.setVisible(true)
            countyDetails.root.setVisible(false, invisible = true)
            countyDetailsScrim.setVisible(false)
        }
    }

    fun navigateToAddWine(countyId: Long) {
        transitionHelper.setSharedAxisTransition(MaterialSharedAxis.Z, navigatingForward = true)

        val action = FragmentHomeDirections.homeToAddWine(countyId = countyId)
        findNavController().navigate(action)
    }

    fun getRecycledViewPool() = recyclePool

    override fun onDestroyView() {
        super.onDestroyView()
        tabAdapter = null
        _binding = null
    }
}
