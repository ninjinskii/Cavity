package com.louis.app.cavity.ui.bottle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemGrapeBinding
import com.louis.app.cavity.model.Grape

class GrapeRecyclerAdapter :
    ListAdapter<Grape, GrapeRecyclerAdapter.GrapeViewHolder>(GrapeItemDiffCallback()) {

    private val grapes = mutableListOf<Grape>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrapeViewHolder {
        return GrapeViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_grape, parent, false)
        )
    }

    override fun onBindViewHolder(holder: GrapeViewHolder, position: Int) =
        holder.bind(getItem(position))

    override fun getItemId(position: Int): Long {
        return currentList[position].idGrape
    }

    class GrapeItemDiffCallback : DiffUtil.ItemCallback<Grape>() {
        override fun areItemsTheSame(oldItem: Grape, newItem: Grape) =
            oldItem.idGrape == newItem.idGrape

        override fun areContentsTheSame(oldItem: Grape, newItem: Grape) =
            oldItem == newItem
    }

    fun addGrape(grape: Grape) {
        grapes.add(grape)
        submitList(grapes.toMutableList())
    }

    fun removeGrape(grape: Grape) {
        grapes.remove(grape)
        submitList(grapes.toMutableList())
    }


    class GrapeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemGrapeBinding.bind(itemView)

        // TODO: change destination
        //private fun navigateToBottle(bottleId: Long, view: View) =
        //view.findNavController().navigate(R.id.show_addGrape)

        fun bind(grape: Grape) {
            with(binding) {

            }
        }
    }
}
