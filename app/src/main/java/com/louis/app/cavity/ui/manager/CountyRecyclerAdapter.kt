package com.louis.app.cavity.ui.manager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.databinding.ItemCountyManagerBinding
import com.louis.app.cavity.model.relation.CountyWithWines

class CountyRecyclerAdapter :
    ListAdapter<CountyWithWines, CountyRecyclerAdapter.CountyViewHolder>(CountyItemDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountyViewHolder {
        val binding =
            ItemCountyManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return CountyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountyViewHolder, position: Int) =
        holder.bind(getItem(position))

    override fun getItemId(position: Int): Long {
        return currentList[position].county.countyId
    }

    class CountyItemDiffCallback : DiffUtil.ItemCallback<CountyWithWines>() {
        override fun areItemsTheSame(oldItem: CountyWithWines, newItem: CountyWithWines) =
            oldItem.county.countyId == newItem.county.countyId

        override fun areContentsTheSame(oldItem: CountyWithWines, newItem: CountyWithWines) =
            oldItem.county == newItem.county && oldItem.wines == newItem.wines
    }

    inner class CountyViewHolder(private val binding: ItemCountyManagerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(countyWithWines: CountyWithWines) {
            with(binding) {
                countyName.text = countyWithWines.county.name
                bottleCount.text = countyWithWines.wines.size.toString()
            }
        }
    }

}