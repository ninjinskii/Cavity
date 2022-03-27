package com.louis.app.cavity.ui.addbottle.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemGrapeBinding
import com.louis.app.cavity.ui.addbottle.viewmodel.QGrapeUiModel

class QuantifiedGrapeRecyclerAdapter(
    val onDeleteListener: (QGrapeUiModel) -> Unit,
    val onValueChangeListener: (QGrapeUiModel, newValue: Int) -> Int
) :
    ListAdapter<QGrapeUiModel, QuantifiedGrapeRecyclerAdapter.GrapeViewHolder>(
        GrapeItemDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrapeViewHolder {
        val binding = ItemGrapeBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return GrapeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GrapeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int) = currentList[position].grapeId

    class GrapeItemDiffCallback : DiffUtil.ItemCallback<QGrapeUiModel>() {
        override fun areItemsTheSame(oldItem: QGrapeUiModel, newItem: QGrapeUiModel) =
            oldItem.grapeId == newItem.grapeId

        override fun areContentsTheSame(oldItem: QGrapeUiModel, newItem: QGrapeUiModel) =
            oldItem == newItem
    }

    inner class GrapeViewHolder(private val binding: ItemGrapeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(qGrape: QGrapeUiModel) = with(binding) {
            grapeName.text = qGrape.name
            slider.value = qGrape.percentage.toFloat()
            percent.text = itemView.context.getString(R.string.percentage, qGrape.percentage)

            slider.clearOnSliderTouchListeners()
            slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                @SuppressLint("RestrictedApi")
                override fun onStartTrackingTouch(slider: Slider) = Unit

                @SuppressLint("RestrictedApi")
                override fun onStopTrackingTouch(slider: Slider) {
                    val acceptedVal = onValueChangeListener(qGrape, slider.value.toInt())
                    slider.value = acceptedVal.toFloat()
                }
            })

            deleteGrape.setOnClickListener {
                onDeleteListener(qGrape)
            }
        }
    }
}
