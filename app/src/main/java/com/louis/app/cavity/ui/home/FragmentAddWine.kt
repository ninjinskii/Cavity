package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogAddCountyBinding
import com.louis.app.cavity.databinding.FragmentAddWineBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.showKeyboard
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentAddWine : Fragment(R.layout.fragment_add_wine) {
    private lateinit var binding: FragmentAddWineBinding
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAddWineBinding.bind(view)

        loadCounties()
        setListeners()
    }

    private fun loadCounties() {
        val allCounties = mutableSetOf<County>()
        val alreadyInflated = mutableSetOf<County>()

        homeViewModel.getAllCounties().observe(viewLifecycleOwner) {
            allCounties.addAll(it)
            val toInflate = allCounties - alreadyInflated

            lifecycleScope.launch(Default) {
                for (county in toInflate) {
                    val chip: Chip =
                        layoutInflater.inflate(
                            R.layout.chip_choice,
                            binding.countyChipGroup,
                            false
                        ) as Chip
                    chip.apply {
                        setTag(R.string.tag_chip_id, county)
                        text = county.name
                    }

                    withContext(Main) {
                        binding.countyChipGroup.addView(chip)
                    }
                }

                alreadyInflated.addAll(toInflate)
            }
        }
    }

    private fun setListeners() {
        binding.submitAddWine.setOnClickListener {
            with(binding) {
                val name = name.text.toString()
                val naming = naming.text.toString()
                val isOrganic = organicWine.isChecked.toInt()
                val color = colorChipGroup.checkedChipId // always -1, need fix

                Wine(0, name, naming, getWineColor(color), 0, isOrganic, "").also {
                    homeViewModel.addWine(it)
                }
            }
        }

        binding.buttonAddCounty.setOnClickListener {
            val dialogBinding = DialogAddCountyBinding.inflate(layoutInflater)

            MaterialAlertDialogBuilder(this.requireContext())
                .setTitle(resources.getString(R.string.add_county))
                .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                }
                .setPositiveButton(resources.getString(R.string.submit)) { _, _ ->
                    homeViewModel.addCounty(dialogBinding.countyName.text.toString())
                }
                .setView(dialogBinding.root)
                .show()

            it.postDelayed({ context?.showKeyboard(dialogBinding.countyName) }, 100)
        }
    }

    // enum ?
    private fun getWineColor(chipId: Int): Int {
        L.v(chipId.toString())
        return when (chipId) {
            R.id.colorWhite -> 0
            R.id.colorRed -> 1
            R.id.colorSweet -> 2
            else -> 3
        }
    }
}
