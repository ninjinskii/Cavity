package com.louis.app.cavity.ui.stats

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentPieBinding
import com.louis.app.cavity.ui.stats.widget.PieView
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentPie : Fragment(R.layout.fragment_pie) {
    private lateinit var yearPicker: YearPicker
    private var _binding: FragmentPieBinding? = null
    private val binding get() = _binding!!
    private val statsViewModel: StatsViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPieBinding.bind(view)

        yearPicker = parentFragment as YearPicker

        setListeners()
        observe()
    }

    private fun setListeners() {
        binding.buttonGroupSwitchStat.addOnButtonCheckedListener { _, checkedId, _ ->
            when (checkedId) {
                R.id.buttonStock -> {
                    statsViewModel.setStatType(StatType.STOCK)
                    yearPicker.setPickYearAllowed(allowed = false)
                }
                R.id.buttonReplenishments -> {
                    statsViewModel.setStatType(StatType.REPLENISHMENTS)
                    yearPicker.setPickYearAllowed(allowed = true)
                }
                R.id.buttonConsumptions -> {
                    statsViewModel.setStatType(StatType.CONSUMPTIONS)
                    yearPicker.setPickYearAllowed(allowed = true)
                }
            }
        }
    }

    private fun observe() {
        val statType = arguments
            ?.getSerializable("com.louis.app.cavity.ui.home.FragmentWines.STAT_TYPE_ID")
                as StatGlobalType


        statsViewModel.results(statType).observe(viewLifecycleOwner) {
            lifecycleScope.launch(Default) {
                val total = it.sumBy { stat -> stat.count }
                val slices = it.map { stat ->
                    stat.resolve(context)
                    val angle = (stat.count.toFloat() / total.toFloat()) * 360f
                    PieView.PieSlice(stat.label, angle, stat.color)
                }

                withContext(Main) {
                    binding.pieView.setPieData(slices, anim = true)
                }
            }

        }


        binding.buttonStock.isChecked = true
    }

    companion object {
        private const val STAT_TYPE_ID =
            "com.louis.app.cavity.ui.home.FragmentWines.STAT_TYPE_ID"

        // Used by WinesPagerAdapter
        fun newInstance(statGlobalType: StatGlobalType): FragmentPie {
            return FragmentPie().apply {
                arguments = bundleOf(STAT_TYPE_ID to statGlobalType)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

enum class StatGlobalType {
    COUNTY,
    COLOR,
    VINTAGE,
    NAMING
}

