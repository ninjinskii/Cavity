package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentWinesBinding

class FragmentWines : Fragment(R.layout.fragment_wines) {
    private var _binding: FragmentWinesBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWinesBinding.bind(view)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val wineAdapter = WineRecyclerAdapter()

        binding.recyclerView.apply {
            layoutManager = HoneycombLayoutManager(
                colCount = 2,
                HoneycombLayoutManager.Orientation.VERTICAL
            )

            setRecycledViewPool((parentFragment as FragmentHome).getRecycledViewPool())
            setHasFixedSize(true)
            adapter = wineAdapter
        }

        val countyId = arguments?.getLong(COUNTY_ID)

        homeViewModel.getWinesWithBottlesByCounty(countyId ?: 0).observe(viewLifecycleOwner) {
            wineAdapter.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        _binding = null
    }

    companion object {
        private const val COUNTY_ID = "com.louis.app.cavity.ui.home.FragmentWines.COUNTY_ID"

        // Used by WinesPagerAdapter
        fun newInstance(countyId: Long): FragmentWines {
            return FragmentWines().apply {
                arguments = bundleOf(COUNTY_ID to countyId)
            }
        }
    }
}
