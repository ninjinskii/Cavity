package com.louis.app.cavity.ui.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.databinding.ItemStatBinding
import com.louis.app.cavity.db.dao.Stat
import com.louis.app.cavity.db.dao.WineColorStat

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
        override fun areItemsTheSame(oldItem: Stat, newItem: Stat) =
            oldItem.label == newItem.label

        override fun areContentsTheSame(oldItem: Stat, newItem: Stat) =
            oldItem.label == newItem.label &&
                oldItem.percentage == newItem.percentage &&
                oldItem.color == newItem.color
    }

    inner class StatViewHolder(private val binding: ItemStatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(stat: Stat) {
            val ctx = itemView.context

            with(binding) {
                label.text =
                    if (stat is WineColorStat) ctx.getString(stat.wcolor.stringRes) else stat.label
                count.text = stat.count.toString()

                val resolvedColor = ctx.getColor(stat.color)
                color.setBackgroundColor(resolvedColor)
            }
        }
    }
}
