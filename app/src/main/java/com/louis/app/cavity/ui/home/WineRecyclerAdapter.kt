package com.louis.app.cavity.ui.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemWineBinding
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.model.relation.WineWithBottles
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.toBoolean

class WineRecyclerAdapter(
    private val colors: List<Int>,
    private val onVintageClickListener: (Long, Bottle) -> Unit,
    private val onShowOptionsListener: (Wine) -> Unit,
) : ListAdapter<WineWithBottles, WineRecyclerAdapter.WineViewHolder>(WineItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineViewHolder {
        val binding = ItemWineBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return WineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WineViewHolder, position: Int) =
        holder.bind(getItem(position))

    override fun getItemId(position: Int): Long {
        return currentList[position].wine.wineId
    }

    class WineItemDiffCallback : DiffUtil.ItemCallback<WineWithBottles>() {
        override fun areItemsTheSame(oldItem: WineWithBottles, newItem: WineWithBottles) =
            oldItem.wine.wineId == newItem.wine.wineId

        override fun areContentsTheSame(oldItem: WineWithBottles, newItem: WineWithBottles) =
            oldItem.wine == newItem.wine
    }

    inner class WineViewHolder(private val binding: ItemWineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(wineWithBottles: WineWithBottles) {
            val wine = wineWithBottles.wine
            val bottles = wineWithBottles.bottles.sortedBy { it.vintage }

            with(binding.wineColorNameNaming) {
                wineName.text = wine.name
                wineNaming.text = wine.naming
                organicImage.setVisible(wine.isOrganic.toBoolean())
                wineColorIndicator.setColorFilter(colors[wine.color])
                binding.chipGroup.removeAllViews()

                for (bottle in bottles) {
                    val chip: Chip =
                        LayoutInflater.from(itemView.context).inflate(
                            R.layout.chip_action,
                            binding.chipGroup,
                            false
                        ) as Chip

                    chip.apply {
                        setTag(R.string.tag_chip_id, bottle.vintage)
                        text = bottle.vintage.toString()

                        if (bottle.isReadyToDrink())
                            chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_glass)

                        setOnClickListener { onVintageClickListener(wine.wineId, bottle) }
                    }

                    binding.chipGroup.addView(chip)
                }

                if (wine.imgPath.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(Uri.parse(wine.imgPath))
                        .centerCrop()
                        .into(binding.wineImage)
                }
            }

            itemView.setOnLongClickListener {
                onShowOptionsListener(wine)
                true
            }
        }
    }
}
