package com.louis.app.cavity.ui.bottle.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ChipActionBinding
import com.louis.app.cavity.db.dao.BottleWithHistoryEntries
import com.louis.app.cavity.domain.history.isConsumption
import com.louis.app.cavity.util.toBoolean

class BottleChipRecyclerAdapter(context: Context, private val onBottleClick: (Long) -> Unit) :
    ListAdapter<BottleWithHistoryEntries, BottleChipRecyclerAdapter.BottleChipViewHolder>(
        BottleItemDiffCallback()
    ) {

    private val glassIcon by lazy {
        val resources = context.resources
        val color = ResourcesCompat.getColor(resources, R.color.high_emphasis, context.theme)
        ContextCompat.getDrawable(context, R.drawable.ic_glass)?.also { it.setTint(color) }
    }

    private val commentIcon by lazy {
        val resources = context.resources
        val color = ResourcesCompat.getColor(resources, R.color.high_emphasis, context.theme)
        ContextCompat.getDrawable(context, R.drawable.ic_comment)?.also { it.setTint(color) }
    }

    private var checkedId = -1L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottleChipViewHolder {
        val binding = ChipActionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BottleChipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BottleChipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int) = currentList[position].bottle.id

    fun submitListWithPreselection(list: List<BottleWithHistoryEntries>?, checkedBottleId: Long): Long {
        // Check a chip if no one is checked
        checkedId = if (checkedBottleId == -1L && list != null) {
            list.first().bottle.id
        } else {
            checkedBottleId
        }

        super.submitList(list)
        return checkedId
    }

    class BottleItemDiffCallback : DiffUtil.ItemCallback<BottleWithHistoryEntries>() {
        override fun areItemsTheSame(oldItem: BottleWithHistoryEntries, newItem: BottleWithHistoryEntries) =
            oldItem.bottle.id == newItem.bottle.id

        override fun areContentsTheSame(oldItem: BottleWithHistoryEntries, newItem: BottleWithHistoryEntries) =
            oldItem.bottle.vintage == newItem.bottle.vintage &&
                    oldItem.bottle.isReadyToDrink() == newItem.bottle.isReadyToDrink() &&
                    oldItem.bottle.isFavorite == newItem.bottle.isFavorite &&
                    oldItem.bottle.consumed == newItem.bottle.consumed
    }

    inner class BottleChipViewHolder(private val binding: ChipActionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bottleWithHistoryEntries: BottleWithHistoryEntries) {
            val (bottle) = bottleWithHistoryEntries
            with(binding.root) {
                text = bottle.vintage.toString()
                chipIcon = getChipStartIcon(bottleWithHistoryEntries)
                isCloseIconVisible = bottle.isFavorite.toBoolean()

                if (bottle.consumed.toBoolean()) {
                    chipBackgroundColor =
                        context.getColorStateList(R.color.chip_background_consumed)
                    chipStrokeColor = context.getColorStateList(R.color.chip_stroke_consumed)
                } else {
                    chipBackgroundColor = null
                    chipStrokeColor = context.getColorStateList(R.color.chip_stroke)
                }

                // We are using the said "close icon" as a simple end icon
                setOnCloseIconClickListener {
                    binding.root.performClick()
                }

                setOnCheckedChangeListener(null)
                isChecked = bottle.id == checkedId
                setOnCheckedChangeListener { chip, isChecked ->
                    if (!isChecked) {
                        chip.isChecked = true
                    } else {
                        updateCheckedChip(checkedId)
                        checkedId = bottle.id
                        onBottleClick(bottle.id)
                    }
                }
            }
        }

        // Rebinds the current checked chip to unckeck itself
        private fun updateCheckedChip(currentCheckedChipId: Long) {
            val toUncheck = currentList.indexOfFirst { it.bottle.id == currentCheckedChipId }
            notifyItemChanged(toUncheck)
        }

        private fun getChipStartIcon(bottleWithHistoryEntries: BottleWithHistoryEntries): Drawable? {
            val (bottle, historyEntries) = bottleWithHistoryEntries
            val consumeEntry =
                historyEntries.firstOrNull { it.type.isConsumption() }

            return when {
                bottle.consumed.toBoolean() &&
                        consumeEntry?.comment?.isNotBlank() == true -> commentIcon

                !bottle.consumed.toBoolean() && bottle.isReadyToDrink() -> glassIcon
                else -> null
            }
        }
    }
}
