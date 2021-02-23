package com.louis.app.cavity.ui.history

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemHistorySeparatorBinding
import com.louis.app.cavity.databinding.ItemHistoryTasteBinding
import com.louis.app.cavity.databinding.ItemHistoryUseBinding
import com.louis.app.cavity.model.HistoryEntryType
import com.louis.app.cavity.model.relation.history.HistoryEntryWithBottleAndTastingAndFriends
import com.louis.app.cavity.util.DateFormatter
import com.louis.app.cavity.util.setVisible

class HistoryRecyclerAdapter(context: Context, private val onHeaderClick: () -> Unit) :
    PagingDataAdapter<HistoryUiModel, RecyclerView.ViewHolder>(
        HistoryEntryDiffItemCallback()
    ) {

    companion object {
        const val TYPE_SEPARATOR = 0
        const val TYPE_NORMAL = 1
        const val TYPE_TASTING = 2
    }

    val redMarker = ColorDrawable(context.getColor(R.color.cavity_red))
    val greenMarker = ColorDrawable(context.getColor(R.color.cavity_light_green))

    val glassIcon = ContextCompat.getDrawable(context, R.drawable.ic_glass)
    val bottleIcon = ContextCompat.getDrawable(context, R.drawable.ic_bottle)
    val giftIcon = ContextCompat.getDrawable(context, R.drawable.ic_gift)
    val tastingIcon = ContextCompat.getDrawable(context, R.drawable.ic_toast_wine)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        when (holder) {
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
                if (item.model.historyEntry.type == HistoryEntryType.TYPE_TASTING)
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

                    when (it.model.historyEntry.type) {
                        HistoryEntryType.TYPE_CONSUME -> bindForConsume(it.model)
                        HistoryEntryType.TYPE_REPLENISHMENT -> bindForReplenishment(it.model)
                        HistoryEntryType.TYPE_GIFTED_TO -> bindForGift(it.model, to = true)
                        else -> bindForGift(it.model, to = false)
                    }
                }

            }
        }

        private fun bindForConsume(item: HistoryEntryWithBottleAndTastingAndFriends) {
            with(binding) {
                bottles.setVisible(false)
                friends.setVisible(true)
                wineColorNameNaming.wineColorIndicator.setVisible(true)
                marker.background = redMarker

                comment.apply {
                    if (item.historyEntry.comment.isBlank()) {
                        setTypeface(null, Typeface.ITALIC)
                        text = context.getString(R.string.no_description)
                    } else {
                        typeface = Typeface.DEFAULT
                        text = item.historyEntry.comment
                    }
                }

                comment.setCompoundDrawablesWithIntrinsicBounds(glassIcon, null, null, null)
                friends.text = item.friends.size.toString()
            }
        }

        private fun bindForReplenishment(item: HistoryEntryWithBottleAndTastingAndFriends) {
            with(binding) {
                bottles.setVisible(false)
                friends.setVisible(false)
                wineColorNameNaming.wineColorIndicator.setVisible(true)
                marker.background = greenMarker

                comment.apply {
                    text =
                        context.getString(R.string.buyed_at, item.bottleAndWine.bottle.buyLocation)
                    setCompoundDrawablesWithIntrinsicBounds(bottleIcon, null, null, null)
                    typeface = Typeface.DEFAULT
                }
            }
        }

        private fun bindForGift(item: HistoryEntryWithBottleAndTastingAndFriends, to: Boolean) {
            with(binding) {
                bottles.setVisible(false)
                friends.setVisible(false)
                wineColorNameNaming.wineColorIndicator.setVisible(true)
                marker.background = if(to) redMarker else greenMarker

                comment.apply {
                    val label = if(to) R.string.gifted_to_someone else R.string.gifted_by_someone
                    text = context.getString(label, item.friends[0].name)
                    setCompoundDrawablesWithIntrinsicBounds(giftIcon, null, null, null)
                    typeface = Typeface.DEFAULT
                }
            }
        }
    }

    inner class HistoryEntryTasteViewHolder(private val binding: ItemHistoryTasteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { }
        }

        fun bind(entry: HistoryUiModel.EntryModel?) {
            with(binding) {
                bottles.setVisible(true)
                friends.setVisible(true)

                title.text = entry?.model?.tasting?.tasting?.opportunity
                bottles.text = entry?.model?.tasting?.bottles?.size?.toString()
                comment.setCompoundDrawablesWithIntrinsicBounds(tastingIcon, null, null, null)
            }
        }
    }

    inner class HistorySeparatorViewHolder(private val binding: ItemHistorySeparatorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { onHeaderClick() }
        }

        fun bind(header: HistoryUiModel.HeaderModel?) {
            header?.let { binding.date.text = DateFormatter.formatDate(it.date) }
        }
    }
}
