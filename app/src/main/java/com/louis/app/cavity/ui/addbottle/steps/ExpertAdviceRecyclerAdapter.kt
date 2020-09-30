package com.louis.app.cavity.ui.addbottle.steps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemExpertAdviceMedalBinding
import com.louis.app.cavity.databinding.ItemExpertAdviceRateBinding
import com.louis.app.cavity.databinding.ItemExpertAdviceStarBinding
import com.louis.app.cavity.model.ExpertAdvice
import com.louis.app.cavity.util.toBoolean

class ExpertAdviceRecyclerAdapter(val onDeleteListener: (ExpertAdvice) -> Unit) :
    ListAdapter<ExpertAdvice, ExpertAdviceRecyclerAdapter.BaseAdviceViewHolder>(
        ExpertAdviceItemDiffCallback()
    ) {

    companion object {
        private const val TYPE_MEDAL = 0
        private const val TYPE_RATE = 1
        private const val TYPE_STAR = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseAdviceViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TYPE_MEDAL -> MedalViewHolder(
                inflater.inflate(
                    R.layout.item_expert_advice_medal,
                    parent,
                    false
                )
            )
            TYPE_RATE -> RateViewHolder(
                inflater.inflate(
                    R.layout.item_expert_advice_rate,
                    parent,
                    false
                )
            )
            TYPE_STAR -> StarViewHolder(
                inflater.inflate(
                    R.layout.item_expert_advice_star,
                    parent,
                    false
                )
            )
            else -> throw IllegalStateException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: BaseAdviceViewHolder, position: Int) {
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
        val advice = currentList[position]

        return when {
            advice.isMedal.toBoolean() -> TYPE_MEDAL
            advice.isRate20.toBoolean() or advice.isRate100.toBoolean() -> TYPE_RATE
            advice.isStar.toBoolean() -> TYPE_STAR
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    class ExpertAdviceItemDiffCallback : DiffUtil.ItemCallback<ExpertAdvice>() {
        override fun areItemsTheSame(oldItem: ExpertAdvice, newItem: ExpertAdvice) =
            oldItem.contestName == newItem.contestName

        override fun areContentsTheSame(oldItem: ExpertAdvice, newItem: ExpertAdvice) =
            oldItem == newItem
    }

    inner class MedalViewHolder(itemView: View) : BaseAdviceViewHolder(itemView) {
        private val bindingMedal = ItemExpertAdviceMedalBinding.bind(itemView)
        private val medalColors = listOf(
            ContextCompat.getColor(itemView.context, R.color.medal_bronze),
            ContextCompat.getColor(itemView.context, R.color.medal_silver),
            ContextCompat.getColor(itemView.context, R.color.medal_gold)
        )

        override fun bind(advice: ExpertAdvice) = with(bindingMedal) {
            contestName.text = advice.contestName
            medal.setColorFilter(medalColors[advice.value])
            deleteAdvice.setOnClickListener {
                onDeleteListener(advice)
            }
        }
    }

    inner class RateViewHolder(itemView: View) : BaseAdviceViewHolder(itemView) {
        private val bindingRate = ItemExpertAdviceRateBinding.bind(itemView)

        override fun bind(advice: ExpertAdvice) = with(bindingRate) {
            contestName.text = advice.contestName
            val total = if (advice.isRate20.toBoolean()) 20 else 100
            rate.text = itemView.context.getString(R.string.item_rate, advice.value, total)
            deleteAdvice.setOnClickListener {
                onDeleteListener(advice)
            }
        }
    }

    inner class StarViewHolder(itemView: View) : BaseAdviceViewHolder(itemView) {
        private val bindingStar = ItemExpertAdviceStarBinding.bind(itemView)

        override fun bind(advice: ExpertAdvice) = with(bindingStar) {
            contestName.text = advice.contestName
            starCount.text = (advice.value + 1).toString()
            deleteAdvice.setOnClickListener {
                onDeleteListener(advice)
            }
        }
    }

    abstract class BaseAdviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(advice: ExpertAdvice)
    }
}
