package com.louis.app.cavity.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.transition.Hold
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentWinesBinding
import com.louis.app.cavity.util.TransitionHelper

class FragmentWines : Fragment(R.layout.fragment_wines) {
    private var _binding: FragmentWinesBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandatory to keep hexagonal views on screen while navigating to another destination
        exitTransition = Hold().apply {
            duration = resources.getInteger(R.integer.cavity_motion_long).toLong()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWinesBinding.bind(view)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val transitionHelper = TransitionHelper(requireParentFragment())
        val icons = ContextCompat.getDrawable(requireContext(), R.drawable.ic_bio)!! to
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_glass)!!.also {
                it.setTint(Color.WHITE)
            }

        val wineAdapter = WineRecyclerAdapter(transitionHelper, icons)

        binding.wineList.apply {
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
        binding.wineList.adapter = null
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
