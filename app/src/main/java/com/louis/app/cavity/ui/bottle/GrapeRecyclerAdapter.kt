package com.louis.app.cavity.ui.bottle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemGrapeBinding
import com.louis.app.cavity.model.Grape

class GrapeRecyclerAdapter :
    ListAdapter<Grape, GrapeRecyclerAdapter.GrapeViewHolder>(GrapeItemDiffCallback()) {

    private val maxGrapeQty = 100

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

                slider.clearOnSliderTouchListeners()
                slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: Slider) {
//
//                        L.v("______________________")
//                        L.v("newValue: $newValue, total: $total, max: $max")
//                        L.v("liste: $currentList")

                    }

                    override fun onStopTrackingTouch(slider: Slider) {
                        val newValue = slider.value.toInt()
                        val total = getTotalSlidersExceptModifiedOneValue(grape.name)

                        if (total + newValue > maxGrapeQty) slider.value =
                            (maxGrapeQty - total).toFloat()

                        val pos = currentList.indexOfFirst { it.name == grape.name }
                        currentList[pos].percentage = slider.value.toInt()
                    }
                })

                deleteGrape.setOnClickListener {
                }
            }
        }

        fun getTotalSlidersExceptModifiedOneValue(modifiedGrapeName: String) = currentList
            .filter { it.name != modifiedGrapeName }
            .map { it.percentage }
            .sum()
    }
}
