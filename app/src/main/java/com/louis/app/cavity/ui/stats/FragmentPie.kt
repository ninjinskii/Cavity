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
    private var _binding: FragmentPieBinding? = null
    private val binding get() = _binding!!
    private val statsViewModel: StatsViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPieBinding.bind(view)

        setListeners()
        observe()
        maybeShowYearPicker()
    }

    private fun setListeners() {
        binding.buttonGroupSwitchStat.addOnButtonCheckedListener { _, checkedId, _ ->
            triggerChecked(checkedId)
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

        statsViewModel.currentItemPosition.observe(viewLifecycleOwner) {
            if (it == arguments?.getInt(POSITION)) {
                maybeShowYearPicker()
            }
        }


        binding.buttonStock.isChecked = true
    }

    private fun triggerChecked(checkedId: Int) {
        when (checkedId) {
            R.id.buttonStock -> statsViewModel.setStatType(StatType.STOCK)
            R.id.buttonReplenishments -> statsViewModel.setStatType(StatType.REPLENISHMENTS)
            R.id.buttonConsumptions -> statsViewModel.setStatType(StatType.CONSUMPTIONS)
        }
    }

    private fun maybeShowYearPicker() {
        statsViewModel.setShouldShowYearPicker(
            binding.buttonGroupSwitchStat.checkedButtonId != R.id.buttonStock
        )
    }

    companion object {
        private const val STAT_TYPE_ID = "com.louis.app.cavity.ui.home.FragmentWines.STAT_TYPE_ID"
        private const val POSITION = "com.louis.app.cavity.ui.home.FragmentWines.POSITION"

        // Used by WinesPagerAdapter
        fun newInstance(statGlobalType: StatGlobalType, position: Int): FragmentPie {
            return FragmentPie().apply {
                arguments = bundleOf(STAT_TYPE_ID to statGlobalType, POSITION to position)
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

