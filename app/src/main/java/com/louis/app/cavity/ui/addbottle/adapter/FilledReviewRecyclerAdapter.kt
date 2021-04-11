package com.louis.app.cavity.ui.addbottle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Checkable
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemReviewMedalBinding
import com.louis.app.cavity.databinding.ItemReviewRateBinding
import com.louis.app.cavity.databinding.ItemReviewStarBinding
import com.louis.app.cavity.ui.addbottle.viewmodel.FReviewUiModel
import com.louis.app.cavity.ui.widget.Rule

class FilledReviewRecyclerAdapter(
    val onValueChangedListener: (fReview: FReviewUiModel, checkedButtonIdOrRate: Int) -> Unit,
    val onDeleteListener: (FReviewUiModel) -> Unit
) :
    ListAdapter<FReviewUiModel, FilledReviewRecyclerAdapter.BaseReviewViewHolder>(
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
            TYPE_MEDAL -> MedalViewHolder(ItemReviewMedalBinding.inflate(inflater, parent, false))
            TYPE_RATE -> RateViewHolder(ItemReviewRateBinding.inflate(inflater, parent, false))
            TYPE_STAR -> StarViewHolder(ItemReviewStarBinding.inflate(inflater, parent, false))
            else -> throw IllegalStateException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: BaseReviewViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getItemId(position: Int) = currentList[position].reviewId

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position].type) {
            0 -> TYPE_MEDAL
            1 -> TYPE_RATE
            2 -> TYPE_RATE
            3 -> TYPE_STAR
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    class ReviewItemDiffCallback : DiffUtil.ItemCallback<FReviewUiModel>() {
        override fun areItemsTheSame(oldItem: FReviewUiModel, newItem: FReviewUiModel) =
            oldItem.reviewId == newItem.reviewId

        override fun areContentsTheSame(oldItem: FReviewUiModel, newItem: FReviewUiModel) =
            oldItem == newItem
    }

    inner class MedalViewHolder(private val binding: ItemReviewMedalBinding) :
        BaseReviewViewHolder(binding) {

        private val medalColors = listOf(
            ContextCompat.getColor(itemView.context, R.color.medal_bronze),
            ContextCompat.getColor(itemView.context, R.color.medal_silver),
            ContextCompat.getColor(itemView.context, R.color.medal_gold)
        )

        override fun bind(item: FReviewUiModel) = with(binding) {
            contestName.text = item.name
            medal.setColorFilter(medalColors[item.value])

            rbGroupMedal.apply {
                clearOnButtonCheckedListeners()

                val v = getChildAt(item.value)
                (v as Checkable).isChecked = true

                addOnButtonCheckedListener { _, checkedId, isChecked ->
                    if (isChecked) {
                        val contestValue = children.indexOfFirst { it.id == checkedId }
                        onValueChangedListener(item, contestValue)
                    }
                }
            }

            deleteReview.setOnClickListener {
                onDeleteListener(item)
            }
        }
    }

    inner class RateViewHolder(private val binding: ItemReviewRateBinding) :
        BaseReviewViewHolder(binding) {

        private val watcher = binding.rate.doAfterTextChanged {
            if (binding.rateLayout.validate()) {
                onValueChangedListener(currentList[bindingAdapterPosition], it.toString().toInt())
            }
        }

        override fun bind(item: FReviewUiModel) = with(binding) {
            val total = if (item.type == 1) 20 else 100

            rate.removeTextChangedListener(watcher)
            contestName.text = item.name

            rateLayout.apply {
                val rule = Rule(R.string.base_error) {
                    it.toInt() in 0..total
                }

                suffixText = "/$total"

                clearRules(clearDefaultRules = false)
                addRules(rule)
            }

            rate.apply {
                setText(item.value.toString())
                setSelection(item.value.toString().length)
                addTextChangedListener(watcher)
            }

            deleteReview.setOnClickListener {
                onDeleteListener(item)
            }
        }
    }

    inner class StarViewHolder(private val binding: ItemReviewStarBinding) :
        BaseReviewViewHolder(binding) {

        override fun bind(item: FReviewUiModel) = with(binding) {
            contestName.text = item.name
            starCount.text = (item.value + 1).toString()

            rbGroupStars.apply {
                clearOnButtonCheckedListeners()

                val v = getChildAt(item.value)
                (v as Checkable).isChecked = true

                addOnButtonCheckedListener { _, checkedId, isChecked ->
                    if (isChecked) {
                        val contestValue = children.indexOfFirst { it.id == checkedId }
                        onValueChangedListener(item, contestValue)
                    }
                }
            }

            deleteReview.setOnClickListener {
                onDeleteListener(item)
            }
        }
    }

    abstract class BaseReviewViewHolder(binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        abstract fun bind(item: FReviewUiModel)
    }
}
