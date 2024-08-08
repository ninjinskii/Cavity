package com.louis.app.cavity.ui.stats

import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
import android.view.animation.PathInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentStatsBinding
import com.louis.app.cavity.db.dao.Year
import com.louis.app.cavity.ui.home.widget.ScrollableTabAdapter
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.setupNavigation

class FragmentStats : Fragment(R.layout.fragment_stats) {
    private lateinit var statsPagerAdapter: StatsPagerAdapter
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val statsViewModel: StatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TransitionHelper(this).apply {
            setFadeThrough(navigatingForward = false)
            setSharedAxisTransition(MaterialSharedAxis.X, navigatingForward = true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatsBinding.bind(view)

        setupNavigation(binding.toolbar)

        setupScrollableTab()
        setupViewPager()
        setupToolbar()
        initRecyclerViews()
        observe()
        hintViewPagerSlide()
    }

    private fun setupScrollableTab() {
        val tabAdapter = ScrollableTabAdapter<Year>(
            onTabClick = { view, _ ->
                binding.years.moveToView(view)
            },
            onLongTabClick = { year, _ ->
                statsViewModel.setComparisonYear(year)
            }
        )

        statsViewModel.years.observe(viewLifecycleOwner) {
            tabAdapter.submitList(it)
        }

        with(binding.years) {
            background = null // Remove background for elegant disappear animation
            adapter = tabAdapter
            addOnTabChangeListener {
                statsViewModel.setYear(tabAdapter.getItem(it))
            }
        }
    }

    private fun setupViewPager() {
        statsPagerAdapter = StatsPagerAdapter(this)

        binding.viewPager.adapter = statsPagerAdapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                statsViewModel.notifyPageChanged(position)
            }
        })
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

    private fun initRecyclerViews() {
        val statsAdapter = StatsRecyclerAdapter(
            onItemClicked = { itemBottlesIds, label ->
                val statType = getString(statsViewModel.getStatTypeLabel())
                val action = FragmentStatsDirections.statsToStatsDetails(
                    "$statType - $label",
                    itemBottlesIds.toLongArray()
                )

                findNavController().navigate(action)
            }
        )

        binding.statDetailsList.apply {
            adapter = statsAdapter
            layoutManager = LinearLayoutManager(context)
        }

        statsViewModel.details.observe(viewLifecycleOwner) {
            statsAdapter.submitList(it)
        }
    }

    private fun observe() {
        statsViewModel.showYearPicker.observe(viewLifecycleOwner) {
            binding.years.setVisible(it)
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

    override fun onDestroyView() {
        super.onDestroyView()
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
