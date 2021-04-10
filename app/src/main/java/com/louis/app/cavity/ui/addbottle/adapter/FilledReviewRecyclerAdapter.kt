package com.louis.app.cavity.ui.addbottle.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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

//    override fun getItemId(position: Int) = currentList[position].getId()

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
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: FReviewUiModel, newItem: FReviewUiModel) =
            oldItem == newItem
    }

    inner class MedalViewHolder(itemView: View) : BaseReviewViewHolder(itemView) {
        private val medalBinding = ItemReviewMedalBinding.bind(itemView)
        private val medalColors = listOf(
            ContextCompat.getColor(itemView.context, R.color.medal_bronze),
            ContextCompat.getColor(itemView.context, R.color.medal_silver),
            ContextCompat.getColor(itemView.context, R.color.medal_gold)
        )

        override fun bind(item: FReviewUiModel) = with(medalBinding) {
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

    inner class RateViewHolder(itemView: View) : BaseReviewViewHolder(itemView) {
        private val rateBinding = ItemReviewRateBinding.bind(itemView)

        override fun bind(item: FReviewUiModel) = with(rateBinding) {
            val watcher = object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (rateBinding.rateLayout.validate()) {
                        onValueChangedListener(item, text.toString().toInt())
                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            }

            val total = if (item.type == 1) 20 else 100

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
                removeTextChangedListener(watcher)
                setText(item.value.toString())
                setSelection(item.value.toString().length)
                addTextChangedListener(watcher)
            }

            deleteReview.setOnClickListener {
                onDeleteListener(item)
            }
        }
    }

    inner class StarViewHolder(itemView: View) : BaseReviewViewHolder(itemView) {
        private val starBinding = ItemReviewStarBinding.bind(itemView)

        override fun bind(item: FReviewUiModel) = with(starBinding) {
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

    abstract class BaseReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: FReviewUiModel)
    }
}
