package com.louis.app.cavity.ui.bottle.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ChipActionBinding
import com.louis.app.cavity.model.Bottle

class BottleChipRecyclerAdapter(context: Context, private val onBottleClick: (Long) -> Unit) :
    ListAdapter<Bottle, BottleChipRecyclerAdapter.BottleChipViewHolder>(BottleItemDiffCallback()) {

    private val glassIcon by lazy {
        val resources = context.resources
        val color = ResourcesCompat.getColor(resources, R.color.high_emphasis, context.theme)
        ContextCompat.getDrawable(context, R.drawable.ic_glass)?.also { it.setTint(color) }
    }

    private var checkedId = -1L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottleChipViewHolder {
        val binding = ChipActionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BottleChipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BottleChipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int) = currentList[position].id

    fun submitListWithPreselection(list: List<Bottle>?, checkedBottleId: Long): Long {
        // Check a chip if no one is checked
        checkedId = if (checkedBottleId == -1L && list != null) {
            list.first().id
        } else {
            checkedBottleId
        }

        super.submitList(list)
        return checkedId
    }

    class BottleItemDiffCallback : DiffUtil.ItemCallback<Bottle>() {
        override fun areItemsTheSame(oldItem: Bottle, newItem: Bottle) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Bottle, newItem: Bottle) =
            oldItem.vintage == newItem.vintage &&
                oldItem.isReadyToDrink() == newItem.isReadyToDrink()
    }

    inner class BottleChipViewHolder(private val binding: ChipActionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bottle: Bottle) {
            with(binding.root) {
                text = bottle.vintage.toString()
                chipIcon = if (bottle.isReadyToDrink()) glassIcon else null

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
            val toUncheck = currentList.indexOfFirst { it.id == currentCheckedChipId }
            notifyItemChanged(toUncheck)
        }
    }
}
