package com.louis.app.cavity.ui.home

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemWineBinding
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.model.relation.wine.WineWithBottles
import com.louis.app.cavity.ui.WineColorResolver
import com.louis.app.cavity.util.toBoolean

class WineRecyclerAdapter(
    private val _context: Context,
    private var listeners: WineAdapterListener?
) :
    ListAdapter<WineWithBottles, WineViewHolder>(WineItemDiffCallback()), WineColorResolver {

    override fun getOverallContext() = _context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineViewHolder {
        val binding = ItemWineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val result = WineViewHolder(binding)

        return result
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

    interface WineAdapterListener {
        fun onItemClick()
        fun onItemLongClick()
    }
}
