package com.louis.app.cavity.ui.home

import android.content.Context
import android.graphics.Color
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemWineBinding
import com.louis.app.cavity.db.dao.WineWithBottles
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.ui.WineColorResolver
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.toBoolean

class WineViewHolder(private val binding: ItemWineBinding) : RecyclerView.ViewHolder(binding.root),
    WineColorResolver {

    private val bioIcon by lazy {
        ContextCompat.getDrawable(itemView.context, R.drawable.ic_bio)
    }

    private val glassIcon by lazy {
        ContextCompat.getDrawable(itemView.context, R.drawable.ic_glass)
            ?.also { it.setTint(Color.WHITE) }
    }

    // TODO: Add raw sql query to WineDao to filter consumed bottles
    fun bind(wineWithBottles: WineWithBottles) {
        val hexagone = binding.root
        val (wine) = wineWithBottles
        val bottles = wineWithBottles.bottles
            .toSet()
            .filter { !it.consumed.toBoolean() }
            .sortedBy { it.vintage }

        with(binding) {
            hexagone.isChecked = false
            infoLayout.setVisible(true)
            bottleRecyclerView.setVisible(false)

            wineName.text = wine.name
            wineNaming.text = wine.naming
            bottlesCount.text = bottles.size.toString()

            val rightIcon = if (wine.isOrganic.toBoolean()) bioIcon else null
            val leftIcon = if (bottles.any { it.isReadyToDrink() }) glassIcon else null
            icons.setCompoundDrawablesWithIntrinsicBounds(leftIcon, null, rightIcon, null)

            hexagone.setMarkerColor(resolveColor(wine.color))

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
                loadBottles(wine, bottles)
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

    private fun loadBottles(wine: Wine, bottles: List<Bottle>) {
        val onClick = { bottleId: Long ->
            val action = FragmentHomeDirections.homeToBottleDetails(wine.id, bottleId)
            itemView.findNavController().navigate(action)
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

    override fun getOverallContext(): Context = itemView.context
}
