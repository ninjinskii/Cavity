package com.louis.app.cavity.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.louis.app.cavity.databinding.BottomSheetStatsBinding

class StatsBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetStatsBinding? = null
    private val binding get() = _binding!!
    private val cellarStatsViewModel: CellarStatsViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { CellarStatsViewModel.Factory }
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

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cellarStatsViewModel.state.collect {
                    binding.update(it)
                }
            }
        }
    }

    private fun BottomSheetStatsBinding.update(state: CellarStatsUiState) {
        stock.text = state.remainingBottles.toString()
        consumed.text = state.totalConsumedBottles.toString()
        price.text = state.totalPriceByCurrency
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

