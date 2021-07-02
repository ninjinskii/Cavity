package com.louis.app.cavity.ui.manager.county

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemCountyManagerBinding
import com.louis.app.cavity.db.dao.CountyWithWines
import com.louis.app.cavity.model.County
import java.util.*

class CountyRecyclerAdapter(
    private val onDragIconTouched: (RecyclerView.ViewHolder) -> Unit,
    private val onRename: (County) -> Unit,
    private val onDelete: (County) -> Unit
) :
    RecyclerView.Adapter<CountyRecyclerAdapter.CountyViewHolder>() {

    private val counties = mutableListOf<CountyWithWines>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountyViewHolder {
        val binding =
            ItemCountyManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return CountyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountyViewHolder, position: Int) {
        holder.bind(counties[position])
    }

    override fun getItemId(position: Int) = counties[position].county.id

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
                    onDragIconTouched(this@CountyViewHolder)
                }

                true
            }

            binding.buttonOptions.setOnClickListener {
                showPopup(it)
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

        private fun showPopup(view: View) {
            val county = counties[bindingAdapterPosition].county

            PopupMenu(context, view).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) setForceShowIcon(true)

                menuInflater.inflate(R.menu.rename_delete_menu, menu)

                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.edit_item -> onRename(county)
                        R.id.delete_item -> onDelete(county)
                    }
                    true
                }
                show()
            }
        }
    }
}
