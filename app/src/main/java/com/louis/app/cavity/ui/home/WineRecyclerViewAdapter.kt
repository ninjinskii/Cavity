package com.louis.app.cavity.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemWineBinding
import com.louis.app.cavity.model.relation.WineWithBottles

class WineRecyclerViewAdapter(private val listener: OnVintageClickListener) :
    ListAdapter<WineWithBottles, WineRecyclerViewAdapter.WineViewHolder>(WineItemDiffCallback()) {

    private lateinit var colors: List<Int>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineViewHolder {
        colors = listOf(
            parent.context.getColor(R.color.wine_white),
            parent.context.getColor(R.color.wine_red),
            parent.context.getColor(R.color.wine_sweet),
            parent.context.getColor(R.color.wine_rose),
            parent.context.getColor(R.color.colorAccent)
        )

        return WineViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_wine, parent, false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: WineViewHolder, position: Int) =
        holder.bind(getItem(position))

    class WineItemDiffCallback : DiffUtil.ItemCallback<WineWithBottles>() {
        override fun areItemsTheSame(oldItem: WineWithBottles, newItem: WineWithBottles): Boolean =
            oldItem.wine.idWine == newItem.wine.idWine

        override fun areContentsTheSame(oldItem: WineWithBottles, newItem: WineWithBottles) =
            oldItem.wine == newItem.wine
    }

    inner class WineViewHolder(
        private val binding: ItemWineBinding,
        private val listener: OnVintageClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        // TODO: change destination
        private fun navigateToBottle(bottleId: Long, view: View) =
            view.findNavController().navigate(R.id.show_addBottle)

        fun bind(wineWithBottles: WineWithBottles) {
            with(binding) {
                model = WineWithBottlesModel(wineWithBottles)
                executePendingBindings()
            }
        }
    }
}
