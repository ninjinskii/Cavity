package com.louis.app.cavity.ui.home

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ChipActionBinding
import com.louis.app.cavity.model.Bottle

class BottleChipRecyclerAdapter(context: Context, private val onClick: (Long) -> Unit) :
    ListAdapter<Bottle, BottleChipRecyclerAdapter.BottleChipViewHolder>(BottleItemDiffCallback()) {

    private val glassIcon by lazy {
        val color = TypedValue().run {
            context.theme.resolveAttribute(R.attr.colorOnSurface, this, true)
            data
        }

        ContextCompat.getDrawable(context, R.drawable.ic_glass)?.also { it.setTint(color) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottleChipViewHolder {
        val binding = ChipActionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BottleChipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BottleChipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int) = currentList[position].id

    class BottleItemDiffCallback : DiffUtil.ItemCallback<Bottle>() {
        override fun areItemsTheSame(oldItem: Bottle, newItem: Bottle) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Bottle, newItem: Bottle) = oldItem == newItem
    }

    inner class BottleChipViewHolder(private val binding: ChipActionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bottle: Bottle) {
            with(binding.root) {
                text = bottle.vintage.toString()
                chipIcon = if (bottle.isReadyToDrink()) glassIcon else null

                setOnClickListener { onClick(bottle.id) }
            }
        }
    }
}
