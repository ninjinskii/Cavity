package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHomeBinding
import com.louis.app.cavity.model.Wine

class FragmentHome : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupScrollableTab()
        setListeners()
        setHasOptionsMenu(true)

        return binding.root
    }

    private fun setupRecyclerView() {
        val colors = context?.let {
            listOf(
                it.getColor(R.color.wine_white),
                it.getColor(R.color.wine_red),
                it.getColor(R.color.wine_sweet),
                it.getColor(R.color.wine_rose),
                it.getColor(R.color.colorAccent)
            )
        }

        val wineAdapter = WineRecyclerAdapter(object : OnVintageClickListener {
            override fun onVintageClick(wine: Wine) {
                TODO()
            }
        }, colors ?: emptyList())

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
                    ) binding.fab.show()
                    else if (dy > 0 && binding.fab.isShown) binding.fab.hide()
                    else if (dy < 0 && !binding.fab.isShown) binding.fab.show()
                }
            })
        }

        homeViewModel.getWinesWithBottles().observe(viewLifecycleOwner) {
            wineAdapter.submitList(it)
        }
    }

    private fun setupScrollableTab() {
        homeViewModel.getAllCounties().observe(viewLifecycleOwner) {
            binding.tab.addTabs(it.map { county -> county.name })
        }
    }

    private fun setListeners() {
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.show_addWine)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.switchView -> TODO("Change item view type in RecyclerView")
        }

        return super.onOptionsItemSelected(item)
    }
}
