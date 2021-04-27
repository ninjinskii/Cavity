package com.louis.app.cavity.ui.stats

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentPieBinding
import com.louis.app.cavity.db.dao.Stat
import com.louis.app.cavity.ui.stats.widget.PieView

class FragmentPie : Fragment(R.layout.fragment_pie) {
    lateinit var globalStatType: StatGlobalType
    private var _binding: FragmentPieBinding? = null
    private val binding get() = _binding!!
    private val statsViewModel: StatsViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPieBinding.bind(view)

        globalStatType = arguments?.getSerializable(STAT_TYPE_ID) as StatGlobalType

        setListeners()
        observe()
        maybeShowYearPicker()
    }

    private fun setListeners() {
        var observedData: LiveData<out List<Stat>>? = null
        val observer = Observer<List<Stat>> {
            updatePieData(it)
        }

        binding.buttonGroupSwitchStat.addOnButtonCheckedListener { _, checkedId, _ ->
            statsViewModel.setShouldShowYearPicker(checkedId != R.id.buttonStock)

            observedData?.removeObserver(observer)
            observedData = switchData(checkedId).also {
                it.observe(viewLifecycleOwner, observer)
            }
        }

        binding.buttonStock.isChecked = true
    }

    private fun observe() {
        statsViewModel.currentItemPosition.observe(viewLifecycleOwner) {
            if (it == arguments?.getInt(POSITION)) {
                maybeShowYearPicker()
            }
        }
    }

    private fun switchData(checkedId: Int) = when (checkedId) {
        R.id.buttonReplenishments -> chooseData(StatType.REPLENISHMENTS)
        R.id.buttonConsumptions -> chooseData(StatType.CONSUMPTIONS)
        else /* R.id.buttonStock */ -> chooseData(StatType.STOCK)
    }

    private fun chooseData(statType: StatType) = when (globalStatType) {
        StatGlobalType.COUNTY -> statsViewModel.getCountyStats(statType)
        StatGlobalType.COLOR -> statsViewModel.getColorStats(statType)
        StatGlobalType.VINTAGE -> statsViewModel.getVintageStats(statType)
        StatGlobalType.NAMING -> statsViewModel.getNamingStats(statType)
    }

    private fun maybeShowYearPicker() {
        statsViewModel.setShouldShowYearPicker(
            binding.buttonGroupSwitchStat.checkedButtonId != R.id.buttonStock
        )
    }

    private fun updatePieData(stats: List<Stat>) {
        val total = stats.sumBy { stat -> stat.count }
        val slices = stats.map { stat ->
            stat.resolve(context)
            val angle = (stat.count.toFloat() / total.toFloat()) * 360f
            PieView.PieSlice(stat.label, angle, stat.color)
        }
        binding.pieView.setPieData(slices, anim = true)

        // Memory leak hunt
//        lifecycleScope.launch(Default) {
//            withContext(Main) {
//            }
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
}

enum class StatGlobalType {
    COUNTY,
    COLOR,
    VINTAGE,
    NAMING
}

