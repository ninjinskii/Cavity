package com.louis.app.cavity.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.louis.app.cavity.R
import com.louis.app.cavity.model.relation.WineWithBottles
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.toBoolean
import kotlinx.android.synthetic.main.item_wine.view.*
import java.net.URL

class WineRecyclerAdapter(private val listener: OnVintageClickListener) :
    ListAdapter<WineWithBottles, WineRecyclerAdapter.WineViewHolder>(WineItemDiffCallback()) {

    private lateinit var colors: List<Int>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineViewHolder {
//        L.v("Computing colors", "COLORS")
//        colors = listOf(
//            parent.context.getColor(R.color.wine_white),
//            parent.context.getColor(R.color.wine_red),
//            parent.context.getColor(R.color.wine_sweet),
//            parent.context.getColor(R.color.wine_rose),
//            parent.context.getColor(R.color.colorAccent)
//        )

        return WineViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_wine, parent, false),
            listener
        )
    }

    override fun onBindViewHolder(holder: WineViewHolder, position: Int) =
        holder.bind(getItem(position))

    class WineItemDiffCallback : DiffUtil.ItemCallback<WineWithBottles>() {
        override fun areItemsTheSame(oldItem: WineWithBottles, newItem: WineWithBottles) =
            oldItem.wine.idWine == newItem.wine.idWine

        override fun areContentsTheSame(oldItem: WineWithBottles, newItem: WineWithBottles) =
            oldItem.wine == newItem.wine
    }

    class WineViewHolder(
        itemView: View,
        private val listener: OnVintageClickListener
    ) : RecyclerView.ViewHolder(itemView) {
        // TODO: change destination
        private fun navigateToBottle(bottleId: Long, view: View) =
            view.findNavController().navigate(R.id.show_addBottle)

        fun bind(wineWithBottles: WineWithBottles) {
            val (wine, bottles) = wineWithBottles

            with(itemView) {
                wineName.text = wine.name
                wineNaming.text = wine.naming
                bioImage.setVisible(wine.isBio.toBoolean())
                //wineColorIndicator.setColorFilter(colors[wine.color])

                Glide.with(itemView.context)
                    .load(URL("https://images.freeimages.com/images/large-previews/9c3/sunshine-1408040.jpg"))
                    .centerCrop()
                    .into(wineImage)
            }
        }
    }
}
