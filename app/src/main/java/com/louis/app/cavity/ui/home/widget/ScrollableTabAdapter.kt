package com.louis.app.cavity.ui.home.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_ID
import com.louis.app.cavity.R
import com.louis.app.cavity.model.Chipable

class ScrollableTabAdapter<T>(
    private val onTabClick: (View, Int) -> Unit,
    private val onLongTabClick: (T, Int) -> Unit
) :
    RecyclerView.Adapter<ScrollableTabAdapter<T>.TabViewHolder>() {

    private val tabs = mutableListOf<T>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_county, parent, false)
        TextViewCompat.setTextAppearance(view.findViewById(R.id.county), R.style.TabTextAppearance)

        return TabViewHolder(view)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bind(tabs[position])
    }

    override fun getItemCount() = tabs.size

    override fun getItemId(position: Int) = (tabs[position] as? Chipable)?.getItemId() ?: NO_ID

    fun getItem(position: Int) = tabs[position]

    fun addAll(list: List<T>) {
        tabs.clear()
        tabs.addAll(list)
        notifyDataSetChanged()
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

