package com.louis.app.cavity.ui

import android.view.LayoutInflater
import android.widget.CompoundButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.louis.app.cavity.R
import com.louis.app.cavity.model.Chipable
import com.louis.app.cavity.model.County
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChipLoader(
    private val scope: CoroutineScope,
    private val layoutInflater: LayoutInflater
) {
    fun loadChips(
        into: ChipGroup,
        items: List<Chipable>,
        preselect: List<Long>,
        selectionRequired: Boolean = true,
        onCheckedChangeListener: ((btn: CompoundButton, isChecked: Boolean) -> Unit)? = null
    ) {
        scope.launch(Default) {
            for ((index, item) in items.withIndex()) {
                val chip: Chip =
                    layoutInflater.inflate(
                        R.layout.chip_choice,
                        into,
                        false
                    ) as Chip

                chip.apply {
                    setTag(R.string.tag_chip_id, item)
                    text = item.getChipText()
                    onCheckedChangeListener?.let { setOnCheckedChangeListener(it) }
                }

                withContext(Main) {
                    into.addView(chip)

                    if (index == 0 && selectionRequired && preselect.isEmpty())
                        chip.isChecked = true

                    if (item.getId() in preselect)
                        chip.isChecked = true
                }
            }
        }
    }
}
