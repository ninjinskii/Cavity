package com.louis.app.cavity.ui.home

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
    private val drawables: Pair<Drawable, Drawable>,
    private val transitionHelper: TransitionHelper
) :
    RecyclerView.ViewHolder(binding.root) {

    private val colorables = binding.run {
        listOf(wineName, wineNaming, bottlesCount, icons)
    }

    private val isLightTheme =
        itemView.context
            .obtainStyledAttributes(intArrayOf(com.google.android.material.R.attr.isLightTheme))
            .use { it.getBoolean(0, false) }


    fun bind(wineWithBottles: WineWithBottles) {
        val hexagone = binding.root
        val (wine, bottles) = wineWithBottles
        val wineColor = ContextCompat.getColor(itemView.context, wine.color.colorRes)

        ViewCompat.setTransitionName(hexagone, wine.id.toString())
        tryBlurEffect()

        with(binding) {
            wineName.text = wine.name
            wineNaming.text = wine.naming
            bottlesCount.text = bottles.size.toString()

            val rightIcon = if (wine.isOrganic.toBoolean()) drawables.first else null
            val leftIcon = if (bottles.any { it.isReadyToDrink() }) drawables.second else null
            icons.setCompoundDrawablesWithIntrinsicBounds(leftIcon, null, rightIcon, null)

            hexagone.setMarkerColor(wineColor)

            val hasImage = wine.imgPath.isNotEmpty()
            updateColorables(hasImage)

            if (hasImage) {
                loadImage(wine.imgPath)
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

    private fun tryBlurEffect() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return
        }

        binding.wineImage.setTargets(colorables)
    }

    private fun loadImage(imgPath: String) {
        Glide.with(itemView.context)
            .load(Uri.parse(imgPath))
            .run {
                val drawable =
                    if (isLightTheme)
                        ResourcesCompat.getDrawable(
                            itemView.resources,
                            R.drawable.ic_image_search,
                            itemView.context.theme
                        )?.apply {
                            setTint(Color.BLACK)
                            alpha = 10
                        }
                    else null

                error(drawable)
            }
            .centerCrop()
            .into(binding.wineImage)
    }

    private fun updateColorables(hasImage: Boolean) {
        if (isLightTheme) {
            colorables.forEach {
                val radius = if (hasImage) 0f else 4f
                it.setShadowLayer(radius, 0f, 0f, Color.BLACK)
            }
        }

    }
}
