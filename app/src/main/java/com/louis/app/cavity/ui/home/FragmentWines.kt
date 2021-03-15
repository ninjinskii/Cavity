package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentWinesBinding
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.util.toBoolean

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
        val wineAdapter = WineRecyclerAdapter(
            requireContext(),
            onVintageClickListener = { wineId: Long, bottle: Bottle ->
                val action = FragmentHomeDirections.homeToBottleDetails(wineId, bottle.id)
                findNavController().navigate(action)
            },
            onShowOptionsListener = { wine ->
                activity?.supportFragmentManager?.let {
                    val action = FragmentHomeDirections.homeToWineOptions(
                        wine.id,
                        wine.countyId,
                        wine.name,
                        wine.naming,
                        wine.isOrganic.toBoolean(),
                        wine.color
                    )
                    findNavController().navigate(action)
                }
            }
        )

        binding.recyclerView.apply {
            layoutManager = HoneycombLayoutManager(
                requireContext(),
                colCount = 2,
                HoneycombLayoutManager.HORIZONTAL
            )
            // setHasFixedSize(true)
            adapter = wineAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    // Show components no matter what if RV can't be scrolled
                    if (
                        !recyclerView.canScrollVertically(1) &&
                        !recyclerView.canScrollVertically(-1)
                    ) {
                        homeViewModel.isScrollingToTop.postValue(true)
                    } else {
                        homeViewModel.isScrollingToTop.postValue(dy < 0)
                    }
                }
            })
        }

        val countyId = arguments?.getLong(COUNTY_ID)

        homeViewModel.getWinesWithBottlesByCounty(countyId ?: 0).observe(viewLifecycleOwner) {
            wineAdapter.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
