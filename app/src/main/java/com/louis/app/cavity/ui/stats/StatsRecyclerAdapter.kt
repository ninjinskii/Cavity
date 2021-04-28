package com.louis.app.cavity.ui.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.databinding.ItemStatBinding
import com.louis.app.cavity.db.dao.Stat

class StatsRecyclerAdapter :
    ListAdapter<Stat, StatsRecyclerAdapter.StatViewHolder>(StatItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatViewHolder {
        val binding = ItemStatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount() = currentList.size

    class StatItemDiffCallback : DiffUtil.ItemCallback<Stat>() {
        override fun areItemsTheSame(oldItem: Stat, newItem: Stat) = oldItem.label == newItem.label

        override fun areContentsTheSame(oldItem: Stat, newItem: Stat) =
            oldItem.count == newItem.count
    }

    inner class StatViewHolder(private val binding: ItemStatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(stat: Stat) {
            val resolvedColor = itemView.context.getColor(stat.safeColor)

            with(binding) {
                label.text = stat.label
                count.text = stat.count.toString()
                color.setBackgroundColor(resolvedColor)
//                comparisonCount.text = comparisonList[adapterPosition].toString()
//                comparisonCount.setVisible(comparisonMode)
//                comparisonIcon.setVisible(comparisonMode)
            }
        }
    }
}
