package com.louis.app.cavity.ui.tasting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemTastingBinding
import com.louis.app.cavity.db.dao.BoundedTasting
import com.louis.app.cavity.util.DateFormatter

class TastingRecyclerAdapter(private val childViewPool: RecyclerView.RecycledViewPool) :
    ListAdapter<BoundedTasting, TastingRecyclerAdapter.TastingViewHolder>
        (TastingItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TastingViewHolder {
        val binding = ItemTastingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TastingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TastingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TastingItemDiffCallback : DiffUtil.ItemCallback<BoundedTasting>() {
        override fun areItemsTheSame(oldItem: BoundedTasting, newItem: BoundedTasting) =
            oldItem.tasting.id == newItem.tasting.id

        override fun areContentsTheSame(oldItem: BoundedTasting, newItem: BoundedTasting) =
            oldItem == newItem
    }

    inner class TastingViewHolder(private val binding: ItemTastingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(boundedTasting: BoundedTasting) {
            val (tasting, bottles, friends) = boundedTasting
            val friendAdapter = FriendChipRecyclerAdapter()
            val childLayoutManager = LinearLayoutManager(
                itemView.context,
                LinearLayoutManager.HORIZONTAL,
                false
            ).apply {
                initialPrefetchItemCount = 4
            }

            with(binding) {
                opportunity.text = tasting.opportunity
                date.text = DateFormatter.formatDate(tasting.date)
                bottleCount.text = bottles.size.toString()
            }

            with(binding.friendList) {
                adapter = friendAdapter
                layoutManager = childLayoutManager
                setRecycledViewPool(childViewPool)

                if (itemDecorationCount == 0) {
                    addItemDecoration(
                        SpaceItemDecoration(
                            itemView.resources.getDimensionPixelSize(R.dimen.small_margin)
                        )
                    )
                }
            }

            friendAdapter.submitList(friends)

            binding.root.setOnClickListener {
                val action = FragmentTastingsDirections.tastingToTastingOverview(tasting.id)
                itemView.findNavController().navigate(action)
            }
        }
    }
}
