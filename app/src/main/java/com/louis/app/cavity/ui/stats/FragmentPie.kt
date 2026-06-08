package com.louis.app.cavity.ui.stats

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentPieBinding
import com.louis.app.cavity.domain.stats.InventoryStatFilter
import com.louis.app.cavity.domain.stats.StatGroupBy
import com.louis.app.cavity.util.setVisible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FragmentPie : Fragment(R.layout.fragment_pie) {
    private var _binding: FragmentPieBinding? = null
    private val binding get() = _binding!!
    private val statsViewModel: StatsViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { StatsViewModel.Factory }
    )

    // Support android api < 33
    @Suppress("DEPRECATION")
    private val statGroupBy by lazy {
        requireArguments().getSerializable(STAT_SLOT)!! as StatGroupBy
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPieBinding.bind(view)
        binding.title.text = requireContext().getString(requireArguments().getInt(TITLE_RES))

        setListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    statsViewModel.uiStateFlow.collectLatest { state ->
                        binding.update(state)
                    }
                }

                launch {
                    statsViewModel.pieResults(statGroupBy).collectLatest {
                        with(binding) {
                            pieView.setPieSlices(it.first, anim = true)
                            toggleGivenBottle.setVisible(it.second)
                            givenBottle.setVisible(it.second)
                        }
                    }
                }

                launch {
                    statsViewModel.pieComparisons(statGroupBy).collectLatest {
                        binding.comparisonPieView.setPieSlices(it, anim = true)
                    }
                }
            }
        }
    }

    private fun setListeners() {
        binding.buttonStock.isChecked = true

        binding.buttonGroupSwitchStat.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) {
                return@addOnButtonCheckedListener
            }

            val statFilter = InventoryStatFilter.fromButtonId(checkedId)
            statsViewModel.setStatFilter(statGroupBy, statFilter)
        }

        binding.toggleGivenBottle.apply {
            thumbDrawable = ResourcesCompat.getDrawable(
                resources,
                R.drawable.switch_thumb,
                requireContext().theme
            )

            setOnCheckedChangeListener { _, isChecked ->
                statsViewModel.setIncludeGifts(statGroupBy, isChecked)
            }
        }

        binding.givenBottle.setOnClickListener {
            binding.toggleGivenBottle.toggle()
        }
    }

    private fun FragmentPieBinding.update(state: StatsUiState) {
        total.text = resources.getString(R.string.total, -1) // TODO: make viewmodel compute total
        comparisonText.text = state.comparisonText
        comparisonPieView.setVisible(state.comparison)
        comparisonText.setVisible(state.comparison)
        buttonGroupSwitchStat.setVisible(!state.comparison)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TITLE_RES = "com.louis.app.cavity.ui.home.FragmentPie.TITLE_RES"
        private const val STAT_SLOT = "com.louis.app.cavity.ui.home.FragmentPie.STAT_SLOT"

        // Used by StatsPagerAdapter
        fun newInstance(statGroupBy: StatGroupBy, @StringRes titleRes: Int): FragmentPie {
            return FragmentPie().apply {
                arguments = bundleOf(
                    STAT_SLOT to statGroupBy,
                    TITLE_RES to titleRes
                )
            }
        }
    }
}

