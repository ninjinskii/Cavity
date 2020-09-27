package com.louis.app.cavity.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemBottleBinding
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.toBoolean

class BottleRecyclerAdapter(
    private val onClickListener: (Bottle) -> Unit,
    private val colors: List<Int>
) : ListAdapter<BottleAndWine, BottleRecyclerAdapter.BottleViewHolder>(BottleItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottleViewHolder {
        return BottleViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_bottle, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BottleViewHolder, position: Int) =
        holder.bind(getItem(position))

    override fun getItemId(position: Int): Long {
        return currentList[position].bottle.bottleId
    }

    class BottleItemDiffCallback : DiffUtil.ItemCallback<BottleAndWine>() {
        override fun areItemsTheSame(oldItem: BottleAndWine, newItem: BottleAndWine) =
            oldItem.wine.wineId == newItem.wine.wineId

        override fun areContentsTheSame(oldItem: BottleAndWine, newItem: BottleAndWine) =
            oldItem.wine == newItem.wine
    }

    inner class BottleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemBottleBinding.bind(itemView)

        fun bind(bottleAndWine: BottleAndWine) {
            val (bottle, wine) = bottleAndWine

            with(binding.wineColorNameNaming) {
                wineName.text = wine.name
                wineNaming.text = wine.naming
                organicImage.setVisible(wine.isOrganic.toBoolean())
                wineColorIndicator.setColorFilter(colors[wine.color])
            }

            binding.vintage.text = bottle.vintage.toString()
        }
    }
}
