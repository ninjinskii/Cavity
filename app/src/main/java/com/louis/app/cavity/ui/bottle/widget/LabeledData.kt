package com.louis.app.cavity.ui.bottle.widget

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.LabeledDataBinding

class LabeledData @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: LabeledDataBinding

    init {
        val view = inflate(context, R.layout.labeled_data, this)
        binding = LabeledDataBinding.bind(view)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LabeledData,
            defStyleAttr,
            0
        ).use {
            val labelString = it.getString(R.styleable.LabeledData_label)
            val iconResource = it.getResourceId(R.styleable.LabeledData_icon, 0)

            with(binding) {
                label.text = labelString
                icon.setImageResource(iconResource)
            }
        }
    }

    fun setData(data: String) {
        binding.data.text = data
    }
}
