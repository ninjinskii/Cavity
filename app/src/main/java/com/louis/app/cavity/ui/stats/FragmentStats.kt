package com.louis.app.cavity.ui.stats

import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
import android.view.animation.PathInterpolator
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import androidx.viewpager2.widget.ViewPager2
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentStatsBinding
import com.louis.app.cavity.domain.stats.StatsYearTimeSpan
import com.louis.app.cavity.ui.home.widget.ScrollableTabAdapter
import com.louis.app.cavity.ui.navigation.StatRoute
import com.louis.app.cavity.ui.navigation.navigate
import com.louis.app.cavity.util.prepareWindowInsets
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.setupNavigation
import kotlinx.coroutines.flow.collectLatest

class FragmentStats : Fragment(R.layout.fragment_stats) {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val statsViewModel: StatsViewModel by viewModels { StatsViewModel.Factory }
    private lateinit var tabAdapter: ScrollableTabAdapter<StatsYearTimeSpan>
    private lateinit var statsAdapter: StatsRecyclerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatsBinding.bind(view)

        setupNavigation(binding.toolbar)

        applyInsets()
        setupScrollableTab()
        setupViewPager()
        setupToolbar()
        initRecyclerView()
        hintViewPagerSlide()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    statsViewModel.screenState.collectLatest { state ->
                        binding.update(state)
                    }
                }

                launch {
                    statsViewModel.pieResultsFlow.collectLatest {
                        statsAdapter.submitList(it)
                    }
                }
            }
        }
    }

    private fun applyInsets() {
        binding.appBar.prepareWindowInsets { view, _, left, top, right, _ ->
            view.updatePadding(left = left, right = right, top = top)
            WindowInsetsCompat.CONSUMED
        }

        val isLandLayout = binding.patch != null

        binding.statDetailsList.prepareWindowInsets { view, _, _, _, _, bottom ->
            val padding = if (binding.years.isVisible && isLandLayout) 0 else bottom
            view.updatePadding(bottom = padding)

            WindowInsetsCompat.CONSUMED
        }

        if (isLandLayout) {
            binding.years.prepareWindowInsets { view, _, _, _, _, bottom ->
                view.updatePadding(bottom = bottom)
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    private fun setupScrollableTab() {
        tabAdapter = ScrollableTabAdapter(
            onTabClick = { view, _ ->
                binding.years.moveToView(view)
            },
            onLongTabClick = { year, _ ->
                statsViewModel.setComparisonYear(year)
            },
            itemId = { timeSpan ->
                timeSpan.year
            },
            displayText = { timeSpan ->
                if (timeSpan.year != 0)
                    timeSpan.year.toString()
                else
                    requireContext().getString(R.string.combined)
            }
        )

        with(binding.years) {
            background = null // Remove background for elegant disappear animation
            adapter = tabAdapter
            setOnTabChangeListener {
                statsViewModel.setYear(tabAdapter.getItem(it))
            }
        }
    }

    private fun setupViewPager() {
        val statsPagerAdapter = StatsPagerAdapter(this, viewLifecycleOwner)

        binding.viewPager.apply {
            adapter = statsPagerAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val groupBy = statsPagerAdapter.getSlotAt(position)
                    statsViewModel.setSelectedGroupBy(groupBy)
                }
            })
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.misc) {
                StatsBottomSheet().show(
                    childFragmentManager,
                    getString(R.string.tag_modal_sheet_id)
                )
                return@setOnMenuItemClickListener true
            }

            false
        }
    }

    private fun initRecyclerView() {
        statsAdapter = StatsRecyclerAdapter(
            onItemClicked = { itemBottlesIds, label ->
                val statType = getString(statsViewModel.getCurrentStatTypeLabel())
                val title = "$statType - $label"
                navigate(StatRoute.StatDetails(title, itemBottlesIds))
            }
        )

        binding.statDetailsList.apply {
            adapter = statsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun hintViewPagerSlide() {
        binding.viewPager.animate()
            .setDuration(2000)
            .setInterpolator(SoftenBounceInterpolator())
            .translationX(0f)
            .translationY(0f)
            .start()
    }

    private fun updateStatDetailsListInset() {
        binding.statDetailsList.requestApplyInsets()
    }

    private fun FragmentStatsBinding.update(state: StatsScreenUiState) {
        tabAdapter.submitList(state.years)
        years.apply {
            setVisible(state.showYearSpanOptions, invisible = true)
            post { binding.years.requestLayout() }
        }
        updateStatDetailsListInset()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        with(binding) {
            viewPager.adapter = null
            years.adapter = null
            statDetailsList.adapter = null
        }

        _binding = null
    }

    class SoftenBounceInterpolator : Interpolator {
        private val slowOut = PathInterpolator(0.46f, 0.49f, 0.45f, 1.01f)
        private val bounceInterpolator = BounceInterpolator()

        override fun getInterpolation(input: Float): Float {
            val bounce = bounceInterpolator.getInterpolation(input)
            return slowOut.getInterpolation(bounce)
        }
    }
}


