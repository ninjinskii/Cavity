package com.louis.app.cavity.ui.bottle.adapter

import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemTastingLogBinding
import com.louis.app.cavity.db.dao.BottleWithHistoryEntries
import com.louis.app.cavity.domain.history.isConsumption
import com.louis.app.cavity.util.DateFormatter

class TastingLogViewHolder(private val binding: ItemTastingLogBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(bottleWithHistoryEntries: BottleWithHistoryEntries) = with(binding) {
        val (bottle, historyEntries) = bottleWithHistoryEntries
        val consumption = historyEntries.firstOrNull { it.type.isConsumption() }
        vintage.text = bottle.vintage.toString()
        drunkAt.text = consumption?.let { DateFormatter.formatDate(it.date) } ?: ""
        comment.text = consumption?.comment ?: binding.root.context.getString(R.string.base_error)
    }
}
