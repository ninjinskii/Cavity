package com.louis.app.cavity.ui.home

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.databinding.ItemWineBinding
import com.louis.app.cavity.db.dao.WineWithBottles
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.toBoolean

class WineViewHolder(
    private val binding: ItemWineBinding,
    private val transitionHelper: TransitionHelper,
    private val drawables: Pair<Drawable, Drawable>
) :
    RecyclerView.ViewHolder(binding.root) {

    // TODO: Add raw sql query to WineDao to filter consumed bottles
    fun bind(wineWithBottles: WineWithBottles) {
        val hexagone = binding.root
        val (wine, bottles) = wineWithBottles
        val wineColor = ContextCompat.getColor(itemView.context, wine.color.colorRes)

        ViewCompat.setTransitionName(hexagone, wine.id.toString())

        with(binding) {
            hexagone.isChecked = false
            infoLayout.setVisible(true)
            bottleRecyclerView.setVisible(false)

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
            hexagone.toggle()
            val isChecked = hexagone.isChecked

            with(binding) {
                infoLayout.setVisible(!isChecked)
                bottleRecyclerView.setVisible(isChecked)
            }

            if (isChecked) {
                loadBottles(hexagone, wine, bottles)
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

    private fun loadBottles(hexagone: View, wine: Wine, bottles: List<Bottle>) {
        val onClick = { bottleId: Long ->
            transitionHelper.setElevationScale() // Or Z shared axis

            val extra = FragmentNavigatorExtras(hexagone to bottleId.toString())
            val action = FragmentHomeDirections.homeToBottleDetails(wine.id, bottleId)
            itemView.findNavController().navigate(action, extra)
        }

        val bottleAdapter = BottleChipRecyclerAdapter(itemView.context, onClick)

        binding.bottleRecyclerView.apply {
            adapter = bottleAdapter
            layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
            setHasFixedSize(false)
        }

        bottleAdapter.submitList(bottles)
    }
}
