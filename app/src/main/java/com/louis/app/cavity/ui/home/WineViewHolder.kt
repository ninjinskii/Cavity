package com.louis.app.cavity.ui.home

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.widget.AppCompatImageView
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
import com.louis.app.cavity.ui.home.widget.EffectImageView
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.toBoolean
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.louis.app.cavity.util.setVisible

class WineViewHolder(
    private val binding: ItemWineBinding,
    private val drawables: Pair<Drawable, Drawable>,
    private val transitionHelper: TransitionHelper,
    private val isLightTheme: Boolean
) :
    RecyclerView.ViewHolder(binding.root) {

    // List of views that need some sort of background
    // (either blur or shadow depending on api version) to be applied for text visibility
    private val colorables = binding.run {
        listOf(wineName, wineNaming, cuvee, bottlesCount, icons)
    }

    fun bind(wineWithBottles: WineWithBottles, highlight: Boolean) {
        val hexagon = binding.root
        val (wine, bottles) = wineWithBottles
        val wineColor = ContextCompat.getColor(itemView.context, wine.color.colorRes)

        ViewCompat.setTransitionName(hexagon, wine.id.toString())

        with(binding) {
            wineName.text = wine.name
            wineNaming.text = wine.naming
            cuvee.text = wine.cuvee
            cuvee.setVisible(cuvee.text.isNotBlank())
            bottlesCount.text = bottles.size.toString()

            val rightIcon = if (wine.isOrganic.toBoolean()) drawables.first else null
            val leftIcon = if (bottles.any { it.isReadyToDrink() }) drawables.second else null
            icons.setCompoundDrawablesWithIntrinsicBounds(leftIcon, null, rightIcon, null)

            hexagon.setMarkerColor(wineColor)

            val hasImage = wine.imgPath.isNotEmpty()

            if (hasImage) {
                loadImage(wine.imgPath)
            } else {
                updateColorables(hasImage = false)
                (binding.wineImage as AppCompatImageView).setImageDrawable(null)
            }
        }

        tryBlurEffect()

        if (highlight) {
            highlight()
        }

        itemView.setOnClickListener {
            // TODO: do not navigate to add bottle f bottle count is 0. Also, handle empty state in FragmentBoittleDetails
            if (bottles.isNotEmpty()) {
                transitionHelper.setElevationScale()

                val transition =
                    itemView.context.getString(R.string.transition_bottle_details, wine.id)
                val extra = FragmentNavigatorExtras(hexagon to transition)
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

    private fun highlight() {
        with(binding.root) {
            post {
                val springX = SpringAnimation(this, DynamicAnimation.SCALE_X, 1f)
                val springY = SpringAnimation(this, DynamicAnimation.SCALE_Y, 1f)

                springX.spring = SpringForce(1f).apply {
                    stiffness = SpringForce.STIFFNESS_VERY_LOW
                    dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                }

                springY.spring = springX.spring

                scaleX = 1.3f
                scaleY = 1.3f

                springX.start()
                springY.start()
            }
        }
    }

    private fun tryBlurEffect() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return
        }

        if (binding.wineImage is EffectImageView) {
            binding.wineImage.setTargets(colorables.filter { it.isVisible })
        }
    }

    private fun loadImage(imgPath: String) {
        /**
         * Note to my future self: do not try to merge ic_image_search with ic_image_not_found
         * even though they are the same, it will cause alpha issue in add wine fragemnt.
         * Cannot override alpha in there, will work once but not twice.
         * This must has something to do with what glide does to resources
         */
        Glide.with(itemView.context)
            .load(imgPath.toUri())
            .run {
                val drawable =
                    ResourcesCompat.getDrawable(
                        itemView.resources,
                        R.drawable.ic_image_not_found,
                        itemView.context.theme
                    )?.apply {
                        setTint(if (isLightTheme) Color.BLACK else Color.WHITE)
                        alpha = 10
                    }

                error(drawable)
            }
            .centerCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ) = false.also { updateColorables(hasImage = false) }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ) = false.also { updateColorables(hasImage = true) }
            })
            .into(binding.wineImage as AppCompatImageView)
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
