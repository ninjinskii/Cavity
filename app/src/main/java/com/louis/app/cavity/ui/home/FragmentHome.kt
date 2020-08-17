package com.louis.app.cavity.ui.home

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHomeBinding
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.ui.CountyScrollableTab
import com.louis.app.cavity.util.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentHome : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)

        setupRecyclerView()
        setupScrollableTab()
    }

    private fun setupRecyclerView() {
        val wineAdapter = WineRecyclerViewAdapter(object : OnVintageClickListener {
            override fun onVintageClick(wine: Wine) {
                TODO("Not yet implemented")
            }
        }, activity as Context)
        wineAdapter.setHasStableIds(true)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            setItemViewCacheSize(10)
            adapter = wineAdapter
//            addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    // Show button no matter what if RV can't be scrolled
//                    if (
//                        !recyclerView.canScrollVertically(1) &&
//                        !recyclerView.canScrollVertically(-1)
//                    ) binding.buttonAdd.extend()
//                    else if (dy > 0 && binding.buttonAdd.isExtended) binding.buttonAdd.shrink()
//                    else if (dy < 0 && !binding.buttonAdd.isExtended) binding.buttonAdd.extend()
//                }
//            })
        }

        homeViewModel.getAllWines().observe(viewLifecycleOwner) {
            wineAdapter.submitList(it)
        }
    }

    private fun setupScrollableTab() {
        binding.tab.addTabs(
            listOf(
                "Alsace", "Beaujolais", "Bourgogne", "Bordeaux", "Italie", "Suisse",
                "Langudoc-Roussillon", "Jura"
            )
        )
    }
}