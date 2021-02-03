package com.louis.app.cavity.ui.manager.grape

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemGrapeManagerBinding
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.relation.grape.GrapeWithQuantifiedGrapes

class GrapeRecylerAdapter(
    private val onRename: (Grape) -> Unit,
    private val onDelete: (Grape) -> Unit
) :
    ListAdapter<GrapeWithQuantifiedGrapes, GrapeRecylerAdapter.GrapeViewHolder>(
        GrapeItemDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrapeViewHolder {
        val binding =
            ItemGrapeManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return GrapeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GrapeViewHolder, position: Int) =
        holder.bind(getItem(position))

    class GrapeItemDiffCallback : DiffUtil.ItemCallback<GrapeWithQuantifiedGrapes>() {
        override fun areItemsTheSame(
            oldItem: GrapeWithQuantifiedGrapes,
            newItem: GrapeWithQuantifiedGrapes
        ) =
            oldItem.grape.id == newItem.grape.id

        override fun areContentsTheSame(
            oldItem: GrapeWithQuantifiedGrapes,
            newItem: GrapeWithQuantifiedGrapes
        ) =
            oldItem.grape == newItem.grape && oldItem.qGrapes.size == oldItem.qGrapes.size

    }

    inner class GrapeViewHolder(private val binding: ItemGrapeManagerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        init {
            binding.buttonOptions.setOnClickListener {
                showPopup(it)
            }
        }

        fun bind(grapeWithQGrape: GrapeWithQuantifiedGrapes) {
            val (grape, qGrapes) = grapeWithQGrape

            with(binding) {
                grapeName.text = grape.name
                bottleCount.text = context.resources.getQuantityString(
                    R.plurals.bottles,
                    qGrapes.size,
                    qGrapes.size
                )
            }

        }

        private fun showPopup(view: View) {
            val grape = getItem(adapterPosition).grape

            PopupMenu(context, view).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) setForceShowIcon(true)

                menuInflater.inflate(R.menu.rename_delete_menu, menu)

                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.edit_item -> onRename(grape)
                        R.id.delete_item -> onDelete(grape)
                    }
                    true
                }
                show()
            }
        }
    }
}
