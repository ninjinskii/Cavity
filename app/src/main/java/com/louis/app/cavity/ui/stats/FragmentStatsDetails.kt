package com.louis.app.cavity.ui.stats

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentStatsDetailsBinding
import com.louis.app.cavity.db.dao.BoundedBottle
import com.louis.app.cavity.ui.search.BottleRecyclerAdapter
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.hideKeyboard
import com.louis.app.cavity.util.prepareWindowInsets

class FragmentStatsDetails : Fragment(R.layout.fragment_stats_details) {

    private val statsDetailsViewModel: StatsDetailsViewModel by viewModels()
    private var _binding: FragmentStatsDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: FragmentStatsDetailsArgs by navArgs()

    private lateinit var transitionHelper: TransitionHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transitionHelper = TransitionHelper(this).apply {
            setSharedAxisTransition(MaterialSharedAxis.X, navigatingForward = false)
            setFadeThrough(navigatingForward = true)
        }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        statsDetailsViewModel.setBottlesIds(args.bottleIds.toList())

        _binding = FragmentStatsDetailsBinding.bind(view)

        binding.title.text = args.title

        applyInsets()
        initRecyclerView()
        setListeners()
    }

    private fun applyInsets() {
        binding.root.prepareWindowInsets { view, windowInsets, left, top, right, bottom ->
            view.updatePadding(left = left, right = right, top = top)
            windowInsets
        }

        binding.bottleList.prepareWindowInsets { view, windowInsets, left, top, right, bottom ->
            view.updatePadding(bottom = bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun initRecyclerView() {
        val bottlesAdapter = BottleRecyclerAdapter(
            onItemClicked = { itemView: View, bottle: BoundedBottle ->
                val transition = getString(R.string.transition_bottle_details, bottle.wine.id)
                val action =
                    FragmentStatsDetailsDirections.statsDetailsToBottleDetails(
                        bottle.wine.id,
                        bottle.bottle.id
                    )
                val extra = FragmentNavigatorExtras(itemView to transition)

                itemView.hideKeyboard()
                transitionHelper.setElevationScale()
                findNavController().navigate(action, extra)
            },
            pickMode = false,
            onPicked = { _, _ -> }
        )

        binding.bottleList.apply {
            adapter = bottlesAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }

        statsDetailsViewModel.bottles.observe(viewLifecycleOwner) {
            bottlesAdapter.submitList(it.toMutableList())
        }
    }

    private fun setListeners() {
        binding.buttonClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
