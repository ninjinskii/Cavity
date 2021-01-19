package com.louis.app.cavity.ui.manager.recycler

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemCountyManagerBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.relation.CountyWithWines
import java.util.*

class CountyRecyclerAdapter(
        private val dragCallback: DragListener,
        private val onLongClick: (County) -> Unit
) :
        RecyclerView.Adapter<CountyRecyclerAdapter.CountyViewHolder>() {

    private val counties = mutableListOf<CountyWithWines>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountyViewHolder {
        val binding =
                ItemCountyManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return CountyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountyViewHolder, position: Int) =
            holder.bind(counties[position])

    override fun getItemId(position: Int): Long {
        return counties[position].county.countyId
    }

    override fun getItemCount() = counties.size

    fun setCounties(list: List<CountyWithWines>) {
        counties.clear()
        counties.addAll(list)
        notifyItemRangeInserted(0, counties.size)
    }

    fun swapCounties(pos1: Int, pos2: Int) {
        Collections.swap(counties, pos1, pos2)
        counties[pos1].county.prefOrder = pos1
        counties[pos2].county.prefOrder = pos2
        notifyItemMoved(pos1, pos2)
    }

    fun getCounties() = counties.map { it.county }

    inner class CountyViewHolder(private val binding: ItemCountyManagerBinding) :
            RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        init {
            // TODO: fix accessibiliy warning
            binding.drag.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    dragCallback.requestDrag(this@CountyViewHolder)
                }

                true
            }

            binding.root.setOnLongClickListener {
                onLongClick(counties[adapterPosition].county)
                false
            }
        }

        fun bind(countyWithWines: CountyWithWines) {
            val (county, wines) = countyWithWines

            with(binding) {
                countyName.text = county.name
                wineCount.text = context.resources.getQuantityText(R.plurals.wines, wines.size)
            }
        }
    }

    interface DragListener {
        fun requestDrag(viewHolder: RecyclerView.ViewHolder)
    }
}