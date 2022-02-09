package com.louis.app.cavity.ui.history.adapter

import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.louis.app.cavity.databinding.ItemHistoryTasteBinding
import com.louis.app.cavity.databinding.ItemHistoryUseBinding
import com.louis.app.cavity.ui.history.HistoryUiModel
import com.louis.app.cavity.util.toBoolean
import kotlin.math.abs

abstract class ReboundableViewHolder(private val binding: ViewBinding) :
    RecyclerView.ViewHolder(binding.root),
    ReboundingSwipeActionCallback.ReboundableViewHolder {

    override val reboundableView = when (binding) {
        is ItemHistoryUseBinding -> binding.cardView
        is ItemHistoryTasteBinding -> binding.cardView
        else -> throw IllegalArgumentException(
            "Cannot use this binding instance to make it reboundable."
        )
    }

    init {
        val resources = itemView.resources
        val theme = itemView.context.theme

        binding.root.background = HistorySwipeActionDrawable(resources, theme)
    }

    @CallSuper
    open fun bind(entry: HistoryUiModel) {
        if (entry is HistoryUiModel.EntryModel) {
            val isFavorite = entry.model.historyEntry.favorite.toBoolean()
            binding.root.isActivated = isFavorite

            // Call to Drawable#jumpToCurrentState() isn't necessary here.
            // View#jumpDrawablesToCurrentState() will be called on our view by the
            // View#onAttachedToWindow() framework callback when the view is added (recycled).

            updateCorner(if (isFavorite) 1f else 0f)
        }

        // TODO: Tasting
    }

    private fun updateCorner(interpolation: Float) {
        reboundableView.progress = interpolation
    }

    override fun onReboundOffsetChanged(
        currentSwipeDistance: Int,
        swipeThreshold: Int,
        currentTargetHasMetThresholdOnce: Boolean
    ) {
        if (currentTargetHasMetThresholdOnce) return

        val isStarred = binding.root.isActivated
        val interpolation = (currentSwipeDistance / swipeThreshold.toFloat()).coerceIn(0F, 1F)
        val adjustedInterpolation = abs((if (isStarred) 1F else 0F) - interpolation)
        updateCorner(adjustedInterpolation)

        val thresholdMet = currentSwipeDistance >= swipeThreshold
        val shouldStar = when {
            thresholdMet && isStarred -> false
            thresholdMet && !isStarred -> true
            else -> return
        }

        binding.root.isActivated = shouldStar
    }
}
