package com.louis.app.cavity.ui.stats

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentPieBinding
import com.louis.app.cavity.db.dao.ColorStat
import com.louis.app.cavity.db.dao.Stat
import com.louis.app.cavity.ui.stats.widget.PieView
import com.louis.app.cavity.util.ColorUtil
import com.louis.app.cavity.util.setVisible
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

        viewPagerPosition = requireArguments().getInt(VIEW_PAGER_POSITION)
        binding.title.text = requireContext().getString(requireArguments().getInt(TITLE_RES))

        setListeners()
        observe()
        maybeShowYearPicker()
    }

    private fun setListeners() {
        binding.buttonStock.isChecked = true

        binding.buttonGroupSwitchStat.addOnButtonCheckedListener { _, checkedId, _ ->
            statsViewModel.setShouldShowYearPicker(checkedId != R.id.buttonStock)

            val stockType = when (checkedId) {
                R.id.buttonReplenishments -> StatType.REPLENISHMENTS
                R.id.buttonConsumptions -> StatType.CONSUMPTIONS
                else /* R.id.buttonStock */ -> StatType.STOCK
            }

            statsViewModel.setStatType(viewPagerPosition, stockType)
        }

    }

    private fun observe() {
        statsViewModel.currentItemPosition.observe(viewLifecycleOwner) {
            if (it == viewPagerPosition) {
                maybeShowYearPicker()
            }
        }

        statsViewModel.results[viewPagerPosition].observe(viewLifecycleOwner) {
            updatePieData(binding.pieView, it)
        }

        statsViewModel.comparison.observe(viewLifecycleOwner) {
            with(binding) {
                comparisonPieView.setVisible(it)
                comparisonText.setVisible(it)
                buttonGroupSwitchStat.setVisible(!it)
            }
        }

        statsViewModel.comparisonDetails.observe(viewLifecycleOwner) {
            updatePieData(binding.comparisonPieView, it)
        }

        statsViewModel.comparisonText.observe(viewLifecycleOwner) {
            binding.comparisonText.text = it
        }
    }

    private fun maybeShowYearPicker() {
        statsViewModel.setShouldShowYearPicker(
            binding.buttonGroupSwitchStat.checkedButtonId != R.id.buttonStock
        )
    }

    private fun updatePieData(pieView: PieView, stats: List<Stat>) {
        lifecycleScope.launch(Default) {
            val total = stats.sumBy { stat -> stat.count }
            val slices = stats.map { stat ->
                val angle = (stat.count.toFloat() / total.toFloat()) * 360f

                if (stat is ColorStat) {
                    stat.label =
                        context?.getString(ColorUtil.getStringResForWineColor(stat.color ?: 0))
                            ?: ""
                }

                PieView.PieSlice(stat.label, angle, stat.safeColor)
            }

            withContext(Main) {
                pieView.setPieData(slices, anim = true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TITLE_RES = "com.louis.app.cavity.ui.home.FragmentWines.TITLE_RES"
        private const val VIEW_PAGER_POSITION =
            "com.louis.app.cavity.ui.home.FragmentWines.VIEW_PAGER_POSITION"

        // Used by StatsPagerAdapter
        fun newInstance(pagerPosition: Int, @StringRes titleRes: Int): FragmentPie {
            return FragmentPie().apply {
                arguments = bundleOf(
                    VIEW_PAGER_POSITION to pagerPosition,
                    TITLE_RES to titleRes
                )
            }
        }
    }
}

