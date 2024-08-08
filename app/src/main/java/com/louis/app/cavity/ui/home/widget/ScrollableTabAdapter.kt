package com.louis.app.cavity.ui.home.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_ID
import com.louis.app.cavity.R

class ScrollableTabAdapter<T>(
    private val onTabClick: (View, Int) -> Unit,
    private val onLongTabClick: (T, Int) -> Unit
) :
    ListAdapter<T, ScrollableTabAdapter<T>.TabViewHolder>(ScrollableItemDiffCallback<T>()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_county, parent, false)
        return TabViewHolder(view)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        if (itemCount <= 0) {
            return NO_ID
        }

        return currentList[position].toString().hashCode().toLong() + position
    }

    public override fun getItem(position: Int): T = super.getItem(position)

    // TODO: disable animations ?

    class ScrollableItemDiffCallback<T> : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T & Any, newItem: T & Any) =
            oldItem.toString() == newItem.toString()

        override fun areContentsTheSame(oldItem: T & Any, newItem: T & Any) = true
    }

    inner class TabViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.county)

        init {
            view.setOnClickListener {
                onTabClick(it, bindingAdapterPosition)
            }
        }

        fun bind(item: T) {
            textView.text = item.toString()

            view.setOnLongClickListener {
                onLongTabClick(item, bindingAdapterPosition)
                true
            }
        }
    }
}

