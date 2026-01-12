package com.louis.app.cavity.ui.bottle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.louis.app.cavity.databinding.ItemTastingLogBinding
import com.louis.app.cavity.db.dao.BottleWithHistoryEntries


class TastingLogRecyclerAdapter(private val onNextClick: () -> Unit) :
    ListAdapter<BottleWithHistoryEntries, TastingLogViewHolder>(LogItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TastingLogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TastingLogViewHolder(
            ItemTastingLogBinding.inflate(inflater, parent, false),
            itemCount,
            onNextClick
        )
    }

    override fun onBindViewHolder(holder: TastingLogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int) = getItem(position).bottle.id

    class LogItemDiffCallback : DiffUtil.ItemCallback<BottleWithHistoryEntries>() {
        override fun areItemsTheSame(
            oldItem: BottleWithHistoryEntries,
            newItem: BottleWithHistoryEntries
        ) =
            oldItem.bottle.id == newItem.bottle.id

        override fun areContentsTheSame(
            oldItem: BottleWithHistoryEntries,
            newItem: BottleWithHistoryEntries
        ) =
            oldItem == newItem
    }
}
