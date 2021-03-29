package com.louis.app.cavity.ui

import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.HorizontalScrollView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.postDelayed
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.louis.app.cavity.R
import com.louis.app.cavity.model.Chipable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChipLoader private constructor(
    private val scope: CoroutineScope,
    private val layoutInflater: LayoutInflater,
    private val items: List<Chipable>,
    private val chipGroup: ChipGroup,
    private val preselectedItems: List<Long>,
    private val selectable: Boolean,
    private val onCheckedChangeListener: ((btn: CompoundButton, isChecked: Boolean) -> Unit)?
) {
    fun go() {
        scope.launch(Default) {
            for ((index, item) in items.withIndex()) {
                val layout = if (selectable) R.layout.chip_choice else R.layout.chip_action
                val chip = layoutInflater.inflate(layout, chipGroup, false) as Chip

                chip.apply {
                    setTag(R.string.tag_chip_id, item)
                    text = item.getChipText()
                    onCheckedChangeListener?.let { setOnCheckedChangeListener(it) }
                    chipIcon = item.getIcon()?.let {
                        ContextCompat.getDrawable(context, it)
                    }
                }

                withContext(Main) {
                    chipGroup.addView(chip)

                    if (selectable) {
                        if (index == 0 && chipGroup.isSelectionRequired) {
                            chip.isChecked = preselectedItems.isEmpty()
                        }

                        chip.isChecked = item.getItemId() in preselectedItems
                    }
                }
            }

            chipGroup.children.firstOrNull { it is Chip && it.isChecked }?.let {
                val scrollView = findParentScrollView(chipGroup)

                scrollView?.postDelayed(500) {
                    scrollView.smoothScrollTo(it.left - it.paddingLeft, it.top)
                }
            }
        }
    }

    private fun findParentScrollView(view: View) : HorizontalScrollView? {
        return try {
            val parent = view.parent
            if (parent is HorizontalScrollView) parent else findParentScrollView(parent as View)
        } catch (e: ClassCastException) {
            null
        }
    }

    data class Builder(
        private var scope: CoroutineScope? = null,
        private var layoutInflater: LayoutInflater? = null,
        private var items: List<Chipable> = emptyList(),
        private var chipGroup: ChipGroup? = null,
        private var preselectedItems: List<Long> = emptyList(),
        private var selectable: Boolean = true,
        private var onCheckedChangeListener: ((btn: CompoundButton, isChecked: Boolean) -> Unit)? = null
    ) {
        fun with(scope: CoroutineScope) = apply { this.scope = scope }
        fun useInflater(inflater: LayoutInflater) = apply { this.layoutInflater = inflater }
        fun load(items: List<Chipable>) = apply { this.items = items }
        fun into(chipGroup: ChipGroup) = apply { this.chipGroup = chipGroup }
        fun preselect(preselect: List<Long>) = apply { this.preselectedItems = preselect }
        fun preselect(preselect: Long) = apply { this.preselectedItems = listOf(preselect) }
        fun selectable(selectable: Boolean) = apply { this.selectable = selectable }
        fun doOnClick(block: (btn: CompoundButton, isChecked: Boolean) -> Unit) = apply {
            this.onCheckedChangeListener = block
        }

        fun build(): ChipLoader {
            when {
                scope == null ->
                    throw IllegalStateException(
                        "Must provide a coroutine scope by calling 'with()'"
                    )

                layoutInflater == null ->
                    throw IllegalStateException(
                        "Must provide a layout inflater by calling 'useInflater()'"
                    )

                chipGroup == null ->
                    throw IllegalStateException(
                        "Must provide a chipgroup by calling 'into()'"
                    )
            }

            return ChipLoader(
                scope!!,
                layoutInflater!!,
                items,
                chipGroup!!,
                preselectedItems,
                selectable,
                onCheckedChangeListener
            )
        }
    }
}
