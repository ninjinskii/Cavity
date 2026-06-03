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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentWinesBinding
import com.louis.app.cavity.ui.navigation.HomeRoute
import com.louis.app.cavity.ui.navigation.navigator
import com.louis.app.cavity.util.prepareWindowInsets
import com.louis.app.cavity.util.setVisible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FragmentWines : Fragment(R.layout.fragment_wines) {
    private var _binding: FragmentWinesBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels(
        ownerProducer = { fragmentWinesParent },
        factoryProducer = { HomeViewModel.Factory }
    )
    private val countyId by lazy {
        requireArguments().getLong(COUNTY_ID)
    }
    private val fragmentWinesParent: FragmentWinesParent
        get() = (parentFragment as? FragmentWinesParent)
            ?: throw IllegalStateException(
                "Parent fragment should implement FragmentWinesParent. It is $parentFragment"
            )

    private var wineAdapter: WineRecyclerAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWinesBinding.bind(view)

        applyInsets()
        initRecyclerView()
        setupListeners()

        val winesFlow = homeViewModel.getWinesWithBottlesByCounty(countyId)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                winesFlow.collectLatest { wines ->
                    binding.emptyState.setVisible(wines.isEmpty())
                    binding.wineList.post {
                        // Sets back the item animator after shared element transition occurred
                        // When scrolling really quickly, binding can be null when post happens
                        _binding?.wineList?.itemAnimator = DefaultItemAnimator()
                    }

                    wineAdapter?.submitList(wines) {
                        homeViewModel.notifyWineObservingStarted(countyId)
                        homeViewModel.viewState.lastWineChange?.let {
                            binding.wineList.post {
                                scrollToWine(it)
                            }
                        }
                    }
                }
            }
        }
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

        wineAdapter = WineRecyclerAdapter(
            icons,
            isLightTheme,
            onItemClick = { wineWithBottles, itemView ->
                fragmentWinesParent.setPendingSharedElement(itemView)
                homeViewModel.handleWineClick(wineWithBottles, countyId)
            },
            onItemLongClick = { homeViewModel.handleWineLongClick(it, countyId) }
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

        val honeycombLayoutManager = HoneycombLayoutManager(colCount, orientation).apply {
            config.recycleOnDetach = false
        }

        binding.wineList.apply {
            layoutManager = honeycombLayoutManager
            adapter = wineAdapter
            setRecycledViewPool(fragmentWinesParent.getRecycledViewPool())
            setHasFixedSize(true)
            itemAnimator = null // Avoid double element artefact on shared element transition return
        }

        prePopulateRecyclerViewPool()
    }

    private fun scrollToWine(lastWineChange: LastWineChange?) {
        val adapter = wineAdapter ?: return
        val (wineId, countyId) = lastWineChange ?: return

        if (countyId != this.countyId || wineId == -1L) {
            return
        }

        for (i in 0 until adapter.itemCount) {
            val adapterWineId = adapter.getItemId(i)

            if (wineId == adapterWineId) {
                adapter.highlightPosition = i
                binding.wineList.smoothScrollToPosition(i)
                homeViewModel.acknowledgeWineChange()
                return
            }
        }
    }

    private fun prePopulateRecyclerViewPool() {
        val viewPool = binding.wineList.recycledViewPool
        val recyclerView = binding.wineList
        val isPoolEmpty = viewPool.getRecycledViewCount(R.layout.item_wine) == 0

        if (isPoolEmpty) {
            repeat(FragmentHome.VIEW_POOL_SIZE) {
                val viewHolder =
                    recyclerView.adapter?.createViewHolder(recyclerView, R.layout.item_wine)

                recyclerView.recycledViewPool.putRecycledView(viewHolder)
            }
        }
    }

    private fun setupListeners() {
        binding.emptyState.setOnActionClickListener {
            navigator.navigate(HomeRoute.AddWine(countyId), requireParentFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.wineList.adapter = null
        wineAdapter = null
        _binding = null
    }

    companion object {
        private const val COUNTY_ID = "com.louis.app.cavity.ui.home.FragmentWines.COUNTY_ID"

        fun newInstance(countyId: Long): FragmentWines {
            return FragmentWines().apply {
                arguments = bundleOf(COUNTY_ID to countyId)
            }
        }
    }
}
