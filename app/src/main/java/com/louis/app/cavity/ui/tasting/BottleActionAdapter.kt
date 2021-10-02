package com.louis.app.cavity.ui.tasting

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemTastingBottleActionsBinding
import com.louis.app.cavity.db.dao.BottleWithTastingActions
import com.louis.app.cavity.model.TastingAction
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.toBoolean

class BottleActionAdapter(private val onActionCheckedChange: (TastingAction, Boolean) -> Unit) :
    ListAdapter<BottleWithTastingActions, BottleActionAdapter.BottleActionViewHolder>
        (BottleWithTastingActionsItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottleActionViewHolder {
        val binding =
            ItemTastingBottleActionsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return BottleActionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BottleActionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int) = R.layout.chip_friend

    class BottleWithTastingActionsItemDiffCallback :
        DiffUtil.ItemCallback<BottleWithTastingActions>() {
        override fun areItemsTheSame(
            oldItem: BottleWithTastingActions,
            newItem: BottleWithTastingActions
        ) =
            oldItem.bottle.id == newItem.bottle.id

        override fun areContentsTheSame(
            oldItem: BottleWithTastingActions,
            newItem: BottleWithTastingActions
        ) =
            oldItem == newItem
    }

    inner class BottleActionViewHolder(private val binding: ItemTastingBottleActionsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bottleWithTastingActions: BottleWithTastingActions) {
            val (bottle, wine, actions) = bottleWithTastingActions
            val wineColor = ContextCompat.getColor(itemView.context, wine.color.colorRes)

            binding.vintage.text = bottle.vintage.toString()

            Glide
                .with(itemView.context)
                .load(Uri.parse(wine.imgPath))
                .centerCrop()
                .into(binding.wineImage)

            with(binding.wineColorNameNaming) {
                organicImage.setVisible(wine.isOrganic.toBoolean())
                wineColorIndicator.setColorFilter(wineColor)
                wineName.text = wine.name
                wineNaming.text = wine.naming
            }

            addActionViews(actions)
        }

        private fun addActionViews(actions: List<TastingAction>) {
            binding.actions.removeAllViews()

            actions.forEach {
                val checkbox = CheckBox(itemView.context).apply {
                    text = it.type
                    isChecked = it.checked.toBoolean()
                    setOnCheckedChangeListener { _, isChecked ->
                        onActionCheckedChange(it, isChecked)
                    }
                }

                binding.actions.addView(
                    checkbox,
                    ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                )
            }
        }
    }
}
