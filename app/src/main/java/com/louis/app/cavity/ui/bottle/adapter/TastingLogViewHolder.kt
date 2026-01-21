package com.louis.app.cavity.ui.bottle.adapter

import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemTastingLogBinding
import com.louis.app.cavity.db.dao.BottleWithHistoryEntries
import com.louis.app.cavity.util.DateFormatter
import com.louis.app.cavity.util.setVisible

class TastingLogViewHolder(
    private val binding: ItemTastingLogBinding,
    private val itemCount: Int,
    private val onNextCLick: () -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(bottleWithHistoryEntries: BottleWithHistoryEntries) = with(binding) {
        val (bottle) = bottleWithHistoryEntries
        val consumption = bottleWithHistoryEntries.getConsumptionEntry()
        vintage.text = bottle.vintage.toString()
        drunkAt.text = consumption?.let { DateFormatter.formatDate(it.date) } ?: ""
        comment.text = consumption?.comment ?: // ?.replace("\n", " - ")
            binding.root.context.getString(R.string.base_error)

        with(binding.buttonNext) {
            setVisible(bindingAdapterPosition == 0 && itemCount > 1)
            setOnClickListener { onNextCLick() }
        }

    }
}
