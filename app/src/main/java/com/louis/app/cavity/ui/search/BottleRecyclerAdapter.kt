package com.louis.app.cavity.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.databinding.ItemBottleBinding
import com.louis.app.cavity.model.relation.BottleAndWineWithQGrapesAndFReviews
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.toBoolean

class BottleRecyclerAdapter(
    private val colors: List<Int>,
    private val onClickListener: (Long, Long) -> Unit
) :
    ListAdapter<BottleAndWineWithQGrapesAndFReviews, BottleRecyclerAdapter.BottleViewHolder>(BottleItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottleViewHolder {
        val binding = ItemBottleBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return BottleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BottleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int) = currentList[position].bottleId

    class BottleItemDiffCallback : DiffUtil.ItemCallback<BottleAndWineWithQGrapesAndFReviews>() {
        override fun areItemsTheSame(oldItem: BottleAndWineWithQGrapesAndFReviews, newItem: BottleAndWineWithQGrapesAndFReviews) =
            oldItem.bottleId == newItem.bottleId

        override fun areContentsTheSame(oldItem: BottleAndWineWithQGrapesAndFReviews, newItem: BottleAndWineWithQGrapesAndFReviews) =
            oldItem == newItem
    }

    inner class BottleViewHolder(private val binding: ItemBottleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bottleAndWine: BottleAndWineWithQGrapesAndFReviews) {
            with(binding.wineColorNameNaming) {
                wineName.text = bottleAndWine.name
                wineNaming.text = bottleAndWine.naming
                organicImage.setVisible(bottleAndWine.isOrganic.toBoolean())
                wineColorIndicator.setColorFilter(colors[bottleAndWine.color])

            }

            binding.root.setOnClickListener {
                onClickListener(bottleAndWine.wineId, bottleAndWine.bottleId)
            }

            binding.vintage.text = bottleAndWine.vintage.toString()
        }
    }
}
