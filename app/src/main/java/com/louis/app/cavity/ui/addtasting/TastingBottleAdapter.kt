package com.louis.app.cavity.ui.addtasting

import android.animation.AnimatorInflater
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemTastingBottleBinding
import com.louis.app.cavity.model.TastingBottle
import com.louis.app.cavity.util.AnimUtil
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.toBoolean

class TastingBottleAdapter(context: Context) :
    ListAdapter<TastingBottle, TastingBottleAdapter.TastingBottleViewHolder>
        (TastingBottleItemDiffCallback()) {

    private val flipLeftIn = AnimatorInflater.loadAnimator(context, R.animator.flip_left_in)
    private val flipLeftOut = AnimatorInflater.loadAnimator(context, R.animator.flip_left_out)
    private val flipRightIn = AnimatorInflater.loadAnimator(context, R.animator.flip_right_in)
    private val flipRightOut = AnimatorInflater.loadAnimator(context, R.animator.flip_right_out)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TastingBottleViewHolder {
        val binding =
            ItemTastingBottleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TastingBottleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TastingBottleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TastingBottleItemDiffCallback : DiffUtil.ItemCallback<TastingBottle>() {
        override fun areItemsTheSame(oldItem: TastingBottle, newItem: TastingBottle) =
            oldItem.bottleId == newItem.bottleId

        override fun areContentsTheSame(oldItem: TastingBottle, newItem: TastingBottle) =
            oldItem == newItem
    }

    inner class TastingBottleViewHolder(private val binding: ItemTastingBottleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bottle: TastingBottle) {
            if (bottle.isSelected) bindSelected(bottle) else bindStandard(bottle)

            binding.root.setOnClickListener {
                val flippedViews = listOf(binding.front, binding.back).apply {
                    if (bottle.isSelected) reversed()
                }

                AnimUtil.flipContainer(flippedViews[0], flippedViews[1])

                bottle.isSelected = !bottle.isSelected


                bind(bottle)
            }
        }

        fun bindStandard(bottle: TastingBottle) {
            val wine = bottle.wine
            val wineColor = ContextCompat.getColor(itemView.context, wine.color.colorRes)

            with(binding) {
                Glide
                    .with(itemView.context)
                    .load(Uri.parse(wine.imgPath))
                    .centerCrop()
                    .into(wineImage)

                wineColorNameNaming.organicImage.setVisible(wine.isOrganic.toBoolean())
                wineColorNameNaming.wineColorIndicator.setColorFilter(wineColor)
                wineColorNameNaming.wineName.text = wine.name
                wineColorNameNaming.wineNaming.text = wine.naming

                vintage.text = bottle.vintage.toString()
                temp.text = bottle.drinkTemp.getUnitString(itemView.context)

                val showJug = bottle.jugTime != 0
                jugTime.setVisible(showJug)
                jug.setVisible(showJug)
                separator.setVisible(showJug)

                if (showJug) {
                    jugTime.text = bottle.jugTime.toString()
                }
            }
        }

        fun bindSelected(bottle: TastingBottle) {
            binding.backTemp.editText?.apply {
                setText(bottle.drinkTemp.toString())
                doAfterTextChanged {
                    if (binding.backTemp.validate(requestFocusIfFail = false)) {
                        // change bottle drink temp on view model
                    }
                }
            }

            val jugTimes = listOf(0, 1, 2, 3, 4)
            val adapter = ArrayAdapter(itemView.context, R.layout.item_naming, jugTimes)

            (binding.backJugTime.editText as? AutoCompleteTextView)?.apply {
                setText(bottle.jugTime.toString())
                setAdapter(adapter)
                doAfterTextChanged {
                    // change botte jug time
                }
            }
        }
    }
}
