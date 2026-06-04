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
import com.louis.app.cavity.domain.stats.Stat
import com.louis.app.cavity.domain.stats.StatSlot
import com.louis.app.cavity.domain.stats.fromButtonId
import com.louis.app.cavity.ui.stats.widget.PieView
import com.louis.app.cavity.util.setVisible
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentPie : Fragment(R.layout.fragment_pie) {
    private var _binding: FragmentPieBinding? = null
    private val binding get() = _binding!!
    private val statsViewModel: StatsViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { StatsViewModel.Factory }
    )

    // Support android api < 33
    @Suppress("DEPRECATION")
    private val statSlot by lazy {
        requireArguments().getSerializable(STAT_SLOT)!! as StatSlot
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPieBinding.bind(view)
        binding.title.text = requireContext().getString(requireArguments().getInt(TITLE_RES))

        setListeners()
        observe()
    }

    private fun setListeners() {
        binding.buttonStock.isChecked = true

        binding.buttonGroupSwitchStat.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) {
                return@addOnButtonCheckedListener
            }

//            statsViewModel.setShouldShowYearPicker(checkedId != R.id.buttonStock)
            statsViewModel.setStatType(statSlot, fromButtonId(checkedId))
        }

        binding.toggleGivenBottle.apply {
            thumbDrawable = ResourcesCompat.getDrawable(
                resources,
                R.drawable.switch_thumb,
                requireContext().theme
            )

            setOnCheckedChangeListener { _, isChecked ->
                statsViewModel.setIncludeGifts(statSlot, isChecked)
            }
        }

        binding.givenBottle.setOnClickListener {
            binding.toggleGivenBottle.toggle()
        }
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    statsViewModel.state.collect { state ->
                        /*if (state.currentStatSlot == statSlot) {
                            maybeShowYearPicker()
                        }*/

                        with(binding) {
                            comparisonPieView.setVisible(state.comparison)
                            comparisonText.setVisible(state.comparison)
                            buttonGroupSwitchStat.setVisible(!state.comparison)
                        }

                        binding.toggleGivenBottle.setVisible(state.showYearSpanOptions)
                        binding.givenBottle.setVisible(state.showYearSpanOptions)
                    }
                }
                launch {
                    statsViewModel.getResults(statSlot).collect { stats ->
                        updatePieData(binding.pieView, stats)

                        lifecycleScope.launch(Default) {
                            val total = stats.sumOf { it.count }

                            withContext(Main) {
                                binding.total.text = resources.getString(R.string.total, total)
                            }
                        }
                    }
                }
                launch {
                    statsViewModel.getComparisons(statSlot).collect {
                        updatePieData(binding.comparisonPieView, it)
                    }
                }
                launch {
                    statsViewModel.comparisonText.collect {
                        binding.comparisonText.text = it
                    }
                }
            }
        }
    }

    private fun updatePieData(pieView: PieView, stats: List<Stat>) {
        pieView.setPieSlices(stats, anim = true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TITLE_RES = "com.louis.app.cavity.ui.home.FragmentPie.TITLE_RES"
        private const val STAT_SLOT = "com.louis.app.cavity.ui.home.FragmentPie.STAT_SLOT"

        // Used by StatsPagerAdapter
        fun newInstance(statSlot: StatSlot, @StringRes titleRes: Int): FragmentPie {
            return FragmentPie().apply {
                arguments = bundleOf(
                    STAT_SLOT to statSlot,
                    TITLE_RES to titleRes
                )
            }
        }
    }
}

