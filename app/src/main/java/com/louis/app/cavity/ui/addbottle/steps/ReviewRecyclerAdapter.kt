package com.louis.app.cavity.ui.addbottle.steps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemReviewMedalBinding
import com.louis.app.cavity.databinding.ItemReviewRateBinding
import com.louis.app.cavity.databinding.ItemReviewStarBinding
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.util.toBoolean

class ReviewRecyclerAdapter(val onDeleteListener: (Review) -> Unit) :
    ListAdapter<Review, ReviewRecyclerAdapter.BaseReviewViewHolder>(ReviewItemDiffCallback()) {

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
        when (holder) {
            is MedalViewHolder -> holder.bind(currentList[position])
            is RateViewHolder -> holder.bind(currentList[position])
            is StarViewHolder -> holder.bind(currentList[position])
        }
    }

    override fun getItemId(position: Int): Long {
        return currentList[position].contestName.hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        val review = currentList[position]

        return when {
            review.isMedal.toBoolean() -> TYPE_MEDAL
            review.isRate20.toBoolean() or review.isRate100.toBoolean() -> TYPE_RATE
            review.isStar.toBoolean() -> TYPE_STAR
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    class ReviewItemDiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review) =
            oldItem.contestName == newItem.contestName

        override fun areContentsTheSame(oldItem: Review, newItem: Review) =
            oldItem == newItem
    }

    inner class MedalViewHolder(itemView: View) : BaseReviewViewHolder(itemView) {
        private val bindingMedal = ItemReviewMedalBinding.bind(itemView)
        private val medalColors = listOf(
            ContextCompat.getColor(itemView.context, R.color.medal_bronze),
            ContextCompat.getColor(itemView.context, R.color.medal_silver),
            ContextCompat.getColor(itemView.context, R.color.medal_gold)
        )

        override fun bind(review: Review) = with(bindingMedal) {
            contestName.text = review.contestName
            //medal.setColorFilter(medalColors[review.value])
            deleteReview.setOnClickListener {
                onDeleteListener(review)
            }
        }
    }

    inner class RateViewHolder(itemView: View) : BaseReviewViewHolder(itemView) {
        private val bindingRate = ItemReviewRateBinding.bind(itemView)

        override fun bind(review: Review) = with(bindingRate) {
            contestName.text = review.contestName
            val total = if (review.isRate20.toBoolean()) 20 else 100
            //rate.text = itemView.context.getString(R.string.item_rate, review.value, total)
            deleteReview.setOnClickListener {
                onDeleteListener(review)
            }
        }
    }

    inner class StarViewHolder(itemView: View) : BaseReviewViewHolder(itemView) {
        private val bindingStar = ItemReviewStarBinding.bind(itemView)

        override fun bind(review: Review) = with(bindingStar) {
            contestName.text = review.contestName
            //starCount.text = (review.value + 1).toString()
            deleteReview.setOnClickListener {
                onDeleteListener(review)
            }
        }
    }

    abstract class BaseReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(review: Review)
    }
}
