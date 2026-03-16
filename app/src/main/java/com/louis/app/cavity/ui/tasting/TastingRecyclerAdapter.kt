package com.louis.app.cavity.ui.tasting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.databinding.ItemTastingBinding
import com.louis.app.cavity.db.dao.BoundedTasting
import com.louis.app.cavity.model.Tasting

class TastingRecyclerAdapter(
    private val childViewPool: RecyclerView.RecycledViewPool,
    private val onItemClickListener: (Tasting, View) -> Unit
) :
    ListAdapter<BoundedTasting, TastingViewHolder>
        (TastingItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TastingViewHolder {
        val binding = ItemTastingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TastingViewHolder(binding, childViewPool, onItemClickListener)
    }

    override fun onBindViewHolder(holder: TastingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TastingItemDiffCallback : DiffUtil.ItemCallback<BoundedTasting>() {
        override fun areItemsTheSame(oldItem: BoundedTasting, newItem: BoundedTasting) =
            oldItem.tasting.id == newItem.tasting.id

        override fun areContentsTheSame(oldItem: BoundedTasting, newItem: BoundedTasting) =
            oldItem == newItem
    }
}
