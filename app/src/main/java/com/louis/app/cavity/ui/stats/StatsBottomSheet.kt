package com.louis.app.cavity.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.louis.app.cavity.databinding.BottomSheetStatsBinding
import com.louis.app.cavity.util.join

class StatsBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetStatsBinding? = null
    private val binding get() = _binding!!
    private val statsViewModel: StatsViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetStatsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statsViewModel.getTotalPriceByCurrency().observe(viewLifecycleOwner) {
            binding.price.text = it.join()
        }

        statsViewModel.getTotalConsumed().observe(viewLifecycleOwner) {
            binding.consumed.text = it.toString()
        }

        statsViewModel.getTotalStock().observe(viewLifecycleOwner) {
            binding.stock.text = it.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

