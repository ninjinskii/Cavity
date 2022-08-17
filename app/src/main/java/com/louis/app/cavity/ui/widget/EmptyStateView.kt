package com.louis.app.cavity.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.EmptyStateBinding
import com.louis.app.cavity.util.setVisible

class EmptyStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: EmptyStateBinding

    private var onActionClickListener: (() -> Unit)? = null
    private var onSecondaryActionClickListener: (() -> Unit)? = null

    init {
        val view = inflate(context, R.layout.empty_state, this)
        binding = EmptyStateBinding.bind(view)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.EmptyStateView,
            defStyleAttr,
            0
        ).use {
            val iconRes = it.getResourceId(R.styleable.EmptyStateView_bigIcon, 0)
            val textRes = it.getString(R.styleable.EmptyStateView_text)
            val actionText = it.getString(R.styleable.EmptyStateView_actionText)
            val secondaryActionText = it.getString(R.styleable.EmptyStateView_secondaryActionText)

            with(binding) {
                icon.setImageResource(iconRes)
                text.text = textRes
                action.setVisible(!actionText.isNullOrEmpty(), invisible = true)
                action.text = actionText
                secondaryAction.setVisible(
                    !secondaryActionText.isNullOrEmpty(),
                    invisible = true
                )
                secondaryAction.text = secondaryActionText
            }
        }

        initListeners()
    }

    fun setIcon(@DrawableRes icon: Int) {
        binding.icon.setImageResource(icon)
    }

    fun setText(text: String) {
        binding.text.text = text
    }

    fun setActionText(text: String?) {
        binding.action.setVisible(!text.isNullOrEmpty(), invisible = true)
        binding.action.text = text
    }

    // Android view attributes convention
    @Suppress("unused")
    fun setSecondaryActionText(text: String?) {
        binding.secondaryAction.setVisible(!text.isNullOrEmpty(), invisible = true)
        binding.secondaryAction.text = text
    }

    fun setOnActionClickListener(block: (() -> Unit)?) {
        onActionClickListener = block
    }

    fun setOnSecondaryActionClickListener(block: (() -> Unit)?) {
        onSecondaryActionClickListener = block
    }

    private fun initListeners() {
        binding.action.setOnClickListener { onActionClickListener?.invoke() }
        binding.secondaryAction.setOnClickListener { onSecondaryActionClickListener?.invoke() }
    }
}
