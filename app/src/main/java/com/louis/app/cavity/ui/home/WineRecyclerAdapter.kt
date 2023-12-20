package com.louis.app.cavity.ui.home

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemWineBinding
import com.louis.app.cavity.db.dao.WineWithBottles
import com.louis.app.cavity.util.TransitionHelper

class WineRecyclerAdapter(
    private val transitionHelper: TransitionHelper,
    private val drawables: Pair<Drawable, Drawable>
) :
    ListAdapter<WineWithBottles, WineViewHolder>(WineItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineViewHolder {
        val binding = ItemWineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WineViewHolder(binding, transitionHelper, drawables)
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
            oldItem.wine == newItem.wine && oldItem.bottles.size == newItem.bottles.size
    }
}
