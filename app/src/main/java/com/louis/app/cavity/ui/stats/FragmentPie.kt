package com.louis.app.cavity.ui.stats

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentPieBinding
import com.louis.app.cavity.db.dao.Stat
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
    private var viewPagerPosition: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPieBinding.bind(view)

        viewPagerPosition = arguments?.getInt(VIEW_PAGER_POSITION) ?: 0

        setListeners()
        observe()
        maybeShowYearPicker()
    }

    private fun setListeners() {
        binding.buttonGroupSwitchStat.addOnButtonCheckedListener { _, checkedId, _ ->
            statsViewModel.setShouldShowYearPicker(checkedId != R.id.buttonStock)

            val stockType = when (checkedId) {
                R.id.buttonReplenishments -> StatType.REPLENISHMENTS
                R.id.buttonConsumptions -> StatType.CONSUMPTIONS
                else /* R.id.buttonStock */ -> StatType.STOCK
            }

            statsViewModel.setStatType(viewPagerPosition, stockType)
        }

        binding.buttonStock.isChecked = true

        binding.buttonCompare.setOnClickListener {
            statsViewModel.toggleComparison()
        }
    }

    private fun observe() {
        val position = arguments?.getInt(VIEW_PAGER_POSITION) ?: 0

        statsViewModel.currentItemPosition.observe(viewLifecycleOwner) {
            if (it == position) {
                maybeShowYearPicker()
            }
        }

        statsViewModel.results[position].observe(viewLifecycleOwner) {
            updatePieData(it)
        }
    }

    private fun maybeShowYearPicker() {
        statsViewModel.setShouldShowYearPicker(
            binding.buttonGroupSwitchStat.checkedButtonId != R.id.buttonStock
        )
    }

    private fun updatePieData(stats: List<Stat>) {
        lifecycleScope.launch(Default) {
            val total = stats.sumBy { stat -> stat.count }
            val slices = stats.map { stat ->
                stat.resolve(context)
                val angle = (stat.count.toFloat() / total.toFloat()) * 360f
                PieView.PieSlice(stat.label, angle, stat.color)
            }
            withContext(Main) {
                binding.pieView.setPieData(slices, anim = true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val VIEW_PAGER_POSITION =
            "com.louis.app.cavity.ui.home.FragmentWines.VIEW_PAGER_POSITION"

        // Used by StatsPagerAdapter
        fun newInstance(pagerPosition: Int): FragmentPie {
            return FragmentPie().apply {
                arguments = bundleOf(VIEW_PAGER_POSITION to pagerPosition)
            }
        }
    }
}

