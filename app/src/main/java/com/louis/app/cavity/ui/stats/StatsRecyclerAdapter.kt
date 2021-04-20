package com.louis.app.cavity.ui.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemStatChartBinding
import com.louis.app.cavity.databinding.ItemStatMirrorBinding
import com.louis.app.cavity.databinding.ItemStatPieBinding

class StatsRecyclerAdapter : ListAdapter<StatUiModel, StatsRecyclerAdapter.StatViewHolder>(StatItemDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            R.layout.item_stat_chart -> ChartViewHolder(
                ItemStatChartBinding.inflate(inflater, parent, false)
            )
            R.layout.item_stat_pie -> PieViewHolder(
                ItemStatPieBinding.inflate(inflater, parent, false)
            )
            R.layout.item_stat_mirror -> MirrorViewHolder(
                ItemStatMirrorBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalStateException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: StatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount() = 5

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is StatUiModel.Chart -> R.layout.item_stat_chart
        is StatUiModel.Pie -> R.layout.item_stat_pie
        is StatUiModel.Mirror -> R.layout.item_stat_mirror
    }

    class StatItemDiffCallback : DiffUtil.ItemCallback<StatUiModel>() {
        override fun areItemsTheSame(oldItem: StatUiModel, newItem: StatUiModel) = true

        override fun areContentsTheSame(oldItem: StatUiModel, newItem: StatUiModel) =
            oldItem == newItem
    }

    inner class PieViewHolder(binding: ItemStatPieBinding) : StatViewHolder(binding) {
        override fun bind(item: StatUiModel) {
            TODO("Not yet implemented")
        }
    }

    inner class ChartViewHolder(binding: ItemStatChartBinding) : StatViewHolder(binding) {
        override fun bind(item: StatUiModel) {
            TODO("Not yet implemented")
        }
    }

    inner class MirrorViewHolder(binding: ItemStatMirrorBinding) : StatViewHolder(binding) {
        override fun bind(item: StatUiModel) {
            TODO("Not yet implemented")
        }
    }

    abstract class StatViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(item: StatUiModel)
    }
}
