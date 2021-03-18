package com.louis.app.cavity.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ChipActionBinding
import com.louis.app.cavity.model.Bottle

class BottleChipAdapter(
    private val onVintageClickListener: (Bottle) -> Unit
) :
    ListAdapter<Bottle, BottleChipAdapter.BottleViewHolder>(BottleItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottleViewHolder {
        val binding = ChipActionBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return BottleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BottleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int) = R.layout.chip_action

    class BottleItemDiffCallback : DiffUtil.ItemCallback<Bottle>() {
        override fun areItemsTheSame(
            oldItem: Bottle,
            newItem: Bottle
        ) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Bottle,
            newItem: Bottle
        ) =
            oldItem == newItem
    }

    inner class BottleViewHolder(private val binding: ChipActionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bottle: Bottle) {
            binding.root.text = bottle.vintage.toString()

            binding.root.setOnClickListener {
                onVintageClickListener(bottle)
            }
        }
    }


}
