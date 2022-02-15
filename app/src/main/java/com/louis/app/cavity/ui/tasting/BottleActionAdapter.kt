package com.louis.app.cavity.ui.tasting

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.checkbox.MaterialCheckBox
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemTastingBottleActionsBinding
import com.louis.app.cavity.db.dao.BottleWithTastingActions
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.TastingAction
import com.louis.app.cavity.util.pxToSp
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.toBoolean

class BottleActionAdapter(
    private val onActionCheckedChange: (TastingAction, Boolean) -> Unit,
    private val onCommentChanged: (Bottle, String) -> Unit,
    private val onCloseIconClicked: (Bottle) -> Unit
) :
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
            true
    }

    inner class BottleActionViewHolder(private val binding: ItemTastingBottleActionsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val watcher = binding.comment.doAfterTextChanged {
            if (binding.commentLayout.validate()) {
                val bottle = currentList[bindingAdapterPosition].bottle
                onCommentChanged(bottle, it.toString())
            }
        }

        private val textSize = itemView.context.run {
            pxToSp(resources.getDimension(R.dimen.body2TextSize).toInt())
        }

        fun bind(bottleWithTastingActions: BottleWithTastingActions) {
            val (bottle, wine, actions) = bottleWithTastingActions
            val wineColor = ContextCompat.getColor(itemView.context, wine.color.colorRes)

            with(binding) {
                comment.removeTextChangedListener(watcher)
                comment.setText(bottle.tastingTasteComment)
                comment.addTextChangedListener(watcher)
                vintage.text = bottle.vintage.toString()
                buttonClose.setOnClickListener {
                    onCloseIconClicked(bottle)
                }
            }

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

            binding.todo.setVisible(actions.isNotEmpty())
            addActionViews(actions)
        }

        private fun addActionViews(actions: List<TastingAction>) {
            binding.actions.removeAllViews()

            actions.forEach {
                val actionText = when (it.type) {
                    TastingAction.Action.SET_TO_FRIDGE -> R.string.set_to_fridge
                    TastingAction.Action.SET_TO_JUG -> R.string.set_to_jug
                }

                val checkbox = MaterialCheckBox(itemView.context).apply {
                    //setTextAppearance(R.style.TextAppearance_Cavity_Body2)
                    // We need to use typeface here, cause Checkbox textAppearance does not work programatically
                    typeface = ResourcesCompat.getFont(context, R.font.forum)
                    textSize = this@BottleActionViewHolder.textSize
                    text = itemView.context.getString(actionText)
                    isChecked = it.done.toBoolean()
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
