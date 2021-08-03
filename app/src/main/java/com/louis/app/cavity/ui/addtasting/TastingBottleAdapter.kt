package com.louis.app.cavity.ui.addtasting

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.louis.app.cavity.databinding.ItemTastingBottleBinding
import com.louis.app.cavity.model.TastingBottle
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.toBoolean

class TastingBottleAdapter :
    ListAdapter<TastingBottle, TastingBottleAdapter.TastingBottleViewHolder>
        (TastingBottleItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TastingBottleViewHolder {
        val binding =
            ItemTastingBottleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TastingBottleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TastingBottleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TastingBottleItemDiffCallback : DiffUtil.ItemCallback<TastingBottle>() {
        override fun areItemsTheSame(oldItem: TastingBottle, newItem: TastingBottle) =
            oldItem.bottleId == newItem.bottleId

        override fun areContentsTheSame(oldItem: TastingBottle, newItem: TastingBottle) =
            oldItem == newItem
    }

    inner class TastingBottleViewHolder(private val binding: ItemTastingBottleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bottle: TastingBottle) {
            val wine = bottle.wine
            val wineColor = ContextCompat.getColor(itemView.context, wine.color.colorRes)

            with(binding) {
                Glide
                    .with(itemView.context)
                    .load(Uri.parse(wine.imgPath))
                    .centerCrop()
                    .into(wineImage)

                wineColorNameNaming.organicImage.setVisible(wine.isOrganic.toBoolean())
                wineColorNameNaming.wineColorIndicator.setColorFilter(wineColor)
                wineColorNameNaming.wineName.text = wine.name
                wineColorNameNaming.wineNaming.text = wine.naming

                vintage.text = bottle.vintage.toString()
                temp.text = bottle.drinkTemp.getUnitString(itemView.context)

                val showJug = bottle.jugTime == null
                jugTime.setVisible(showJug)
                jug.setVisible(showJug)
                separator.setVisible(showJug)

                if (showJug) {
                    jugTime.text = bottle.jugTime.toString()
                }
            }
        }
    }
}
