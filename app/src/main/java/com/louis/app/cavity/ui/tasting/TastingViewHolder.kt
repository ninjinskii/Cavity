package com.louis.app.cavity.ui.tasting

import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemTastingBinding
import com.louis.app.cavity.db.dao.BoundedTasting
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.util.DateFormatter

class TastingViewHolder(
    private val binding: ItemTastingBinding,
    private val childViewPool: RecyclerView.RecycledViewPool,
    private val onItemClickListener: (Tasting, View) -> Unit
) :
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

        ViewCompat.setTransitionName(binding.root, tasting.id.toString())

        with(binding) {
            opportunity.text = tasting.opportunity
            date.text = DateFormatter.formatDate(tasting.date)
            bottleCount.text = bottles.size.toString()
        }

        with(binding.friendList) {
            adapter = friendAdapter
            layoutManager = childLayoutManager
            setRecycledViewPool(childViewPool)
            setHasFixedSize(true)
            setTargetView(binding.root)

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
            onItemClickListener(tasting, binding.root)
            // It is possible to do this, but we prefer not
            /*itemView.findFragment<Fragment>()
                .navigate(
                    TastingRoute.TastingDetails(tasting.id, tasting.opportunity),
                    binding.root
                )*/
        }
    }
}
