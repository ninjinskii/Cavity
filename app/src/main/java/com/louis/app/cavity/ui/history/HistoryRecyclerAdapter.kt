package com.louis.app.cavity.ui.history

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemHistorySeparatorBinding
import com.louis.app.cavity.databinding.ItemHistoryUseBinding
import com.louis.app.cavity.model.relation.history.HistoryEntryWithBottleAndTastingAndFriends
import com.louis.app.cavity.util.DateFormatter
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.setVisible

class HistoryRecyclerAdapter :
    PagingDataAdapter<HistoryUiModel, RecyclerView.ViewHolder>(
        HistoryEntryDiffItemCallback()
    ) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        if (holder is HistoryEntryViewHolder) {
            holder.bind(item as HistoryUiModel.EntryModel?)
        } else if (holder is HistorySeparatorViewHolder) {
            holder.bind(item as HistoryUiModel.HeaderModel)
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
        is HistoryUiModel.HeaderModel -> 1
        else -> throw IllegalStateException("Unknown view type")
    }

    class HistoryEntryDiffItemCallback : DiffUtil.ItemCallback<HistoryUiModel>() {
        override fun areItemsTheSame(oldItem: HistoryUiModel, newItem: HistoryUiModel): Boolean {
            val isSameEntry = oldItem is HistoryUiModel.EntryModel
                    && newItem is HistoryUiModel.EntryModel
                    && oldItem.item.historyEntry.id == newItem.item.historyEntry.id

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
                val (bottle, wine) = it.item.bottleAndWine

                with(binding) {
                    wineColorNameNaming.wineNaming.text = wine.naming
                    wineColorNameNaming.wineName.text = wine.name
                    vintage.text = bottle.vintage.toString()

                    when (it.item.getType()) {
                        HistoryEntryTypes.TYPE_USE -> bindForUse(it.item)
                        HistoryEntryTypes.TYPE_REPLENISHMENT -> bindForReplenishment(it.item)
                        HistoryEntryTypes.TYPE_GIFTED_TO -> bindForGiftedTo(it.item)
                        HistoryEntryTypes.TYPE_GIFTED_BY -> bindForGiftedBy(it.item)
                        HistoryEntryTypes.TYPE_TASTING -> bindForTasting(it.item)
                    }
                }

            }
        }

        private fun bindForUse(item: HistoryEntryWithBottleAndTastingAndFriends) {
            with(binding) {
                bottles.setVisible(false)
                friends.setVisible(true)
                title.setVisible(false)
                marker.background = ColorDrawable(root.context.getColor(R.color.cavity_red))

                comment.text = item.bottleAndWine.bottle.tasteComment
                friends.text = item.friends.size.toString()
            }
        }

        private fun bindForReplenishment(item: HistoryEntryWithBottleAndTastingAndFriends) {
            with(binding) {
                bottles.setVisible(false)
                friends.setVisible(false)
                title.setVisible(false)
                comment.text = item.bottleAndWine.bottle.buyLocation
                marker.background = ColorDrawable(root.context.getColor(R.color.cavity_light_green))

            }
        }

        private fun bindForGiftedTo(item: HistoryEntryWithBottleAndTastingAndFriends) {
            with(binding) {
                bottles.setVisible(false)
                friends.setVisible(false)
                title.setVisible(false)
                comment.text = root.context.getString(R.string.gifted_to, item.friends[0].firstName)
                marker.background = ColorDrawable(root.context.getColor(R.color.cavity_red))
            }
        }

        private fun bindForGiftedBy(item: HistoryEntryWithBottleAndTastingAndFriends) {
            with(binding) {
                bottles.setVisible(false)
                friends.setVisible(false)
                title.setVisible(false)
                comment.text = root.context.getString(R.string.gifted_by, item.friends[0].firstName)
                marker.background = ColorDrawable(root.context.getColor(R.color.cavity_light_green))
            }
        }

        private fun bindForTasting(item: HistoryEntryWithBottleAndTastingAndFriends) {
            with(binding) {
                bottles.setVisible(true)
                friends.setVisible(true)
                title.setVisible(true)
                wineColorNameNaming.wineColorIndicator.setVisible(false)
                title.text = item.tasting?.tasting?.opportunity
                marker.background = ColorDrawable(root.context.getColor(R.color.cavity_gold))
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
