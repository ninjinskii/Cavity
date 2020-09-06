package com.louis.app.cavity.ui.bottle.steps

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

class GrapeRecyclerAdapter(
    val onDeleteListener: (Grape) -> Unit,
    val onValueChangeListener: (Grape) -> Unit
) :
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
                percent.text = getFormattedPercentage(grape.percentage)
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

                        //val pos = currentList.indexOfFirst { it.name == grape.name }
                        //currentList[pos].percentage = slider.value.toInt()

                        binding.percent.text = getFormattedPercentage(slider.value)
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

    companion object {
        fun getFormattedPercentage(value: Number): String {
            val numeric = when (value) {
                is Float -> value.toInt().toString()
                is Int -> value.toString()
                else ->
                    throw UnsupportedOperationException("Cannot set value other than Int or Float")
            }

            return String.format(numeric, R.string.percentage)
        }
    }
}
