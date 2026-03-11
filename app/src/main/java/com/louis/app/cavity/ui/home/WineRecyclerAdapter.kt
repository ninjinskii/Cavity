package com.louis.app.cavity.ui.home

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemWineBinding
import com.louis.app.cavity.db.dao.WineWithBottles

class WineRecyclerAdapter(
    private val drawables: Pair<Drawable, Drawable>,
    private val isLightTheme: Boolean,
    private val onItemClick: (wineWithBottles: WineWithBottles, itemView: View) -> Unit,
    private val onItemLongClick: (wineWithBottles: WineWithBottles) -> Unit
) :
    ListAdapter<WineWithBottles, WineViewHolder>(WineItemDiffCallback()) {

    var highlightPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineViewHolder {
        val binding = ItemWineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WineViewHolder(binding, drawables, isLightTheme)
    }

    // Rebind click listeners every bind because listeners captures FragmentWines and view holders
    // are shared between multiple FragmentWines, which can lead to crash when navigating or else
    override fun onBindViewHolder(holder: WineViewHolder, position: Int) {
        val item = getItem(position)
        val highlight = highlightPosition == position

        with(holder) {
            bind(item, highlight)

            itemView.setOnClickListener {
                onItemClick(item, itemView)
            }

            itemView.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }

        if (highlight) {
            highlightPosition = null
        }
    }

    override fun getItemId(position: Int) = getItem(position).wine.id

    override fun getItemViewType(position: Int) = R.layout.item_wine

    override fun onViewRecycled(holder: WineViewHolder) {
        super.onViewRecycled(holder)

        // We need to null out listeners, because they capture FragmentWines and view holders are
        // shared between multiple FragmentWines, which can lead to crash when navigating or else
        with(holder.itemView) {
            setOnClickListener(null)
            setOnLongClickListener(null)
        }
    }

    class WineItemDiffCallback : DiffUtil.ItemCallback<WineWithBottles>() {
        override fun areItemsTheSame(oldItem: WineWithBottles, newItem: WineWithBottles) =
            oldItem.wine.id == newItem.wine.id

        override fun areContentsTheSame(oldItem: WineWithBottles, newItem: WineWithBottles) =
            oldItem.wine == newItem.wine && oldItem.bottles.size == newItem.bottles.size
    }
}
