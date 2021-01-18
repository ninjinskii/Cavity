package com.louis.app.cavity.ui.manager.recycler

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.databinding.ItemCountyManagerBinding
import com.louis.app.cavity.model.County
import kotlinx.coroutines.withContext
import java.util.*

class CountyRecyclerAdapter(
    private val dragCallback: DragListener,
    private val onLongClick: (County) -> Unit
) :
    RecyclerView.Adapter<CountyRecyclerAdapter.CountyViewHolder>() {

    private val counties = mutableListOf<County>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountyViewHolder {
        val binding =
            ItemCountyManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return CountyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountyViewHolder, position: Int) =
        holder.bind(counties[position])

    override fun getItemId(position: Int): Long {
        return counties[position].countyId
    }

    override fun getItemCount() = counties.size

    fun setCounties(list: List<County>) {
        counties.clear()
        counties.addAll(list)
        notifyDataSetChanged()
    }

    fun swapCounties(pos1: Int, pos2: Int) {
        Collections.swap(counties, pos1, pos2)
        counties[pos1].prefOrder = pos1
        counties[pos2].prefOrder = pos2
        notifyItemMoved(pos1, pos2)
    }

    fun getCounties() = counties.toList()

    inner class CountyViewHolder(private val binding: ItemCountyManagerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // TODO: fix accessibiliy warning
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

        fun bind(county: County) {
            with(binding) {
                countyName.text = county.name
            }
        }
    }

    interface DragListener {
        fun requestDrag(viewHolder: RecyclerView.ViewHolder)
    }
}