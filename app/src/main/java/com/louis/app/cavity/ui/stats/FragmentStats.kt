package com.louis.app.cavity.ui.stats

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
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
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatsBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        setupScrollableTab()
        setupViewPager()
        initRecyclerViews()
        observe()
        maybeAnimateViewPager()
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
            tabAdapter.addAll(it)
        }

        with(binding.years) {
            background = null // Remove background for elegant disapear animation
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

    private fun initRecyclerViews() {
        val statsAdapter = StatsRecyclerAdapter()

        binding.statDetailsList.apply {
            adapter = statsAdapter
            layoutManager = LinearLayoutManager(context)
        }

        statsViewModel.details.observe(viewLifecycleOwner) {
            statsAdapter.submitList(it)
        }

//        statsViewModel.comparison.observe(viewLifecycleOwner) {
//            if (!it) statsAdapter.comparisonDetails = emptyList()
//
//            lifecycleScope.launch(Main) {
//                delay(300)
//
//                with(binding.recyclerView) {
//                    val animator = itemAnimator
//                    itemAnimator = null
//                    statsAdapter.notifyItemRangeChanged(0, statsAdapter.itemCount - 1)
//
//                    lifecycleScope.launch(Main) {
//                        delay(200)
//                        itemAnimator = animator
//                    }
//                }
//            }
//        }
    }

    private fun observe() {
        statsViewModel.showYearPicker.observe(viewLifecycleOwner) {
            binding.years.setVisible(it)
        }

    }

    private fun maybeAnimateViewPager() {
        // This cause a memory leak
//        binding.viewPager.doOnLayout {
//            it as ViewPager2
//            it.setCurrentItem(statsPagerAdapter.itemCount - 1, false)
//            it.postDelayed(200L) {
//                it.setCurrentItem(0, true)
//            }
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
