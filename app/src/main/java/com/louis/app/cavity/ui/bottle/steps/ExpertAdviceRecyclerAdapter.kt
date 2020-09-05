package com.louis.app.cavity.ui.bottle.steps

import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemExpertAdviceBinding
import com.louis.app.cavity.model.ExpertAdvice
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.toBoolean
import com.louis.app.cavity.util.toInt

//TODO: check unique name in grape and expertadvice
class ExpertAdviceRecyclerAdapter(val listener: (ExpertAdvice) -> Unit) :
    ListAdapter<ExpertAdvice, ExpertAdviceRecyclerAdapter.ExpertAdviceViewHolder>(
        ExpertAdviceItemDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpertAdviceViewHolder {
        return ExpertAdviceViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_expert_advice, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ExpertAdviceViewHolder, position: Int) =
        holder.bind(getItem(position))

    override fun getItemId(position: Int): Long {
        return currentList[position].idExpertAdvice
    }

    class ExpertAdviceItemDiffCallback : DiffUtil.ItemCallback<ExpertAdvice>() {
        override fun areItemsTheSame(oldItem: ExpertAdvice, newItem: ExpertAdvice) =
            oldItem.idExpertAdvice == newItem.idExpertAdvice

        override fun areContentsTheSame(oldItem: ExpertAdvice, newItem: ExpertAdvice) =
            oldItem == newItem
    }

    inner class ExpertAdviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemExpertAdviceBinding.bind(itemView)

        fun bind(advice: ExpertAdvice) {
            with(binding) {
                rbGroupType.setOnCheckedChangeListener { _, checkedId ->
                    val pos = currentList.indexOfFirst { it.contestName == advice.contestName }
                    currentList[pos].revertType()

                    when (checkedId) {
                        R.id.rbMedal -> currentList[pos].isMedal = true.toInt()
                        R.id.rbRate100 -> currentList[pos].isRate100 = true.toInt()
                        R.id.rbRate20 -> currentList[pos].isRate20 = true.toInt()
                        R.id.rbStar -> currentList[pos].isStar = true.toInt()
                    }
                }

                when {
                    advice.isMedal.toBoolean() -> {
                        rbGroupType.check(R.id.rbMedal)
                        rbGroupMedal.setVisible(true)
                        rbGroupStars.setVisible(false)
                        rateLayout.setVisible(false)
                    }
                    advice.isRate100.toBoolean() -> {
                        rbGroupType.check(R.id.rbRate100)
                        rateLayout.setVisible(true)
                        rbGroupMedal.setVisible(false)
                        rbGroupStars.setVisible(false)
                    }
                    advice.isRate20.toBoolean() -> {
                        rbGroupType.check(R.id.rbRate20)
                        rateLayout.setVisible(true)
                        rbGroupMedal.setVisible(false)
                        rbGroupStars.setVisible(false)
                    }
                    advice.isStar.toBoolean() -> {
                        rbGroupType.check(R.id.rbStar)
                        rbGroupStars.setVisible(true)
                        rateLayout.setVisible(false)
                        rbGroupMedal.setVisible(false)
                    }
                }

                val value: Int = advice.value
                when (value) {
                    0 -> {
                        rbBronze.isChecked = true
                        rbStar1.isChecked = true
                    }
                    1 -> {
                        rbSilver.isChecked = true
                        rbStar2.isChecked = true
                    }
                    2 -> {
                        rbGold.isChecked = true
                        rbStar3.isChecked = true
                    }
                }

                //rate.text = value.toString()

                deleteExpertAdvice.setOnClickListener {
                    listener(advice)
                }
            }
        }
    }
}
