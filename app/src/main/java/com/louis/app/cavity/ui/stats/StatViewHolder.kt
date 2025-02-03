package com.louis.app.cavity.ui.stats

import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.databinding.ItemStatBinding
import com.louis.app.cavity.db.dao.Stat
import com.louis.app.cavity.db.dao.WineColorStat

class StatViewHolder(
    private val binding: ItemStatBinding,
    private val onItemClicked: (itemBottlesIds: List<Long>, label: String) -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(stat: Stat) {
        val ctx = itemView.context

        with(binding) {
            label.text =
                if (stat is WineColorStat) ctx.getString(stat.wcolor.stringRes) else stat.label
            count.text = stat.count.toString()

            val resolvedColor = ctx.getColor(stat.color)
            color.setBackgroundColor(resolvedColor)

            root.setOnClickListener {
                val label =
                    if (stat is WineColorStat) it.context.getString(stat.wcolor.stringRes)
                    else stat.label

                onItemClicked(stat.bottleIds, label)
            }
        }
    }
}
