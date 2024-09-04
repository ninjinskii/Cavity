package com.louis.app.cavity.ui.home

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.louis.app.cavity.databinding.ItemWineBinding
import com.louis.app.cavity.db.dao.WineWithBottles
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.util.toBoolean

class WineViewHolder(
    private val binding: ItemWineBinding,
    private val drawables: Pair<Drawable, Drawable>,
    private val onItemClick: (wine: Wine, bottles: List<Bottle>, itemView: View) -> Unit,
    private val onItemLongClick: (wine: Wine, bottles: List<Bottle>) -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(wineWithBottles: WineWithBottles) {
        val hexagone = binding.root
        val (wine, bottles) = wineWithBottles
        val wineColor = ContextCompat.getColor(itemView.context, wine.color.colorRes)

        ViewCompat.setTransitionName(hexagone, wine.id.toString())
//        tryBlurEffect(binding.wineNaming)

        with(binding) {
            wineName.text = wine.name
            wineNaming.text = wine.naming
            bottlesCount.text = bottles.size.toString()

            val rightIcon = if (wine.isOrganic.toBoolean()) drawables.first else null
            val leftIcon = if (bottles.any { it.isReadyToDrink() }) drawables.second else null
            icons.setCompoundDrawablesWithIntrinsicBounds(leftIcon, null, rightIcon, null)

            hexagone.setMarkerColor(wineColor)

            if (wine.imgPath.isNotEmpty()) {
                loadImage(wine.imgPath)
            } else {
                binding.wineImage.setImageDrawable(null)
            }
        }

        itemView.setOnClickListener {
            onItemClick(wine, bottles, binding.root)
        }

        itemView.setOnLongClickListener {
            onItemLongClick(wine, bottles)
            true
        }
    }

//    private fun tryBlurEffect(view: View) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
//            return
//        }
//
//        binding.wineImage.setTargets(listOf(itemView.findViewById(R.id.wineNaming), itemView.findViewById(R.id.bottlesCount), itemView.findViewById(R.id.icons), itemView.findViewById(R.id.wineName)))
//    }

    private fun loadImage(imgPath: String) {
        Glide.with(itemView.context)
            .load(Uri.parse(imgPath))
            .centerCrop()
            .into(binding.wineImage)
    }
}
