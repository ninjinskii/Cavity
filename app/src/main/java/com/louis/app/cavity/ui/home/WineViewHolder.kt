package com.louis.app.cavity.ui.home

import android.content.Context
import android.graphics.Color
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemWineBinding
import com.louis.app.cavity.model.relation.wine.WineWithBottles
import com.louis.app.cavity.ui.WineColorResolver
import com.louis.app.cavity.util.toBoolean

class WineViewHolder(private val binding: ItemWineBinding) : RecyclerView.ViewHolder(binding.root),
    WineColorResolver {

    val bioIcon by lazy {
        ContextCompat.getDrawable(itemView.context, R.drawable.ic_bio)
    }

    val glassIcon by lazy {
        ContextCompat.getDrawable(itemView.context, R.drawable.ic_glass)
            ?.also { it.setTint(Color.WHITE) }
    }

    // TODO: Add raw sql query to WineDao to filter consumed bottles
    fun bind(wineWithBottles: WineWithBottles) {
        val wine = wineWithBottles.wine
        val bottles = wineWithBottles.bottles
            .toSet()
            .filter { !it.consumed.toBoolean() }
            .sortedBy { it.vintage }

        with(binding) {
            wineName.text = wine.name
            wineNaming.text = wine.naming

            val rightIcon = if (wine.isOrganic.toBoolean()) bioIcon else null
            val leftIcon = if (bottles.any { it.isReadyToDrink() }) glassIcon else null
            icons.setCompoundDrawablesWithIntrinsicBounds(leftIcon, null, rightIcon, null)

            root.setMarkerColor(resolveColor(wine.color))

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
                val action = FragmentHomeDirections.homeToBottleDetails(wine.id, bottles[0].id)
                itemView.findNavController().navigate(action)
            }
        }

        itemView.setOnLongClickListener {
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

    override fun getOverallContext(): Context = itemView.context
}
