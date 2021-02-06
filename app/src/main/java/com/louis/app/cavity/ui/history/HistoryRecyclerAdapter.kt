package com.louis.app.cavity.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.databinding.ItemHistorySeparatorBinding
import com.louis.app.cavity.databinding.ItemHistoryUseBinding
import com.louis.app.cavity.util.DateFormatter

class HistoryRecyclerAdapter :
    PagingDataAdapter<HistoryUiModel, RecyclerView.ViewHolder>(
        HistoryEntryDiffItemCallback()
    ) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        if (holder is HistoryEntryViewHolder) {
            holder.bind(item as HistoryUiModel.EntryModel?)
        } else if (holder is HistorySeparatorViewHolder) {
            holder.bind(item as HistoryUiModel.HeaedrModel)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            0 -> HistoryEntryViewHolder(
                ItemHistoryUseBinding.inflate(inflater, parent, false)
            )
            else -> HistorySeparatorViewHolder(
                ItemHistorySeparatorBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is HistoryUiModel.EntryModel -> 0
        is HistoryUiModel.HeaedrModel -> 1
        else -> throw IllegalStateException("Unknown view type")
    }

    class HistoryEntryDiffItemCallback : DiffUtil.ItemCallback<HistoryUiModel>() {
        override fun areItemsTheSame(oldItem: HistoryUiModel, newItem: HistoryUiModel): Boolean {
            val isSameEntry = oldItem is HistoryUiModel.EntryModel
                    && newItem is HistoryUiModel.EntryModel
                    && oldItem.historyEntry.id == newItem.historyEntry.id

            val isSameSeparator = oldItem is HistoryUiModel.HeaedrModel
                    && newItem is HistoryUiModel.HeaedrModel
                    && oldItem == newItem

            return isSameEntry or isSameSeparator
        }

        override fun areContentsTheSame(oldItem: HistoryUiModel, newItem: HistoryUiModel) =
            oldItem == newItem
    }

    inner class HistoryEntryViewHolder(private val binding: ItemHistoryUseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: HistoryUiModel.EntryModel?) {
            entry?.let {
                binding.wineColorNameNaming.wineNaming.text =
                    DateFormatter.formatDate(it.historyEntry.date)
                binding.wineColorNameNaming.wineName.text = "Bonjour"
                binding.comment.text = "Une entrée dans l'historique"
                binding.friends.text = "3"
            }
        }
    }

    inner class HistorySeparatorViewHolder(private val binding: ItemHistorySeparatorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(header: HistoryUiModel.HeaedrModel?) {
            header?.let { binding.date.text = DateFormatter.formatDate(it.date) }
        }
    }

}
