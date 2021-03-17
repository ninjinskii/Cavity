package com.louis.app.cavity.ui.home

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemWineBinding
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.model.relation.wine.WineWithBottles
import com.louis.app.cavity.ui.WineColorResolver
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.toBoolean

class WineRecyclerAdapter(
    private val _context: Context,
    private val onVintageClickListener: (Long, Bottle) -> Unit,
    private val onShowOptionsListener: (Wine) -> Unit,
) :
    ListAdapter<WineWithBottles, WineRecyclerAdapter.WineViewHolder>(WineItemDiffCallback()),
    WineColorResolver {

    override fun getOverallContext() = _context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineViewHolder {
        val binding = ItemWineBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return WineViewHolder(binding)
    }

    override fun getItemViewType(position: Int) = R.layout.item_wine

    override fun onBindViewHolder(holder: WineViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int) = getItem(position).wine.id

    class WineItemDiffCallback : DiffUtil.ItemCallback<WineWithBottles>() {
        override fun areItemsTheSame(oldItem: WineWithBottles, newItem: WineWithBottles) =
                oldItem.wine.id == newItem.wine.id

        override fun areContentsTheSame(oldItem: WineWithBottles, newItem: WineWithBottles) =
            oldItem.wine == newItem.wine && oldItem.bottles == newItem.bottles
    }

    inner class WineViewHolder(private val binding: ItemWineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // TODO: Might use a different way to filter bottles
        fun bind(wineWithBottles: WineWithBottles) {
            val wine = wineWithBottles.wine
            val bottles = wineWithBottles.bottles
                    .filter { !it.consumed.toBoolean() }
                    .sortedBy { it.vintage }

            with(binding) {
                wineName.text = wine.name
                wineNaming.text = wine.naming
                root.strokeColor = if (wine.isOrganic.toBoolean()) _context.getColor(R.color.cavity_green) else _context.getColor(R.color.cavity_gold)
                //organicImage.setVisible(wine.isOrganic.toBoolean())
                //wineColorIndicator.setColorFilter(resolveColor(wine.color))
                //binding.chipGroup.removeAllViews()

                /*for (bottle in bottles) {
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

                        setOnClickListener { onVintageClickListener(wine.id, bottle) }
                    }

                    binding.chipGroup.addView(chip)
                }*/

                if (wine.imgPath.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(Uri.parse(wine.imgPath))
                        .centerCrop()
                        .into(binding.wineImage)
                } else {
                    binding.wineImage.setImageDrawable(null)
                }
            }

            itemView.setOnLongClickListener {
                onShowOptionsListener(wine)
                true
            }
        }
    }
}
