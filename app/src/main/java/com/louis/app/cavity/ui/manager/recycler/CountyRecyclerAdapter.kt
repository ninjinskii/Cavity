package com.louis.app.cavity.ui.manager.recycler

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemCountyManagerBinding
import com.louis.app.cavity.model.relation.CountyWithWines
import java.util.*

class CountyRecyclerAdapter(
        private val dragCallback: DragListener,
        private val onLongClick: (CountyWithWines) -> Unit
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
        notifyDataSetChanged()
    }

    fun swapCounties(pos1: Int, pos2: Int) {
        Collections.swap(counties, pos1, pos2)
        counties[pos1].county.prefOrder = pos1
        counties[pos2].county.prefOrder = pos2
        notifyItemMoved(pos1, pos2)
    }

    fun getCounties() = counties.map { it.county }

    // Clicking on the drag icon does nothing anyway
    @SuppressLint("ClickableViewAccessibility")
    inner class CountyViewHolder(private val binding: ItemCountyManagerBinding) :
            RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        init {
            binding.drag.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    dragCallback.requestDrag(this@CountyViewHolder)
                }

                true
            }

            binding.root.setOnLongClickListener {
                onLongClick(counties[adapterPosition])
                false
            }
        }

        fun bind(countyWithWines: CountyWithWines) {
            val (county, wines) = countyWithWines

            with(binding) {
                countyName.text = county.name
                wineCount.text =
                        context.resources.getQuantityString(R.plurals.wines, wines.size, wines.size)
            }
        }
    }

    interface DragListener {
        fun requestDrag(viewHolder: RecyclerView.ViewHolder)
    }
}