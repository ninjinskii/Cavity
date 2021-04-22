package com.louis.app.cavity.ui.stats

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentStatsBinding
import com.louis.app.cavity.util.setupNavigation

class FragmentStats : Fragment(R.layout.fragment_stats) {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val statsViewModel: StatsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatsBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val statAdapter = StatsRecyclerAdapter()

        binding.recyclerView.apply {
            adapter = statAdapter
            // TODO: adapt to screen
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
        }

        statsViewModel.consumedBottlesByVintage.observe(viewLifecycleOwner) {
            val resolved = it.map { slice -> slice.resolve(requireContext()) }
            statAdapter.submitList(listOf(StatsUiModel.Pie(resolved)))
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
