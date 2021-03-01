package com.louis.app.cavity.ui.addbottle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemGrapeBinding
import com.louis.app.cavity.model.relation.grape.QuantifiedGrapeAndGrape

class QuantifiedGrapeRecyclerAdapter(
    val onDeleteListener: (QuantifiedGrapeAndGrape) -> Unit,
    val onValueChangeListener: (QuantifiedGrapeAndGrape, newValue: Int) -> Int
) :
    ListAdapter<QuantifiedGrapeAndGrape, QuantifiedGrapeRecyclerAdapter.GrapeViewHolder>(
        GrapeItemDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrapeViewHolder {
        val binding = ItemGrapeBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return GrapeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GrapeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int) = currentList[position].getId()

    class GrapeItemDiffCallback : DiffUtil.ItemCallback<QuantifiedGrapeAndGrape>() {
        override fun areItemsTheSame(
            oldItem: QuantifiedGrapeAndGrape,
            newItem: QuantifiedGrapeAndGrape
        ) =
            oldItem.getId() == newItem.getId()

        override fun areContentsTheSame(
            oldItem: QuantifiedGrapeAndGrape,
            newItem: QuantifiedGrapeAndGrape
        ) =
            oldItem == newItem
    }

    inner class GrapeViewHolder(private val binding: ItemGrapeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(qGrapeAndGrape: QuantifiedGrapeAndGrape) = with(binding) {
            val (qGrape, name) = qGrapeAndGrape

            grapeName.text = name
            slider.value = qGrape.percentage.toFloat()
            percent.text = itemView.context.getString(
                R.string.percentage,
                qGrape.percentage
            )

            slider.clearOnSliderTouchListeners()
            slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    val acceptedVal = onValueChangeListener(qGrapeAndGrape, slider.value.toInt())
                    slider.value = acceptedVal.toFloat()
                }
            })

            deleteGrape.setOnClickListener {
                onDeleteListener(qGrapeAndGrape)
            }
        }
    }
}
