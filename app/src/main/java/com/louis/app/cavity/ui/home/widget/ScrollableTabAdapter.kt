package com.louis.app.cavity.ui.home.widget

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.NO_ID
import com.louis.app.cavity.R

class ScrollableTabAdapter<T : Any>(
    private val onTabClick: (View, Int) -> Unit,
    private val onLongTabClick: (T, Int) -> Unit,
    idToContent: (T) -> Pair<Any, Any>
) :
    ListAdapter<T, TabViewHolder<T>>(ScrollableItemDiffCallback<T>(idToContent)) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder<T> {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_county, parent, false)
        return TabViewHolder(view, onTabClick, onLongTabClick)
    }

    override fun onBindViewHolder(holder: TabViewHolder<T>, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        if (itemCount <= 0) {
            return NO_ID
        }

        return currentList[position].toString().hashCode().toLong() + position
    }

    public override fun getItem(position: Int): T = super.getItem(position)

    class ScrollableItemDiffCallback<T>(
        private val idToContentComparator: (T) -> Pair<Any, Any>
    ) :
        DiffUtil.ItemCallback<T>() {

        override fun areItemsTheSame(oldItem: T & Any, newItem: T & Any) =
            idToContentComparator(oldItem).first == idToContentComparator(newItem).first

        // Responsibility of consumers
        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: T & Any, newItem: T & Any) =
            idToContentComparator(oldItem).second == idToContentComparator(newItem).second
    }
}

