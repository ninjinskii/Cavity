package com.louis.app.cavity.ui.home

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemWineBinding
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.model.relation.wine.WineWithBottles
import com.louis.app.cavity.ui.WineColorResolver
import com.louis.app.cavity.util.toBoolean

class WineRecyclerAdapter(
    private val _context: Context,
    private val onClickListener: (Long, Long) -> Unit,
    private val onShowOptionsListener: (Wine) -> Unit
) :
    ListAdapter<WineWithBottles, WineRecyclerAdapter.WineViewHolder>(WineItemDiffCallback()),
    WineColorResolver {

    override fun getOverallContext() = _context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineViewHolder {
        val binding = ItemWineBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return WineViewHolder(binding)
    }

    override fun getItemViewType(position: Int) = R.layout.item_wine

    override fun onBindViewHolder(holder: WineViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int) = getItem(position).wine.id

    class WineItemDiffCallback : DiffUtil.ItemCallback<WineWithBottles>() {
        override fun areItemsTheSame(oldItem: WineWithBottles, newItem: WineWithBottles) =
            oldItem.wine.id == newItem.wine.id

        override fun areContentsTheSame(oldItem: WineWithBottles, newItem: WineWithBottles) =
            oldItem.wine == newItem.wine && oldItem.bottles == newItem.bottles
    }

    inner class WineViewHolder(private val binding: ItemWineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val vintageSb = StringBuilder()

        // TODO: Add raw sql query to WineDao to filter consumed bottles
        fun bind(wineWithBottles: WineWithBottles) {
            val wine = wineWithBottles.wine
            val bottles = wineWithBottles.bottles
                .toSet()
                .filter { !it.consumed.toBoolean() }
                .sortedBy { it.vintage }

            with(binding) {
                wineName.text = wine.name
                wineNaming.text = wine.naming
                //organicImage.setVisible(wine.isOrganic.toBoolean())
                //wineColorIndicator.setColorFilter(resolveColor(wine.color))

                vintageSb.clear().append(bottles.map { it.vintage }.toString())
                binding.vintages.text = vintageSb.toString()

                if (wine.imgPath.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(Uri.parse(wine.imgPath))
                        .centerCrop()
                        .into(binding.wineImage)
                } else {
                    binding.wineImage.setImageDrawable(null)
                }
            }

            itemView.setOnClickListener {
                if (bottles.isNotEmpty()) {
                    onClickListener(wine.id, bottles[0].id)
                }
            }

            itemView.setOnLongClickListener {
                onShowOptionsListener(wine)
                true
            }
        }
    }
}
