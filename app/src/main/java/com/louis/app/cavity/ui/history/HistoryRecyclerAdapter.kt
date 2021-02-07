package com.louis.app.cavity.ui.history

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemHistorySeparatorBinding
import com.louis.app.cavity.databinding.ItemHistoryTasteBinding
import com.louis.app.cavity.databinding.ItemHistoryUseBinding
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.relation.history.HistoryEntryWithBottleAndTastingAndFriends
import com.louis.app.cavity.util.DateFormatter
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.setVisible

class HistoryRecyclerAdapter :
    PagingDataAdapter<HistoryUiModel, RecyclerView.ViewHolder>(
        HistoryEntryDiffItemCallback()
    ) {

    companion object {
        const val TYPE_SEPARATOR = 0
        const val TYPE_NORMAL = 1
        const val TYPE_TASTING = 2
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        when(holder) {
            is HistoryEntryViewHolder -> holder.bind(item as HistoryUiModel.EntryModel?)
            is HistoryEntryTasteViewHolder -> holder.bind(item as HistoryUiModel.EntryModel?)
            is HistorySeparatorViewHolder -> holder.bind(item as HistoryUiModel.HeaderModel?)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TYPE_SEPARATOR -> HistorySeparatorViewHolder(
                ItemHistorySeparatorBinding.inflate(inflater, parent, false)
            )
            TYPE_NORMAL -> HistoryEntryViewHolder(
                ItemHistoryUseBinding.inflate(inflater, parent, false)
            )
            else -> HistoryEntryTasteViewHolder(
                ItemHistoryTasteBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is HistoryUiModel.HeaderModel -> TYPE_SEPARATOR
            is HistoryUiModel.EntryModel ->
                if (item.model.getHistoryType() == HistoryTypes.HISTORY_TASTING)
                    TYPE_TASTING else TYPE_NORMAL
            else -> throw IllegalStateException("Unknown view type")
        }
    }

    class HistoryEntryDiffItemCallback : DiffUtil.ItemCallback<HistoryUiModel>() {
        override fun areItemsTheSame(oldItem: HistoryUiModel, newItem: HistoryUiModel): Boolean {
            val isSameEntry = oldItem is HistoryUiModel.EntryModel
                    && newItem is HistoryUiModel.EntryModel
                    && oldItem.model.historyEntry.id == newItem.model.historyEntry.id

            val isSameSeparator = oldItem is HistoryUiModel.HeaderModel
                    && newItem is HistoryUiModel.HeaderModel
                    && oldItem == newItem

            return isSameEntry or isSameSeparator
        }

        override fun areContentsTheSame(oldItem: HistoryUiModel, newItem: HistoryUiModel) =
            oldItem == newItem
    }

    inner class HistoryEntryViewHolder(private val binding: ItemHistoryUseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { }
        }

        fun bind(entry: HistoryUiModel.EntryModel?) {
            entry?.let {
                val (bottle, wine) = it.model.bottleAndWine

                with(binding) {
                    wineColorNameNaming.wineNaming.text = wine.naming
                    wineColorNameNaming.wineName.text = wine.name
                    vintage.text = bottle.vintage.toString()

                    when (it.model.getHistoryType()) {
                        HistoryTypes.HISTORY_USE -> bindForUse(it.model)
                        HistoryTypes.HISTORY_REPLENISHMENT -> bindForReplenishment(it.model)
                        HistoryTypes.HISTORY_GIFTED_TO -> bindForGiftedTo(it.model)
                        else -> bindForGiftedBy(it.model)
                    }
                }

            }
        }

        private fun bindForUse(item: HistoryEntryWithBottleAndTastingAndFriends) {
            with(binding) {
                bottles.setVisible(false)
                friends.setVisible(true)
                wineColorNameNaming.wineColorIndicator.setVisible(true)
                marker.background = ColorDrawable(root.context.getColor(R.color.cavity_red))

                comment.text = item.bottleAndWine.bottle.tasteComment
                friends.text = item.friends.size.toString()
            }
        }

        private fun bindForReplenishment(item: HistoryEntryWithBottleAndTastingAndFriends) {
            with(binding) {
                bottles.setVisible(false)
                friends.setVisible(false)
                wineColorNameNaming.wineColorIndicator.setVisible(true)
                comment.text = item.bottleAndWine.bottle.buyLocation
                marker.background = ColorDrawable(root.context.getColor(R.color.cavity_light_green))

            }
        }

        private fun bindForGiftedTo(item: HistoryEntryWithBottleAndTastingAndFriends) {
            with(binding) {
                bottles.setVisible(false)
                friends.setVisible(false)
                wineColorNameNaming.wineColorIndicator.setVisible(true)
                comment.text = root.context.getString(R.string.gifted_to, item.friends[0].firstName)
                marker.background = ColorDrawable(root.context.getColor(R.color.cavity_red))
            }
        }

        private fun bindForGiftedBy(item: HistoryEntryWithBottleAndTastingAndFriends) {
            with(binding) {
                bottles.setVisible(false)
                friends.setVisible(false)
                wineColorNameNaming.wineColorIndicator.setVisible(true)
                comment.text = root.context.getString(R.string.gifted_by, item.friends[0].firstName)
                marker.background = ColorDrawable(root.context.getColor(R.color.cavity_light_green))
            }
        }
    }

    inner class HistoryEntryTasteViewHolder(private val binding: ItemHistoryTasteBinding) :
        RecyclerView.ViewHolder(binding.root) {

            fun bind(entry: HistoryUiModel.EntryModel?) {
                with(binding) {
                    title.text = entry?.model?.tasting?.tasting?.opportunity
                    bottles.text = entry?.model?.tasting?.bottles?.size?.toString()
                }
            }
    }

    inner class HistorySeparatorViewHolder(private val binding: ItemHistorySeparatorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(header: HistoryUiModel.HeaderModel?) {
            header?.let { binding.date.text = DateFormatter.formatDate(it.date) }
        }
    }
}
