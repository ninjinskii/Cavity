package com.louis.app.cavity.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StringRes
import com.google.android.material.textfield.TextInputLayout
import com.louis.app.cavity.R

class RuledTextInputLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextInputLayout(context, attrs, defStyleAttr) {

    private val rules = mutableSetOf<Rule>()

    fun addRules(vararg newRules: Rule) {
        rules.addAll(newRules)
    }

    fun validate(required: Boolean): Boolean {
        val input = editText?.text.toString()

        if (required && input.isBlank()) {
            error = context.getString(R.string.required_field)
            return false
        }

        if (!required && input.isBlank()) return true

        for (rule in rules) {
            if (!rule.test(input)) {
                error = context.getString(rule.onTestFailed)
                requestFocus()
                return false
            }
        }

        clearError()
        return true
    }

    private fun clearError() {
        error = null
    }

}

data class Rule(@StringRes val onTestFailed: Int, val test: (String) -> Boolean)
