package com.louis.app.cavity.ui

import android.view.LayoutInflater
import android.widget.CompoundButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.louis.app.cavity.R
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Wine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface CountyLoader {
    fun loadCounties(
        scope: CoroutineScope,
        layoutInflater: LayoutInflater,
        chipGroup: ChipGroup,
        counties: Set<County>,
        editWine: Wine? = null,
        selectionRequired: Boolean = true,
        onCheckedChangeListener: ((btn: CompoundButton, isChecked: Boolean) -> Unit)? = null
    ) {
        scope.launch(Default) {
            for ((index, county) in counties.withIndex()) {
                val chip: Chip =
                    layoutInflater.inflate(
                        R.layout.chip_choice,
                        chipGroup,
                        false
                    ) as Chip
                chip.apply {
                    setTag(R.string.tag_chip_id, county)
                    text = county.name
                    onCheckedChangeListener?.let { setOnCheckedChangeListener(it) }
                }

                withContext(Main) {
                    chipGroup.addView(chip)
                    if (index == 0 && selectionRequired) chip.isChecked = true

                    if (editWine != null && county.countyId == editWine.countyId) {
                        chip.isChecked = true
                    }
                }
            }
        }
    }
}
