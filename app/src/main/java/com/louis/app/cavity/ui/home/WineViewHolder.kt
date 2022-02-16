package com.louis.app.cavity.ui.home

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemWineBinding
import com.louis.app.cavity.db.dao.WineWithBottles
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.toBoolean

class WineViewHolder(
    private val binding: ItemWineBinding,
    private val transitionHelper: TransitionHelper,
    private val drawables: Pair<Drawable, Drawable>
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(wineWithBottles: WineWithBottles) {
        val hexagone = binding.root
        val (wine, bottles) = wineWithBottles
        val wineColor = ContextCompat.getColor(itemView.context, wine.color.colorRes)

        ViewCompat.setTransitionName(hexagone, wine.id.toString())

        with(binding) {
            wineName.text = wine.name
            wineNaming.text = wine.naming
            bottlesCount.text = bottles.size.toString()

            val rightIcon = if (wine.isOrganic.toBoolean()) drawables.first else null
            val leftIcon = if (bottles.any { it.isReadyToDrink() }) drawables.second else null
            icons.setCompoundDrawablesWithIntrinsicBounds(leftIcon, null, rightIcon, null)

            hexagone.setMarkerColor(wineColor)

            if (wine.imgPath.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(Uri.parse(wine.imgPath))
                    .centerCrop()
                    .into(binding.wineImage)
            } else {
                binding.wineImage.setImageDrawable(null)
            }
        }

        itemView.setOnClickListener {
            if (bottles.isNotEmpty()) {
                transitionHelper.setElevationScale()

                val transition =
                    itemView.context.getString(R.string.transition_bottle_details, wine.id)
                val extra = FragmentNavigatorExtras(hexagone to transition)
                val action = FragmentHomeDirections.homeToBottleDetails(wine.id, -1)
                itemView.findNavController().navigate(action, extra)
            } else {
                transitionHelper.setSharedAxisTransition(MaterialSharedAxis.Z, true)

                val action = FragmentHomeDirections.homeToAddBottle(wine.id, -1L)
                itemView.findNavController().navigate(action)
            }
        }

        itemView.setOnLongClickListener {
            transitionHelper.setSharedAxisTransition(MaterialSharedAxis.Z, navigatingForward = true)

            val action = FragmentHomeDirections.homeToWineOptions(
                wine.id,
                wine.countyId,
                wine.name,
                wine.naming,
                wine.isOrganic.toBoolean(),
                wine.color
            )
            itemView.findNavController().navigate(action)

            true
        }
    }
}
