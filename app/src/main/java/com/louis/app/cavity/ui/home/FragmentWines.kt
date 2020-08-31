package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentWinesBinding
import com.louis.app.cavity.model.Wine

class FragmentWines : Fragment(R.layout.fragment_wines) {
    private lateinit var binding: FragmentWinesBinding
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWinesBinding.bind(view)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val colors = context?.let {
            listOf(
                it.getColor(R.color.wine_white),
                it.getColor(R.color.wine_red),
                it.getColor(R.color.wine_sweet),
                it.getColor(R.color.wine_rose),
                it.getColor(R.color.colorAccent)
            )
        }

        val listener = object : OnVintageClickListener {
            override fun onVintageClick(wine: Wine) {
                TODO()
            }
        }

        val listenerLongClick = object : OnLongClickListener {
            override fun onLongClick(wine: Wine) {
                homeViewModel.editWine = wine
                findNavController().navigate(R.id.homeToAddWine)
            }
        }

        val wineAdapter = WineRecyclerAdapter(listener, listenerLongClick, colors ?: emptyList())

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = wineAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    // Show components no matter what if RV can't be scrolled
                    if (
                        !recyclerView.canScrollVertically(1) &&
                        !recyclerView.canScrollVertically(-1)
                    ) homeViewModel.isScrollingToTop.postValue(true)
                    else if (dy > 0) homeViewModel.isScrollingToTop.postValue(false)
                    else if (dy < 0) homeViewModel.isScrollingToTop.postValue(true)
                }
            })
        }

        val countyId = arguments?.getLong(COUNTY_ID)
        homeViewModel.getWinesWithBottlesByCounty(countyId ?: 0).observe(viewLifecycleOwner) {
            wineAdapter.submitList(it)
        }
    }

    companion object {
        private const val COUNTY_ID = "com.louis.app.cavity.ui.home.FragmentWines.COUNTY_ID"

        fun newInstance(countyId: Long): FragmentWines {
            return FragmentWines().apply {
                arguments = Bundle().apply { putLong(COUNTY_ID, countyId) }
            }
        }
    }
}
