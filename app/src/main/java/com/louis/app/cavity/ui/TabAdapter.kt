package com.louis.app.cavity.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.model.County

class TabAdapter(
    private val tabs: MutableList<County> = mutableListOf(),
    private val style: TabStyle
) : RecyclerView.Adapter<TabAdapter.TabViewHolder>() {

    private var onClickListener: ((position: Int) -> Unit)? = null
    private var onLongClickListener: ((county: County) -> Unit) = {}

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TabViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_county, parent, false)

        TextViewCompat.setTextAppearance(view.findViewById(R.id.county), style.tabTextStyle)
        return TabViewHolder(view)
    }

    override fun getItemCount() = tabs.size

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) =
        holder.bind(tabs[position])

    fun addAll(list: List<County>) {
        tabs.clear()
        tabs.addAll(list)
        notifyDataSetChanged()
    }

    fun onTabClick(listener: ((position: Int) -> Unit)?) {
        this.onClickListener = listener
    }

    fun onLongTabClick(longClickListener: ((county: County) -> Unit)) {
        this.onLongClickListener = longClickListener
    }

    //internal fun listenTabChangeForPager() {}

    inner class TabViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.county)

        init {
            view.setOnClickListener {
                onClickListener?.invoke(adapterPosition)
            }
        }

        fun bind(county: County) {
            textView.text = county.name

            view.setOnLongClickListener {
                onLongClickListener(county)
                true
            }
        }
    }
}

