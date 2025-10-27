package com.louis.app.cavity.ui.home

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentWinesBinding
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.prepareWindowInsets
import com.louis.app.cavity.util.setVisible

class FragmentWines : Fragment(R.layout.fragment_wines) {
    private var _binding: FragmentWinesBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()

    private var honeycombLayoutManager: HoneycombLayoutManager? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWinesBinding.bind(view)

        applyInsets()
        initRecyclerView()
        setListeners()
    }

    private fun applyInsets() {
        val wineListPadding = binding.wineList.paddingTop
        binding.wineList.prepareWindowInsets { view, _, _, top, _, _ ->
            view.updatePadding(top = top + wineListPadding)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun initRecyclerView() {
        val icons = ContextCompat.getDrawable(requireContext(), R.drawable.ic_bio)!! to
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_glass)!!.also {
                    it.setTint(Color.WHITE)
                }

        val isLightTheme = when (
            requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        ) {
            Configuration.UI_MODE_NIGHT_YES -> false
            Configuration.UI_MODE_NIGHT_NO -> true
            else -> true
        }

        val wineAdapter = WineRecyclerAdapter(
            icons,
            TransitionHelper(requireParentFragment()),
            isLightTheme
        ).apply {
            setHasStableIds(true)
        }

        val colCount = resources.getInteger(R.integer.honeycomb_cols)
        val flat = resources.getBoolean(R.bool.flat_hexagones)
        val orientation =
            if (flat) {
                HoneycombLayoutManager.Orientation.HORIZONTAL
            } else {
                HoneycombLayoutManager.Orientation.VERTICAL
            }

        honeycombLayoutManager = HoneycombLayoutManager(colCount, orientation).apply {
            config.jumpScrollThreshold = 10
        }

        binding.wineList.apply {
            layoutManager = honeycombLayoutManager
            setRecycledViewPool((parentFragment as FragmentHome).getRecycledViewPool())
            setHasFixedSize(true)
            adapter = wineAdapter
        }

        prePopulateRecyclerViewPool()

        val countyId = arguments?.getLong(COUNTY_ID)

        homeViewModel.getWinesWithBottlesByCounty(countyId ?: 0).observe(viewLifecycleOwner) {
            binding.emptyState.setVisible(it.isEmpty())
            wineAdapter.submitList(it) {
                if (homeViewModel.lastAddedWine.value != null) {
                    scrollToWine(wineAdapter)
                }
            }
        }
    }

    private fun scrollToWine(adapter: WineRecyclerAdapter) {
        val countyId = arguments?.getLong(COUNTY_ID)
        val (wine, county) = homeViewModel.lastAddedWine.value!!.peekContent()

        if (county.id != countyId) {
            return
        }

        homeViewModel.lastAddedWine.value?.getContentIfNotHandled()?.let {
            for (i in 0 until adapter.itemCount) {
                val wineId = adapter.getItemId(i)

                if (wineId == wine.id) {
                    adapter.highlightPosition = i
                    binding.wineList.smoothScrollToPosition(i)
                }
            }
        }
    }

    private fun prePopulateRecyclerViewPool() {
        val viewPool = binding.wineList.recycledViewPool
        val recylerView = binding.wineList
        val isPoolEmpty = viewPool.getRecycledViewCount(R.layout.item_wine) == 0

        if (isPoolEmpty) {
            repeat(FragmentHome.VIEW_POOL_SIZE) {
                val viewHolder =
                    recylerView.adapter?.createViewHolder(recylerView, R.layout.item_wine)

                recylerView.recycledViewPool.putRecycledView(viewHolder)
            }
        }
    }

    private fun setListeners() {
        binding.emptyState.setOnActionClickListener {
            (parentFragment as? FragmentHome)?.navigateToAddWine(
                arguments?.getLong(COUNTY_ID) ?: return@setOnActionClickListener
            )
        }
    }

    override fun onPause() {
        honeycombLayoutManager?.config?.skipNextRecycleOnDetach = true
        super.onPause()
    }

    override fun onResume() {
        honeycombLayoutManager?.config?.skipNextRecycleOnDetach = false
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        honeycombLayoutManager = null
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
