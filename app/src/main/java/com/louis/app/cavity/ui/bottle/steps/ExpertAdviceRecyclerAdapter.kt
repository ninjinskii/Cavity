package com.louis.app.cavity.ui.bottle.steps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemExpertAdviceBinding
import com.louis.app.cavity.model.ExpertAdvice
import com.louis.app.cavity.util.toBoolean
import com.louis.app.cavity.util.toInt

class ExpertAdviceRecyclerAdapter(val listener: (ExpertAdvice) -> Unit) :
    ListAdapter<ExpertAdvice, ExpertAdviceRecyclerAdapter.ExpertAdviceViewHolder>(
        ExpertAdviceItemDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpertAdviceViewHolder {
        return ExpertAdviceViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_expert_advice, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ExpertAdviceViewHolder, position: Int) =
        holder.bind(getItem(position))

    override fun getItemId(position: Int): Long {
        return currentList[position].idExpertAdvice
    }

    class ExpertAdviceItemDiffCallback : DiffUtil.ItemCallback<ExpertAdvice>() {
        override fun areItemsTheSame(oldItem: ExpertAdvice, newItem: ExpertAdvice) =
            oldItem.idExpertAdvice == newItem.idExpertAdvice

        override fun areContentsTheSame(oldItem: ExpertAdvice, newItem: ExpertAdvice) =
            oldItem == newItem
    }

    inner class ExpertAdviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemExpertAdviceBinding.bind(itemView)

        fun bind(advice: ExpertAdvice) {
            with(binding) {

            }
        }
    }
}
