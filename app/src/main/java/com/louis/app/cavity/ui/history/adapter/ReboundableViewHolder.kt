package com.louis.app.cavity.ui.history.adapter

import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.card.MaterialCardView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemHistoryTasteBinding
import com.louis.app.cavity.databinding.ItemHistoryUseBinding
import com.louis.app.cavity.ui.history.HistoryUiModel
import com.louis.app.cavity.util.toBoolean
import kotlin.math.abs

abstract class ReboundableViewHolder(private val binding: ViewBinding) :
    RecyclerView.ViewHolder(binding.root),
    ReboundingSwipeActionCallback.ReboundableViewHolder {

    private val starredCornerSize = itemView.resources.getDimension(R.dimen.starred_corner_size)
    private var isStarred = false

    override val reboundableView = when (binding) {
        is ItemHistoryUseBinding -> binding.cardView
        is ItemHistoryTasteBinding -> binding.cardView
        else -> binding.root
    }

    init {
        binding.root.background = HistorySwipeActionDrawable().apply {
            initResources(itemView.resources, itemView.context.theme)
        }
    }

    @CallSuper
    open fun bind(entry: HistoryUiModel) {
        if (entry is HistoryUiModel.EntryModel) {
            val isFavorite = entry.model.historyEntry.favorite.toBoolean()
                .also { isStarred = it; binding.root.isActivated }

            updateCorner(if (isFavorite) 1f else 0f)
        }
    }

    private fun updateCorner(interpolation: Float) {
        (reboundableView as MaterialCardView).progress = interpolation
    }

    override fun onReboundOffsetChanged(
        currentSwipePercentage: Float,
        swipeThreshold: Float,
        currentTargetHasMetThresholdOnce: Boolean
    ) {
        if (currentTargetHasMetThresholdOnce) return

        val interpolation = (currentSwipePercentage / swipeThreshold).coerceIn(0F, 1F)
        val adjustedInterpolation = abs((if (isStarred) 1F else 0F) - interpolation)
        updateCorner(adjustedInterpolation)

        val thresholdMet = currentSwipePercentage >= swipeThreshold
        val shouldStar = when {
            thresholdMet && isStarred -> false
            thresholdMet && !isStarred -> true
            else -> return
        }

        binding.root.isActivated = shouldStar
    }
}
