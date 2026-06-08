package com.louis.app.cavity.ui.home.widget

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R

class TabViewHolder<T>(
    val view: View,
    private val onTabClick: (View, Int) -> Unit,
    private val onLongTabClick: (T, Int) -> Unit,
    private val displayText: (T) -> String,
) :
    RecyclerView.ViewHolder(view) {

    private val textView: TextView = view.findViewById(R.id.tabAdapterItemName)

    init {
        view.setOnClickListener {
            onTabClick(it, bindingAdapterPosition)
        }
    }

    fun bind(item: T) {
        textView.text = displayText(item)

        view.setOnLongClickListener {
            onLongTabClick(item, bindingAdapterPosition)
            true
        }
    }
}
