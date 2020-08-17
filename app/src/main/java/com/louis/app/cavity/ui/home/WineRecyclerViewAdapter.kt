package com.louis.app.cavity.ui.home

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.louis.app.cavity.R
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.toBoolean

class WineRecyclerViewAdapter(private val listener: OnVintageClickListener, context: Context) :
    ListAdapter<Wine, WineRecyclerViewAdapter.WineViewHolder>(WineItemDiffCallback()) {

    private val colors = listOf(
        context.getColor(R.color.wine_white),
        context.getColor(R.color.wine_red),
        context.getColor(R.color.wine_sweet),
        context.getColor(R.color.wine_rose),
        context.getColor(R.color.colorAccent)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineViewHolder {
        return WineViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_wine, parent, false),
            listener
        )
    }

    override fun onBindViewHolder(holder: WineViewHolder, position: Int) =
        L.v("bind")
        //holder.bind(getItem(position))

    class WineItemDiffCallback : DiffUtil.ItemCallback<Wine>() {
        override fun areItemsTheSame(oldItem: Wine, newItem: Wine): Boolean =
            oldItem.idWine == newItem.idWine

        override fun areContentsTheSame(oldItem: Wine, newItem: Wine) = oldItem == newItem
    }

    inner class WineViewHolder(itemView: View, private val listener: OnVintageClickListener) :
        RecyclerView.ViewHolder(itemView) {
        private val vImage: ImageView = itemView.findViewById(R.id.wineImage)
        private val vName: TextView = itemView.findViewById(R.id.wineName)
        private val vNaming: TextView = itemView.findViewById(R.id.wineNaming)
        private val vColor: ImageView = itemView.findViewById(R.id.wineColorIndicator)
        private val vBioImage: ImageView = itemView.findViewById(R.id.bioImage)
        //private val vVintageLayout: ChipGroup = itemView.findViewById(R.id.chipGroup)

        fun bind(wine: Wine) {
            with(wine) {
                vName.text = name
                vNaming.text = naming
                //vColor.setColorFilter(colors[color])
                vBioImage.visibility = if (isBio.toBoolean()) View.VISIBLE else View.GONE
                //vVintageLayout.addView()
            }
        }
    }
}
