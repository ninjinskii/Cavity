package com.louis.app.cavity.ui.bottle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemShowReviewMedalBinding
import com.louis.app.cavity.databinding.ItemShowReviewRateBinding
import com.louis.app.cavity.databinding.ItemShowReviewStarBinding
import com.louis.app.cavity.databinding.ItemTastingLogBinding
import com.louis.app.cavity.db.dao.BottleWithHistoryEntries
import com.louis.app.cavity.db.dao.FReviewAndReview
import com.louis.app.cavity.util.ColorUtil


class TastingLogRecyclerAdapter() :
    ListAdapter<BottleWithHistoryEntries, TastingLogViewHolder>(LogItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TastingLogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TastingLogViewHolder(ItemTastingLogBinding.inflate(inflater, parent, false))
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
