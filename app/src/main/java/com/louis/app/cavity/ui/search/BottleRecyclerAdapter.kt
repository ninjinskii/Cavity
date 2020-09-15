package com.louis.app.cavity.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemBottleBinding
import com.louis.app.cavity.model.Bottle

class BottleRecyclerAdapter :
    ListAdapter<Bottle, BottleRecyclerAdapter.BottleViewHolder>(BottleItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottleViewHolder {
        return BottleViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_bottle, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BottleViewHolder, position: Int) =
        holder.bind(getItem(position))

    override fun getItemId(position: Int): Long {
        return currentList[position].idBottle
    }

    class BottleItemDiffCallback : DiffUtil.ItemCallback<Bottle>() {
        override fun areItemsTheSame(oldItem: Bottle, newItem: Bottle) =
            oldItem.idBottle == newItem.idBottle

        override fun areContentsTheSame(oldItem: Bottle, newItem: Bottle) =
            oldItem == newItem
    }

    inner class BottleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemBottleBinding.bind(itemView)

        fun bind(bottle: Bottle) {

        }
    }
}
