package com.louis.app.cavity.ui.addbottle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemReviewMedalBinding
import com.louis.app.cavity.databinding.ItemReviewRateBinding
import com.louis.app.cavity.databinding.ItemReviewStarBinding
import com.louis.app.cavity.model.relation.FilledBottleReviewXRef
import com.louis.app.cavity.model.relation.FilledReviewAndReview

class FilledReviewRecyclerAdapter(
    val onValueChangedListener: (fReview: FilledBottleReviewXRef, checkedButtonId: Int) -> Unit,
    val onDeleteListener: (FilledBottleReviewXRef) -> Unit
) :
    ListAdapter<FilledReviewAndReview, FilledReviewRecyclerAdapter.BaseReviewViewHolder>(
        ReviewItemDiffCallback()
    ) {

    companion object {
        private const val TYPE_MEDAL = 0
        private const val TYPE_RATE = 1
        private const val TYPE_STAR = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseReviewViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TYPE_MEDAL -> MedalViewHolder(
                inflater.inflate(
                    R.layout.item_review_medal,
                    parent,
                    false
                )
            )
            TYPE_RATE -> RateViewHolder(
                inflater.inflate(
                    R.layout.item_review_rate,
                    parent,
                    false
                )
            )
            TYPE_STAR -> StarViewHolder(
                inflater.inflate(
                    R.layout.item_review_star,
                    parent,
                    false
                )
            )
            else -> throw IllegalStateException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: BaseReviewViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getItemId(position: Int): Long {
        return currentList[position].getId()
    }

    override fun getItemViewType(position: Int): Int {
        val (_, review) = currentList[position]

        return when (review.type) {
            0 -> TYPE_MEDAL
            1 -> TYPE_RATE
            2 -> TYPE_RATE
            3 -> TYPE_STAR
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    class ReviewItemDiffCallback : DiffUtil.ItemCallback<FilledReviewAndReview>() {
        override fun areItemsTheSame(
            oldItem: FilledReviewAndReview,
            newItem: FilledReviewAndReview
        ) =
            oldItem.getId() == newItem.getId()

        override fun areContentsTheSame(
            oldItem: FilledReviewAndReview,
            newItem: FilledReviewAndReview
        ) =
            oldItem == newItem
    }

    inner class MedalViewHolder(itemView: View) : BaseReviewViewHolder(itemView) {
        private val medalBinding = ItemReviewMedalBinding.bind(itemView)
        private val medalColors = listOf(
            ContextCompat.getColor(itemView.context, R.color.medal_bronze),
            ContextCompat.getColor(itemView.context, R.color.medal_silver),
            ContextCompat.getColor(itemView.context, R.color.medal_gold)
        )

        override fun bind(item: FilledReviewAndReview) = with(medalBinding) {
            val (fReview, review) = item

            contestName.text = review.contestName
            medal.setColorFilter(medalColors[fReview.value])

            medalBinding.rbGroupMedal.apply {
                clearOnButtonCheckedListeners()
                addOnButtonCheckedListener { _, checkedId, _ ->
                    onValueChangedListener(fReview, children.indexOfFirst { it.id == checkedId })
                }
            }

            deleteReview.setOnClickListener {
                onDeleteListener(fReview)
            }
        }
    }

    inner class RateViewHolder(itemView: View) : BaseReviewViewHolder(itemView) {
        private val rateBinding = ItemReviewRateBinding.bind(itemView)

        override fun bind(item: FilledReviewAndReview) = with(rateBinding) {
            val (fReview, review) = item
            val total = if (review.type == 1) 20 else 100

            contestName.text = review.contestName
            rateLayout.suffixText = "/$total"
            rate.setText(fReview.value.toString())

            deleteReview.setOnClickListener {
                onDeleteListener(fReview)
            }
        }
    }

    inner class StarViewHolder(itemView: View) : BaseReviewViewHolder(itemView) {
        private val starBinding = ItemReviewStarBinding.bind(itemView)

        override fun bind(item: FilledReviewAndReview) = with(starBinding) {
            val (fReview, review) = item

            contestName.text = review.contestName
            starCount.text = (fReview.value + 1).toString()

            starBinding.rbGroupStars.apply {
                clearOnButtonCheckedListeners()
                addOnButtonCheckedListener { _, checkedId, _ ->
                    onValueChangedListener(fReview, children.indexOfFirst { it.id == checkedId })
                }
            }

            deleteReview.setOnClickListener {
                onDeleteListener(fReview)
            }
        }
    }

    abstract class BaseReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: FilledReviewAndReview)
    }
}
