package com.louis.app.cavity.ui.manager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.databinding.ItemCountyManagerBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.relation.CountyWithWines

class CountyRecyclerAdapter :
    ListAdapter<County, CountyRecyclerAdapter.CountyViewHolder>(CountyItemDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountyViewHolder {
        val binding =
            ItemCountyManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return CountyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountyViewHolder, position: Int) =
        holder.bind(getItem(position))

    override fun getItemId(position: Int): Long {
        return currentList[position].countyId
    }

    class CountyItemDiffCallback : DiffUtil.ItemCallback<County>() {
        override fun areItemsTheSame(oldItem: County, newItem: County): Boolean {
            return oldItem.countyId == newItem.countyId
        }

        override fun areContentsTheSame(oldItem: County, newItem: County): Boolean {
            return oldItem == newItem
        }
    }

    inner class CountyViewHolder(private val binding: ItemCountyManagerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(county: County) {
            with(binding) {
                countyName.text = county.name
            }
        }
    }
}