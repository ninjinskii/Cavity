package com.louis.app.cavity.ui.stats

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentPieBinding
import com.louis.app.cavity.util.ColorUtil
import com.louis.app.cavity.util.L

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
    }

    private fun setListeners() {
        binding.buttonGroupSwitchStat.addOnButtonCheckedListener { _, checkedId, _ ->
            when (checkedId) {
                R.id.buttonStock -> statsViewModel.setStatType(StatType.STOCK)
                R.id.buttonReplenishments -> statsViewModel.setStatType(StatType.REPLENISHMENTS)
                R.id.buttonConsumptions -> statsViewModel.setStatType(StatType.CONSUMPTIONS)
            }
        }
    }

    private fun observe() {
        val setPieData = { stat: Stat -> binding.pieView.setPieData(stat, anim = true) }
        val statType = arguments
            ?.getSerializable("com.louis.app.cavity.ui.home.FragmentWines.STAT_TYPE_ID")
                as StatGlobalType

        L.v(statType.name)

        when (statType) {
            StatGlobalType.COUNTY -> {
            }

            StatGlobalType.COLOR -> statsViewModel.colorStats.observe(viewLifecycleOwner) {
                L.v("observe")
                val stat = Stat(it.map { s ->
                    StringResStatItem(
                        name = ColorUtil.getStringResForWineColor(s.color),
                        count = s.count,
                        color = ColorUtil.getColorResForWineColor(s.color),
                        icon = null
                    )
                })
                setPieData(stat)
            }

            StatGlobalType.VINTAGE -> {
            }
            StatGlobalType.NAMING -> {
            }
        }
        binding.buttonStock.isChecked = true
    }

    companion object {
        private const val STAT_TYPE_ID = "com.louis.app.cavity.ui.home.FragmentWines.STAT_TYPE_ID"

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

