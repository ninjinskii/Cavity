package com.louis.app.cavity.ui.search

import android.net.Uri
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemBottleBinding
import com.louis.app.cavity.db.dao.BoundedBottle
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.toBoolean
import androidx.core.net.toUri

class BottleRecyclerAdapter(
    private val onItemClicked: (View, BoundedBottle) -> Unit,
    private val pickMode: Boolean,
    private val onPicked: (BoundedBottle, Boolean) -> Unit
) :
    ListAdapter<BoundedBottle, BottleRecyclerAdapter.BottleViewHolder>(BottleItemDiffCallback()) {

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
            val wineColor = ContextCompat.getColor(itemView.context, wine.color.colorRes)

            ViewCompat.setTransitionName(binding.root, bottle.id.toString())

            with(binding) {
                marker.setVisible(bottle.consumed.toBoolean())
                checkedIcon.setVisible(bottle.isSelected)
                vintage.text = bottle.vintage.toString()
            }

            with(binding.wineColorNameNaming) {
                wineName.text = wine.name
                wineNaming.text = wine.naming
                organicImage.setVisible(wine.isOrganic.toBoolean())
                wineColorIndicator.setColorFilter(wineColor)
            }

            binding.root.setOnClickListener {
                if (pickMode) {
                    bottle.isSelected = !bottle.isSelected
                    TransitionManager.beginDelayedTransition(it as ViewGroup)
                    binding.checkedIcon.setVisible(bottle.isSelected)
                    onPicked(boundedBottle, bottle.isSelected)
                } else {
                    onItemClicked(itemView, boundedBottle)
                }
            }

            // If this view is available, we're on large screen
            if (binding.capacity != null) {
                bindLargeScreenVariant(wine, bottle)
            }
        }

        private fun bindLargeScreenVariant(wine: Wine, bottle: Bottle) {
            val context = itemView.context
            val formattedPrice = bottle.price.let { if (it != -1F) it.toString() else "" }

            with(binding) {
                wineImage?.let {
                    Glide.with(context)
                        .load(wine.imgPath.toUri())
                        .centerCrop()
                        .into(it)
                }

                capacity?.text = context.getString(bottle.bottleSize.stringRes)

                if (formattedPrice.isNotEmpty()) {
                    separatorPrice?.setVisible(true)
                    price?.setVisible(true)
                    price?.text = context.getString(
                        R.string.price_and_currency,
                        formattedPrice,
                        bottle.currency
                    )
                }

                apogeeIcon?.setVisible(bottle.isReadyToDrink())
                favoriteIcon?.setVisible(bottle.isFavorite.toBoolean())
            }
        }
    }
}
