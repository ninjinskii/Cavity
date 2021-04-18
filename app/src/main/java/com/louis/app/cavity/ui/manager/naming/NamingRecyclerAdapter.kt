package com.louis.app.cavity.ui.manager.naming

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemNamingManagerBinding
import com.louis.app.cavity.db.dao.NamingWithWines

class NamingRecyclerAdapter(
    private val onRename: (Naming) -> Unit,
    private val onDelete: (Naming) -> Unit
) :
    ListAdapter<NamingWithWines, NamingRecyclerAdapter.NamingViewHolder>(GrapeItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NamingViewHolder {
        val binding =
            ItemNamingManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return NamingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NamingViewHolder, position: Int) =
        holder.bind(getItem(position))

    class GrapeItemDiffCallback : DiffUtil.ItemCallback<NamingWithWines>() {
        override fun areItemsTheSame(oldItem: NamingWithWines, newItem: NamingWithWines) =
            oldItem.naming.id == newItem.naming.id

        override fun areContentsTheSame(oldItem: NamingWithWines, newItem: NamingWithWines) =
            oldItem.naming == newItem.naming && oldItem.wines.size == oldItem.wines.size
    }

    inner class NamingViewHolder(private val binding: ItemNamingManagerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        init {
            binding.buttonOptions.setOnClickListener {
                showPopup(it)
            }
        }

        fun bind(namingWithWines: NamingWithWines) {
            val (_naming, wines) = namingWithWines

            with(binding) {
                naming.text = _naming.naming
                wineCount.text = context.resources.getQuantityString(
                    R.plurals.wines,
                    wines.size,
                    wines.size
                )
            }
        }

        private fun showPopup(view: View) {
            val naming = getItem(adapterPosition).naming

            PopupMenu(context, view).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) setForceShowIcon(true)

                menuInflater.inflate(R.menu.rename_delete_menu, menu)

                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.edit_item -> onRename(naming)
                        R.id.delete_item -> onDelete(naming)
                    }
                    true
                }
                show()
            }
        }
    }
}
