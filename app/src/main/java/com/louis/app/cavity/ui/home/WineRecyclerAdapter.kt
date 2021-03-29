package com.louis.app.cavity.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemWineBinding
import com.louis.app.cavity.model.relation.wine.WineWithBottles

class WineRecyclerAdapter(private val viewPool: RecyclerView.RecycledViewPool) :
    ListAdapter<WineWithBottles, WineViewHolder>(WineItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineViewHolder {
        val binding = ItemWineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WineViewHolder(binding, viewPool)
    }

    override fun onBindViewHolder(holder: WineViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int) = getItem(position).wine.id

    override fun getItemViewType(position: Int) = R.layout.item_wine

    class WineItemDiffCallback : DiffUtil.ItemCallback<WineWithBottles>() {
        override fun areItemsTheSame(oldItem: WineWithBottles, newItem: WineWithBottles) =
            oldItem.wine.id == newItem.wine.id

        override fun areContentsTheSame(oldItem: WineWithBottles, newItem: WineWithBottles) =
            oldItem.wine == newItem.wine && oldItem.bottles == newItem.bottles
    }
}
