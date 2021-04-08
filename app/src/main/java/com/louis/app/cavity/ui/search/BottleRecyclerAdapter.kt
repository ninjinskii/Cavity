package com.louis.app.cavity.ui.search

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.databinding.ItemBottleBinding
import com.louis.app.cavity.model.relation.bottle.BoundedBottle
import com.louis.app.cavity.ui.WineColorResolver
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.toBoolean

class BottleRecyclerAdapter(
    private val _context: Context,
    private val onClickListener: (Long, Long) -> Unit
) :
    ListAdapter<BoundedBottle, BottleRecyclerAdapter.BottleViewHolder>(
        BottleItemDiffCallback()
    ), WineColorResolver {

    override fun getOverallContext() = _context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottleViewHolder {
        val binding = ItemBottleBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return BottleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BottleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int) = currentList[position].bottle.id

    class BottleItemDiffCallback : DiffUtil.ItemCallback<BoundedBottle>() {
        override fun areItemsTheSame(oldItem: BoundedBottle, newItem: BoundedBottle) =
            oldItem.bottle.id == newItem.bottle.id

        override fun areContentsTheSame(oldItem: BoundedBottle, newItem: BoundedBottle) =
            oldItem == newItem
    }

    inner class BottleViewHolder(private val binding: ItemBottleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(boundedBottle: BoundedBottle) {
            val (bottle, wine) = boundedBottle

            with(binding.wineColorNameNaming) {
                wineName.text = wine.name
                wineNaming.text = wine.naming
                organicImage.setVisible(wine.isOrganic.toBoolean())
                wineColorIndicator.setColorFilter(resolveColor(wine.color))

            }

            binding.root.setOnClickListener {
                onClickListener(wine.id, bottle.id)
            }

            binding.vintage.text = bottle.vintage.toString()
        }
    }
}
