package com.louis.app.cavity.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentWinesBinding
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.setVisible

class FragmentWines : Fragment(R.layout.fragment_wines) {
    private var _binding: FragmentWinesBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWinesBinding.bind(view)

        initRecyclerView()
        setListeners()
    }

    private fun initRecyclerView() {
        val icons = ContextCompat.getDrawable(requireContext(), R.drawable.ic_bio)!! to
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_glass)!!.also {
                    it.setTint(Color.WHITE)
                }

        val wineAdapter = WineRecyclerAdapter(
            icons,
            TransitionHelper(requireParentFragment())
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

        binding.wineList.apply {
            layoutManager = HoneycombLayoutManager(colCount, orientation)
            setRecycledViewPool((parentFragment as FragmentHome).getRecycledViewPool())
            setHasFixedSize(true)
            adapter = wineAdapter
        }

        prePopulateRecyclerViewPool()

        val countyId = arguments?.getLong(COUNTY_ID)

        homeViewModel.getWinesWithBottlesByCounty(countyId ?: 0).observe(viewLifecycleOwner) {
            binding.emptyState.setVisible(it.isEmpty())
            wineAdapter.submitList(it)
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

    private fun recyleViewsOnExit() {
//        val exitTransition = parentFragment?.exitTransition as Transition?
//        exitTransition?.addListener(
//            object : Transition.TransitionListener {
//                override fun onTransitionEnd(transition: Transition) {
//                    exitTransition.removeListener(this)
//
//                    val recyclerView = binding.wineList
//                    recyclerView.layoutManager?.apply {
//                        removeAndRecycleAllViews(recyclerView.Recycler())
//                        recyclerView.Recycler().clear()
//                    }
//                }
//
//                override fun onTransitionStart(transition: Transition) = Unit
//                override fun onTransitionCancel(transition: Transition) = Unit
//                override fun onTransitionPause(transition: Transition) = Unit
//                override fun onTransitionResume(transition: Transition) = Unit
//            })
    }

    override fun onPause() {
//        honeycombMangaer.delayRecycling = true
        super.onPause()
    }

    override fun onResume() {
//        honeycombMangaer.delayRecycling = false
        super.onResume()
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
