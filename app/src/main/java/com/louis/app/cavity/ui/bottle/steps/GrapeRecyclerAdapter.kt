package com.louis.app.cavity.ui.bottle.steps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemGrapeBinding
import com.louis.app.cavity.model.Grape

class GrapeRecyclerAdapter(
    val onDeleteListener: (Grape) -> Unit,
    val onValueChangeListener: (Grape) -> Unit
) :
    ListAdapter<Grape, GrapeRecyclerAdapter.GrapeViewHolder>(GrapeItemDiffCallback()) {

    val maxGrapeQty = 100

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrapeViewHolder {
        val binding = ItemGrapeBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return GrapeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GrapeViewHolder, position: Int) =
        holder.bind(getItem(position))

    override fun getItemId(position: Int): Long {
        return currentList[position].name.hashCode().toLong()
    }

    class GrapeItemDiffCallback : DiffUtil.ItemCallback<Grape>() {
        override fun areItemsTheSame(oldItem: Grape, newItem: Grape) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Grape, newItem: Grape) =
            oldItem == newItem
    }

    inner class GrapeViewHolder(private val binding: ItemGrapeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(grape: Grape) {
            with(binding) {
                grapeName.text = grape.name
                percent.text = itemView.context.getString(R.string.percentage, grape.percentage)
                slider.value = grape.percentage.toFloat()

                slider.clearOnSliderTouchListeners()
                slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: Slider) {
                    }

                    override fun onStopTrackingTouch(slider: Slider) {
                        val newValue = slider.value.toInt()
                        val total = getTotalSlidersExceptModifiedOneValue(grape.name)

                        if (total + newValue > maxGrapeQty) slider.value =
                            (maxGrapeQty - total).toFloat()

                        grape.percentage = slider.value.toInt()

                        binding.percent.text =
                            itemView.context.getString(R.string.percentage, slider.value.toInt())
                        onValueChangeListener(grape)
                    }
                })

                deleteGrape.setOnClickListener {
                    onDeleteListener(grape)
                }
            }
        }

        fun getTotalSlidersExceptModifiedOneValue(modifiedGrapeName: String) = currentList
            .filter { it.name != modifiedGrapeName }
            .map { it.percentage }
            .sum()
    }
}
