package com.louis.app.cavity.ui

import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.postDelayed
import androidx.core.widget.TextViewCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.louis.app.cavity.R
import com.louis.app.cavity.model.Chipable
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.util.AvatarLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChipLoader private constructor(
    private val scope: CoroutineScope,
    private val layoutInflater: LayoutInflater,
    @LayoutRes private val layout: Int,
    private val items: List<Chipable>,
    private val avatar: Boolean,
    private val chipGroup: ChipGroup,
    private val preselectedItems: List<Long>,
    private val selectable: Boolean,
    private val onEmpty: String?,
    private val showIconIf: (Chipable) -> Boolean,
    private val onClickListener: ((View) -> Unit)?
) {

    fun go() {
        scope.launch(Default) {
            val itemsToInflate = clearChipGroup()

            for ((index, item) in itemsToInflate.withIndex()) {
                val chip = layoutInflater.inflate(layout, chipGroup, false) as Chip

                chip.apply {
                    setTag(R.string.tag_chip_id, item)
                    text = item.getChipText()

                    if (showIconIf(item)) {
                        chipIcon = item.getIcon()?.let {
                            ContextCompat.getDrawable(context, it)
                        }
                    } else if (avatar && item is Friend) {
                        AvatarLoader.requestAvatar(context, item.imgPath) { avatar ->
                            chipIcon = avatar
                            chipIconTint = null
                        }
                    }

                    onClickListener?.let { setOnClickListener(it) }
                }

                withContext(Main) {
                    chipGroup.addView(chip)

                    if (selectable) {
                        chip.isCheckable = true

                        if (index == 0 && chipGroup.isSelectionRequired) {
                            chip.isChecked = preselectedItems.isEmpty()
                        }

                        chip.isChecked = item.getItemId() in preselectedItems
                    } else {
                        chip.isCheckable = false
                    }
                }
            }

            maybeShowEmptyState()
            scrollToCheckedChip()
        }
    }

    private suspend fun clearChipGroup(): List<Chipable> {
        chipGroup.children.firstOrNull { it is AppCompatTextView }?.let {
            withContext(Main) {
                chipGroup.removeView(it)
            }
        }

        val currentList = chipGroup.children.map { it.getTag(R.string.tag_chip_id) as Chipable }
        val toInflate = items.filter { it !in currentList }
        val toRemove = mutableSetOf<View>()

        chipGroup.forEach {
            val item = it.getTag(R.string.tag_chip_id) as Chipable?
            if (item !in items && item != null) toRemove.add(it)
        }

        withContext(Main) {
            toRemove.forEach { chipGroup.removeView(it) }
        }

        return toInflate
    }

    private fun scrollToCheckedChip() {
        chipGroup.children.firstOrNull { it is Chip && it.isChecked }?.let {
            val scrollView = findParentScrollView(chipGroup)

            scrollView?.postDelayed(500) {
                scrollView.smoothScrollTo(it.left - it.paddingLeft, it.top)
            }
        }
    }

    private fun findParentScrollView(view: View): HorizontalScrollView? {
        return try {
            val parent = view.parent
            parent as? HorizontalScrollView ?: findParentScrollView(parent as View)
        } catch (_: ClassCastException) {
            null
        }
    }

    private suspend fun maybeShowEmptyState() {
        withContext(Main) {
            val emptyTextView = AppCompatTextView(chipGroup.context).apply {
                text = onEmpty
                TextViewCompat.setTextAppearance(this, R.style.TextAppearance_Cavity_Body2_Italic)
            }

            if (items.isEmpty() && onEmpty != null) {
                chipGroup.removeAllViews()
                chipGroup.addView(emptyTextView)
            }
        }
    }

    data class Builder(
        private var scope: CoroutineScope? = null,
        private var layoutInflater: LayoutInflater? = null,
        @LayoutRes private var layout: Int = R.layout.chip_choice,
        private var items: List<Chipable> = emptyList(),
        private var avatar: Boolean = false,
        private var chipGroup: ChipGroup? = null,
        private var preselectedItems: List<Long> = emptyList(),
        private var selectable: Boolean = true,
        private var minified: Boolean = false,
        private var onEmpty: String? = null,
        private var showIconIf: (Chipable) -> Boolean = { false },
        private var onClickListener: ((View) -> Unit)? = null
    ) {
        fun with(scope: CoroutineScope) = apply { this.scope = scope }
        fun useInflater(inflater: LayoutInflater) = apply { this.layoutInflater = inflater }
        fun toInflate(@LayoutRes layout: Int) = apply { this.layout = layout }
        fun load(items: List<Chipable>) = apply { this.items = items }
        fun useAvatar(useAvatar: Boolean) = apply { this.avatar = useAvatar }
        fun into(chipGroup: ChipGroup) = apply { this.chipGroup = chipGroup }
        fun preselect(preselect: List<Long>) = apply { this.preselectedItems = preselect }
        fun preselect(preselect: Long) = apply { this.preselectedItems = listOf(preselect) }
        fun selectable(selectable: Boolean) = apply { this.selectable = selectable }
        fun emptyText(text: String?) = apply { this.onEmpty = text }

        @Suppress("unused")
        fun showIconIf(block: (Chipable) -> Boolean) = apply { this.showIconIf = block }
        fun doOnClick(block: (View) -> Unit) = apply {
            this.onClickListener = block
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
                layout,
                items,
                avatar,
                chipGroup!!,
                preselectedItems,
                selectable,
                onEmpty,
                showIconIf,
                onClickListener
            )
        }
    }
}
