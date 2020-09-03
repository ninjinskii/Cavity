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
import com.louis.app.cavity.ui.bottle.steps.SliderWatcher

class GrapeRecyclerAdapter(val watcher: SliderWatcher) :
    ListAdapter<Grape, GrapeRecyclerAdapter.GrapeViewHolder>(GrapeItemDiffCallback()) {

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

    inner class GrapeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemGrapeBinding.bind(itemView)

        fun bind(grape: Grape) {
            with(binding) {
                grapeName.text = grape.name
                percent.text = grape.percentage.toString() + "%"
                slider.value = grape.percentage.toFloat()

                slider.addOnChangeListener { _, value, _ ->
                    if (!watcher.isValueAllowed(value.toInt())) {
                        val trustedValue = watcher.onValueRejected()
                        slider.value = trustedValue.toFloat()
                    }
                }

                deleteGrape.setOnClickListener {
                }
            }

        }
    }
}
