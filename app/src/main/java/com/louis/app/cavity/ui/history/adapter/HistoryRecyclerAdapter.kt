package com.louis.app.cavity.ui.history.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.paging.PagingDataAdapter
import androidx.paging.log
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemHistoryBinding
import com.louis.app.cavity.databinding.ItemHistorySeparatorBinding
import com.louis.app.cavity.domain.history.HistoryEntryType
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.history.HistoryUiModel
import com.louis.app.cavity.util.ColorUtil
import com.louis.app.cavity.util.DateFormatter
import com.louis.app.cavity.util.setVisible

class HistoryRecyclerAdapter(
    private val context: Context,
    private val colorUtil: ColorUtil,
    private val onHeaderClick: () -> Unit,
    private val onItemClick: (HistoryUiModel.EntryModel) -> Unit,
    private val onSwiped: (HistoryUiModel.EntryModel) -> Unit,
    private val onEditBottleComment: (HistoryUiModel.EntryModel, String) -> Unit,
) :
    PagingDataAdapter<HistoryUiModel, RecyclerView.ViewHolder>(HistoryEntryDiffItemCallback()) {

    companion object {
        const val TYPE_SEPARATOR = 0
        const val TYPE_NORMAL = 1
    }

    // Only lightweight drawables here
    private val drawables = mutableMapOf<Int, Drawable>()

    private fun getDrawable(@DrawableRes id: Int): Drawable? {
        return drawables[id] ?: ContextCompat.getDrawable(context, id)?.also { drawables[id] = it }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        when (holder) {
            is HistoryEntryViewHolder -> holder.bind(item as HistoryUiModel.EntryModel?)
            is HistorySeparatorViewHolder -> holder.bind(item as HistoryUiModel.HeaderModel?)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TYPE_SEPARATOR -> HistorySeparatorViewHolder(
                ItemHistorySeparatorBinding.inflate(inflater, parent, false)
            )
            /* TYPE_NORMAL */ else -> HistoryEntryViewHolder(
                ItemHistoryBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HistoryUiModel.HeaderModel -> TYPE_SEPARATOR
            is HistoryUiModel.EntryModel -> TYPE_NORMAL
            null -> TYPE_NORMAL
        }
    }

    class HistoryEntryDiffItemCallback : DiffUtil.ItemCallback<HistoryUiModel>() {
        override fun areItemsTheSame(oldItem: HistoryUiModel, newItem: HistoryUiModel): Boolean {
            val isSameEntry = oldItem is HistoryUiModel.EntryModel
                    && newItem is HistoryUiModel.EntryModel
                    && oldItem.model.historyEntry.id == newItem.model.historyEntry.id

            val isSameSeparator = oldItem is HistoryUiModel.HeaderModel
                    && newItem is HistoryUiModel.HeaderModel
                    && DateFormatter.roundToDay(oldItem.date) == DateFormatter.roundToDay(newItem.date)

            return isSameEntry || isSameSeparator
        }

        override fun areContentsTheSame(oldItem: HistoryUiModel, newItem: HistoryUiModel) =
            oldItem == newItem
    }

    inner class HistoryEntryViewHolder(private val binding: ItemHistoryBinding) :
        ReboundableViewHolder(binding) {

        fun bind(entry: HistoryUiModel.EntryModel?) {
            super.bind(entry ?: return)

            val historyEntry = entry.model.historyEntry
            val (markerColor, icon, label, _, showFriends) = historyEntry.getResources()
            val (bottle, wine) = entry.model.bottleAndWine
            val wineColor = ContextCompat.getColor(context, wine.color.colorRes)
            val colorCategory =
                if (historyEntry.type == HistoryEntryType.TASTING)
                    ColorUtil.ColorCategory.PRIMARY
                else
                    ColorUtil.ColorCategory.OTHER

            val resolvedMarkerColor = colorUtil.getColor(markerColor, colorCategory)

            with(binding) {
                wineColorNameNaming.wineColorIndicator.setColorFilter(wineColor)
                wineColorNameNaming.wineNaming.text = wine.naming
                wineColorNameNaming.wineName.text = wine.name
                vintage.text = bottle.vintage.toString()

                friends.setVisible(showFriends && entry.model.friends.isNotEmpty())
                friends.text = entry.model.friends.size.toString()

                marker.setBackgroundColor(resolvedMarkerColor)

                comment.apply {
                    when (historyEntry.type) {
                        HistoryEntryType.REMOVE, HistoryEntryType.TASTING -> {
                            val comment = historyEntry.comment

                            text = if (comment.isBlank()) {
                                alpha = 0.38f
                                setTextAppearance(R.style.TextAppearance_Cavity_Body2_Italic)
                                context.getString(R.string.no_description)
                            } else {
                                alpha = 1f
                                setTextAppearance(R.style.TextAppearance_Cavity_Body2)
                                comment
                            }
                        }

                        else -> {
                            alpha = 1f
                            setTextAppearance(R.style.TextAppearance_Cavity_Body2)

                            val data = if (historyEntry.type == HistoryEntryType.ADD) {
                                entry.model.bottleAndWine.bottle.buyLocation
                            } else {
                                entry.model.friends.joinToString(", ") { it.name }
                            }

                            text = context.getString(label, data)
                        }
                    }

                    setCompoundDrawablesWithIntrinsicBounds(getDrawable(icon), null, null, null)
                }

                cardView.setOnClickListener {
                    onItemClick(entry)
                }

                cardView.setOnLongClickListener {
                    val type = historyEntry.type
                    val emptyComment = historyEntry.comment.isBlank()
                    val isLongPressable =
                        (type == HistoryEntryType.REMOVE || type == HistoryEntryType.TASTING)

                    val fragment = binding.root.findFragment<Fragment>()
                    val editCommentDialog = SimpleInputDialog(
                        context,
                        fragment.layoutInflater,
                        fragment.viewLifecycleOwner
                    )
                    val dialogResource = SimpleInputDialog.DialogContent(
                        title = R.string.edit,
                        hint = R.string.tasting_comment,
                        icon = R.drawable.ic_edit
                    ) {
                        onEditBottleComment(entry, it)
                    }

                    if (!isLongPressable) {
                        return@setOnLongClickListener false
                    }

                    if (emptyComment) {
                        editCommentDialog.show(dialogResource)
                        return@setOnLongClickListener true
                    }

                    MaterialAlertDialogBuilder(context)
                        .setMessage(entry.model.historyEntry.comment)
                        .setPositiveButton(R.string.edit) { _, _ ->
                            editCommentDialog.showForEdit(
                                dialogResource,
                                entry.model.historyEntry.comment
                            )
                        }
                        .show()

                    return@setOnLongClickListener true
                }
            }
        }

        override fun onRebounded(position: Int) {
            onSwiped(getItem(position) as HistoryUiModel.EntryModel)
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
