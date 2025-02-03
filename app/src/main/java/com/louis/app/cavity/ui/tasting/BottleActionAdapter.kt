package com.louis.app.cavity.ui.tasting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.louis.app.cavity.databinding.ItemTastingBottleActionsBinding
import com.louis.app.cavity.db.dao.BottleWithTastingActions
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.TastingAction

class BottleActionAdapter(
    private val onActionCheckedChange: (TastingAction, Boolean) -> Unit,
    private val onCommentChanged: (Bottle, String) -> Unit,
    private val onCloseIconClicked: (Bottle) -> Unit
) :
    ListAdapter<BottleWithTastingActions, BottleActionViewHolder>
        (BottleWithTastingActionsItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottleActionViewHolder {
        val binding =
            ItemTastingBottleActionsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return BottleActionViewHolder(
            binding,
            onActionCheckedChange,
            onCommentChanged,
            onCloseIconClicked
        )
    }

    override fun onBindViewHolder(holder: BottleActionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: BottleActionViewHolder) {
        holder.clearTextWatcher()
        super.onViewRecycled(holder)
    }

    class BottleWithTastingActionsItemDiffCallback :
        DiffUtil.ItemCallback<BottleWithTastingActions>() {
        override fun areItemsTheSame(
            oldItem: BottleWithTastingActions,
            newItem: BottleWithTastingActions
        ) =
            oldItem.bottle.id == newItem.bottle.id

        override fun areContentsTheSame(
            oldItem: BottleWithTastingActions,
            newItem: BottleWithTastingActions
        ) =
            true
    }
}
