package com.louis.app.cavity.ui.stats

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentSheetListBinding
import com.louis.app.cavity.ui.navigation.StatDetailsRoute
import com.louis.app.cavity.ui.navigationnext.navigate
import com.louis.app.cavity.ui.navigationnext.navigateUp
import com.louis.app.cavity.ui.search.BottleRecyclerAdapter
import com.louis.app.cavity.util.prepareWindowInsets

class FragmentStatsDetails : Fragment(R.layout.fragment_sheet_list) {
    private val statsDetailsViewModel: StatsDetailsViewModel by viewModels()
    private var _binding: FragmentSheetListBinding? = null
    private val binding get() = _binding!!
    private val args: FragmentStatsDetailsArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        statsDetailsViewModel.setBottlesIds(args.bottleIds.toList())

        _binding = FragmentSheetListBinding.bind(view)

        binding.title.text = args.title

        applyInsets()
        initRecyclerView()
        setListeners()
    }

    private fun applyInsets() {
        binding.root.prepareWindowInsets { view, windowInsets, left, top, right, _ ->
            view.updatePadding(left = left, right = right, top = top)
            windowInsets
        }

        binding.bottleList.prepareWindowInsets { view, _, _, _, _, bottom ->
            view.updatePadding(bottom = bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun initRecyclerView() {
        val bottlesAdapter = BottleRecyclerAdapter(
            onItemClicked = { itemView: View, boundedBottle ->
                val (bottle, wine) = boundedBottle
                navigate(StatDetailsRoute.BottleDetails(wine.id, bottle.id), itemView)
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
            navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
