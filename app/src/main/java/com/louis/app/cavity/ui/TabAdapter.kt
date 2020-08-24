package com.louis.app.cavity.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R

class TabAdapter(
    private val tabs: MutableList<String> = mutableListOf(),
    private val style: TabStyle
) : RecyclerView.Adapter<TabViewHolder>() {

    private var listener: ((position: Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TabViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_county, parent, false)

        TextViewCompat.setTextAppearance(view.findViewById(R.id.county), style.tabTextStyle)
        return TabViewHolder(view, listener)
    }

    override fun getItemCount() = tabs.size

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) =
        holder.bind(tabs[position])

    fun addAll(list: List<String>) {
        tabs.clear()
        tabs.addAll(list)
        notifyDataSetChanged()
    }

    fun onTabClick(listener: ((position: Int) -> Unit)?) {
        this.listener = listener
    }

    internal fun listenTabChangeForPager() {}

}

class TabViewHolder(
    view: View,
    private val listener: ((position: Int) -> Unit)?
) : RecyclerView.ViewHolder(view) {

    private val tab: TextView = view.findViewById(R.id.county)

    init {
        view.setOnClickListener {
            listener?.invoke(adapterPosition)
        }
    }

    fun bind(model: String) {
        tab.text = model
    }
}
