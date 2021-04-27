package com.louis.app.cavity.ui.stats

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentStatsBinding
import com.louis.app.cavity.db.dao.Year
import com.louis.app.cavity.ui.home.widget.ScrollableTabAdapter
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.setupNavigation

class FragmentStats : Fragment(R.layout.fragment_stats), YearPicker {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val statsViewModel: StatsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatsBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        setupScrollableTab()
        setupViewPager()
        initRecyclerView()
        setListener()
    }

    private fun setupScrollableTab() {
        val tabAdapter = ScrollableTabAdapter<Year>(
            onTabClick = {
            },
            onLongTabClick = {
            }
        )

        statsViewModel.years.observe(viewLifecycleOwner) {
            statsViewModel.setYear(it.first())
            tabAdapter.addAll(it)
        }

        with(binding.years) {
            background = null // Remove MaterialShapeDrawable for elegant disapear animation
            adapter = tabAdapter
            addOnTabChangeListener {
                statsViewModel.setYear(tabAdapter.getItem(it))
            }
        }
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = StatsPagerAdapter(this)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                statsViewModel.notifyPageChanged(position)
            }
        })
    }

    private fun initRecyclerView() {
        //val statAdapter = StatsRecyclerAdapter()

//        binding.recyclerView.apply {
//            adapter = statAdapter
//            layoutManager = GridLayoutManager(requireContext(), 1)
//            setHasFixedSize(true)
//        }

//        statsViewModel.display.observe(viewLifecycleOwner) {
//            statAdapter.submitList(it)
//        }

    }

    private fun setListener() {
//        binding.toggleAnyYear.setOnCheckedChangeListener { _, isChecked ->
//            statsViewModel.setYear(if (isChecked) null else System.currentTimeMillis())
//        }


    }

    override fun setPickYearAllowed(allowed: Boolean) {
        binding.years.setVisible(allowed)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
