package com.louis.app.cavity.ui.bottle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemShowReviewMedalBinding
import com.louis.app.cavity.databinding.ItemShowReviewRateBinding
import com.louis.app.cavity.databinding.ItemShowReviewStarBinding
import com.louis.app.cavity.db.dao.FReviewAndReview


class ShowFilledReviewsRecyclerAdapter :
    ListAdapter<FReviewAndReview, ShowFilledReviewsRecyclerAdapter.BaseReviewViewHolder>(
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
                    R.layout.item_show_review_medal,
                    parent,
                    false
                )
            )
            TYPE_RATE -> RateViewHolder(
                inflater.inflate(
                    R.layout.item_show_review_rate,
                    parent,
                    false
                )
            )
            TYPE_STAR -> StarViewHolder(
                inflater.inflate(
                    R.layout.item_show_review_star,
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

    override fun getItemId(position: Int) = currentList[position].review.id

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

    class ReviewItemDiffCallback : DiffUtil.ItemCallback<FReviewAndReview>() {
        override fun areItemsTheSame(oldItem: FReviewAndReview, newItem: FReviewAndReview) =
            oldItem.review.id == newItem.review.id

        override fun areContentsTheSame(oldItem: FReviewAndReview, newItem: FReviewAndReview) =
            oldItem == newItem
    }

    inner class MedalViewHolder(itemView: View) : BaseReviewViewHolder(itemView) {
        private val medalBinding = ItemShowReviewMedalBinding.bind(itemView)
        private val medalColors = listOf(
            ContextCompat.getColor(itemView.context, R.color.medal_bronze),
            ContextCompat.getColor(itemView.context, R.color.medal_silver),
            ContextCompat.getColor(itemView.context, R.color.medal_gold)
        )

        override fun bind(item: FReviewAndReview) = with(medalBinding) {
            val (fReview, review) = item

            contestName.text = review.contestName
            medal.setColorFilter(medalColors[fReview.value])
        }
    }

    inner class RateViewHolder(itemView: View) : BaseReviewViewHolder(itemView) {
        private val rateBinding = ItemShowReviewRateBinding.bind(itemView)

        override fun bind(item: FReviewAndReview) = with(rateBinding) {
            val (fReview, review) = item
            val total = if (review.type == 1) 20 else 100

            contestName.text = review.contestName
            rate.text = itemView.context.getString(R.string.item_rate, fReview.value, total)
        }
    }

    inner class StarViewHolder(itemView: View) : BaseReviewViewHolder(itemView) {
        private val starBinding = ItemShowReviewStarBinding.bind(itemView)

        override fun bind(item: FReviewAndReview) = with(starBinding) {
            val (fReview, review) = item

            contestName.text = review.contestName
            starCount.text = (fReview.value + 1).toString()
        }
    }

    abstract class BaseReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: FReviewAndReview)
    }
}
