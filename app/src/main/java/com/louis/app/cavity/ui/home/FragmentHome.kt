package com.louis.app.cavity.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.marginRight
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigationrail.NavigationRailView
import com.google.android.material.transition.MaterialContainerTransform
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHomeBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.ui.addwine.FragmentAddWine
import com.louis.app.cavity.ui.home.widget.ScrollableTabAdapter
import com.louis.app.cavity.ui.navigation.HomeRoute
import com.louis.app.cavity.ui.navigation.navigate
import com.louis.app.cavity.ui.navigation.fragmentResultListener
import com.louis.app.cavity.util.*
import kotlinx.coroutines.launch

class FragmentHome : Fragment(R.layout.fragment_home), FragmentWinesParent {
    companion object {
        const val VIEW_POOL_SIZE = 25
        const val ADD_WINE_RESULT_KEY =
            "com.louis.app.cavity.ui.home.FragmentHome.ADD_WINE_RESULT_KEY"
    }

    private var tabAdapter: ScrollableTabAdapter<County>? = null
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels { HomeViewModel.Factory }
    private val recyclePool by lazy {
        RecyclerView.RecycledViewPool().apply {
            setMaxRecycledViews(R.layout.item_wine, VIEW_POOL_SIZE)
        }
    }

    private var pendingSharedElement: View? = null
        get() = field.also { pendingSharedElement = null }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
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
        postponeEnterTransition()

        _binding = FragmentHomeBinding.bind(view)

        val toolbar = binding.appBar.toolbar
        toolbar.doOnLayout {
            val hasNavigationRail =
                activity?.findViewById<NavigationRailView>(R.id.navigationRail) != null

            setupNavigation(toolbar, hasNavigationRail)
        }

        applyInsets()
        listenToAddWineResult()
        setupScrollableTab()
        setViewPagerOrientation()
        setListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    homeViewModel.event.collect {
                        when (it) {
                            is HomeEvent.Navigation -> navigate(it.appRoute, pendingSharedElement)
                            HomeEvent.WinesObservingStarted -> {
                                // Note that startPostponedEnterTransition() will wait for the next
                                // layout pass to trigger animation. So, actually, calling this when
                                // data is observed is the right moment
                                startPostponedEnterTransition()
                            }
                            is HomeEvent.ScrollToCounty -> {
                                binding.viewPager.currentItem = it.index
                            }
                        }
                    }
                }

                launch {
                    homeViewModel.state.collect {
                        binding.update(it)
                    }
                }
            }
        }
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

            // Force symmetrical horizontal insets
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
        binding.fab.prepareWindowInsets(true) { view, windowInsets, _, _, right, _ ->
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.updateMargins(right = fabMargin + right)
            windowInsets
        }
    }

    private fun listenToAddWineResult() {
        fragmentResultListener<FragmentAddWine.Result>(ADD_WINE_RESULT_KEY) {
            val (wineId, countyId) = it ?: return@fragmentResultListener
            homeViewModel.notifyWineChange(wineId, countyId)
        }
    }

    private fun setupScrollableTab() {
        tabAdapter = ScrollableTabAdapter(
            onTabClick = { _, position ->
                binding.viewPager.currentItem = position
            },
            onLongTabClick = { county, position ->
                showCountyDetails(position, county)
            },
            idToContent = { county ->
                county.id to county
            }
        )
    }

    private fun setViewPagerOrientation() {
        val flat = resources.getBoolean(R.bool.flat_hexagones)
        val orientation =
            if (flat) ViewPager2.ORIENTATION_VERTICAL else ViewPager2.ORIENTATION_HORIZONTAL

        binding.viewPager.orientation = orientation
    }

    private fun setListeners() {
        var currentCounty = 0L

        binding.tab.setOnPageChangeListener {
            currentCounty = tabAdapter?.getItem(it)?.getItemId() ?: 0
        }

        binding.emptyState.setOnActionClickListener {
            navigate(HomeRoute.AddWine(currentCounty), binding.appBar.toolbarLayout)
        }

        binding.fab.setOnClickListener {
            navigate(HomeRoute.AddWine(currentCounty), binding.appBar.toolbarLayout)
        }

        binding.countyDetailsScrim.setOnClickListener {
            hideCountyDetails()
        }
    }

    private fun FragmentHomeBinding.update(state: HomeState) {
        state.observedCounty?.let {
            with(countyDetails) {
                price.text = it.bottlePrice.join()
                namings.setSlices(it.namingCount, anim = true)
                vintages.setSlices(it.vintagesCount, anim = true)
                bottles.text =
                    resources.getQuantityString(R.plurals.bottles, it.bottleCount, it.bottleCount)
            }
        }

        val counties = state.nonEmptyCounties
        emptyState.setVisible(counties.isEmpty())
        if (tabAdapter?.itemCount != counties.size) {
            tab.adapter = tabAdapter
            viewPager.adapter =
                WinesPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle, counties)
        }
        tabAdapter?.submitList(counties)
        tab.setupWithViewPager(viewPager)

        updateToolbarTitle(state.toolbarTitle ?: getString(R.string.app_name))

        val clearText = getString(R.string.all)
        val locations = listOf(clearText) + state.storageLocations
        if (state.showStorageDialog) {
            appBar.toolbar.setOnTitleClickListener { showStorageLocationDialog(locations) }
        }
    }

    private fun showStorageLocationDialog(items: List<String>) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.storage_location)
            .setItems(items.toTypedArray()) { _, selectedPosition ->
                val countyId = binding.viewPager.adapter?.getItemId(binding.viewPager.currentItem)
                val selection = items[selectedPosition].takeUnless { selectedPosition == 0 }
                homeViewModel.setStorageLocation(selection, countyId)
            }
            .show()
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

    private fun setCurrentCounty(countyId: Long) {
        val position = (_binding?.viewPager?.adapter as? WinesPagerAdapter)?.getPosition(countyId)

        if (position != -1) {
            _binding?.viewPager?.currentItem = position ?: return
        }
    }

    private fun updateToolbarTitle(title: String) {
        findNavController().currentDestination?.label = title
        activity?.setTitle(title)
        val toolbar = binding.appBar.toolbar
        toolbar.post { toolbar.title = title }
    }

    override fun getRecycledViewPool() = recyclePool

    override fun setPendingSharedElement(sharedElement: View) {
        this.pendingSharedElement = sharedElement
    }

    override fun onResume() {
        super.onResume()
        binding.viewPager.post {
            checkScrollRequest()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabAdapter = null
        _binding = null
    }
}
