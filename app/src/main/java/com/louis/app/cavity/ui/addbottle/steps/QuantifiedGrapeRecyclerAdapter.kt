package com.louis.app.cavity.ui.addbottle.steps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemGrapeBinding
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.relation.QuantifiedBottleGrapeXRef

class QuantifiedGrapeRecyclerAdapter(
    val onDeleteListener: (QuantifiedBottleGrapeXRef) -> Unit,
    val onValueChangeListener: (QuantifiedBottleGrapeXRef, newValue: Int) -> Unit
) :
    ListAdapter<QuantifiedBottleGrapeXRef, QuantifiedGrapeRecyclerAdapter.GrapeViewHolder>(
        GrapeItemDiffCallback()
    ) {

    var grapes = emptyList<Grape>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrapeViewHolder {
        val binding = ItemGrapeBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return GrapeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GrapeViewHolder, position: Int) =
        holder.bind(getItem(position))

    override fun getItemId(position: Int): Long {
        return currentList[position].getId()
    }

    class GrapeItemDiffCallback : DiffUtil.ItemCallback<QuantifiedBottleGrapeXRef>() {
        override fun areItemsTheSame(
            oldItem: QuantifiedBottleGrapeXRef,
            newItem: QuantifiedBottleGrapeXRef
        ) =
            oldItem.getId() == newItem.getId()

        override fun areContentsTheSame(
            oldItem: QuantifiedBottleGrapeXRef,
            newItem: QuantifiedBottleGrapeXRef
        ) =
            oldItem == newItem
    }

    inner class GrapeViewHolder(private val binding: ItemGrapeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(quantifiedGrape: QuantifiedBottleGrapeXRef) {
            with(binding) {
                grapeName.text = getGrapeName(quantifiedGrape.grapeId)
                percent.text =
                    itemView.context.getString(R.string.percentage, quantifiedGrape.percentage)
                slider.value = quantifiedGrape.percentage.toFloat()

                slider.clearOnSliderTouchListeners()
                slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: Slider) {
                    }

                    override fun onStopTrackingTouch(slider: Slider) {
                        onValueChangeListener(quantifiedGrape, slider.value.toInt())
                    }
                })

                deleteGrape.setOnClickListener {
                    onDeleteListener(quantifiedGrape)
                }
            }
        }

        private fun getGrapeName(grapeId: Long) =
            grapes.find { it.grapeId == grapeId }?.name.orEmpty()
    }
}
