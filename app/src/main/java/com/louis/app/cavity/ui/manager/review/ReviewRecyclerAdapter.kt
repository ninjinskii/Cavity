package com.louis.app.cavity.ui.manager.review

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemReviewManagerBinding
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.model.relation.ReviewWithFilledReviews

class ReviewRecyclerAdapter(
    private val onRename: (Review) -> Unit,
    private val onDelete: (Review) -> Unit
) :
    ListAdapter<ReviewWithFilledReviews, ReviewRecyclerAdapter.ReviewViewHolder>(
        ReviewItemDiffCallback()
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReviewViewHolder {
        val binding =
            ItemReviewManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReviewItemDiffCallback : DiffUtil.ItemCallback<ReviewWithFilledReviews>() {
        override fun areItemsTheSame(
            oldItem: ReviewWithFilledReviews,
            newItem: ReviewWithFilledReviews
        ): Boolean {
            return oldItem.review.reviewId == newItem.review.reviewId
        }

        override fun areContentsTheSame(
            oldItem: ReviewWithFilledReviews,
            newItem: ReviewWithFilledReviews
        ): Boolean {
            return oldItem.review == newItem.review && oldItem.fReview.size == newItem.fReview.size
        }
    }

    inner class ReviewViewHolder(private val binding: ItemReviewManagerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        init {
            binding.buttonOptions.setOnClickListener {
                showPopup(it)
            }
        }

        fun bind(reviewWithFilledReviews: ReviewWithFilledReviews) {
            val (review, fReview) = reviewWithFilledReviews

            with(binding) {
                reviewName.text = review.contestName
                bottleCount.text = context.resources.getQuantityString(
                    R.plurals.bottles,
                    fReview.size,
                    fReview.size
                )
            }
        }

        private fun showPopup(view: View) {
            val grape = getItem(adapterPosition).review

            PopupMenu(context, view).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) setForceShowIcon(true)

                menuInflater.inflate(R.menu.rename_delete_menu, menu)

                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.edit_item -> onRename(grape)
                        R.id.delete_item -> onDelete(grape)
                    }
                    true
                }
                show()
            }
        }
    }
}
