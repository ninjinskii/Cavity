package com.louis.app.cavity.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnFocusChangeListener
import androidx.annotation.StringRes
import androidx.core.content.res.use
import com.google.android.material.textfield.TextInputLayout
import com.louis.app.cavity.R

class RuledTextInputLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.textInputStyle
) :
    TextInputLayout(context, attrs, defStyleAttr), TextInputLayout.OnEditTextAttachedListener {

    companion object {
        const val RULE_ABSENT = 0x0
        const val RULE_REQUIRED = 0x1
        const val RULE_INTEGER = 0x2
        const val RULE_FLOATING = 0x4
        const val RULE_POSITIVE = 0x8
    }

    private val rules = mutableSetOf<Rule>()
    private var flags: Int = 0

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RuledTextInputLayout,
            defStyleAttr,
            R.style.Widget_MaterialComponents_TextInputLayout_FilledBox //com.google.android.material.R.style.Widget_Design_TextInputLayout,
        ).use {
            flags = it.getInteger(R.styleable.RuledTextInputLayout_rule, RULE_ABSENT)
            setDefaultRules()
        }

        onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validate()
        }
    }

    fun addRules(vararg newRules: Rule) {
        rules.addAll(newRules)
    }

    fun validate(requestFocusIfFail: Boolean = true): Boolean {
        val input = editText?.text.toString().trim()

        if (!containsFlag(RULE_REQUIRED) && input.isBlank())
            return true

        if (containsFlag(RULE_REQUIRED) && input.isBlank()) {
            error = context.getString(R.string.required_field)
            if (requestFocusIfFail) requestFocus()
            return false
        }

        for (rule in rules) {
            if (!rule.test(input)) {
                error = context.getString(rule.onTestFailed)
                if (requestFocusIfFail) requestFocus()
                return false
            }
        }

        clearError()
        return true
    }

    fun clearRules(clearDefaultRules: Boolean) {
        rules.clear()

        if (!clearDefaultRules) {
            setDefaultRules()
        }
    }

    private fun setDefaultRules() {
        if (containsFlag(RULE_REQUIRED))
            addRules(Rule(R.string.required_field) { it.isNotBlank() })

        if (containsFlag(RULE_INTEGER))
            addRules(Rule(R.string.require_integer) { it.toIntOrNull() != null })

        if (containsFlag(RULE_FLOATING))
            addRules(Rule(R.string.require_float) { it.toFloatOrNull() != null })

        if (containsFlag(RULE_POSITIVE))
            addRules(Rule(R.string.no_negative) {
                when {
                    containsFlag(RULE_INTEGER) -> it.toInt() > 0
                    containsFlag(RULE_FLOATING) -> it.toFloat() > 0
                    else -> throw IllegalArgumentException("When Positive rule is set, you must also provide either Integer or Floating rule.")
                }
            })
    }

    private fun containsFlag(flag: Int) = flags or flag == flags

    private fun clearError() {
        error = null
    }

    override fun onEditTextAttached(textInputLayout: TextInputLayout) {
        textInputLayout.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validate(requestFocusIfFail = false)
        }
    }
}

data class Rule(@StringRes val onTestFailed: Int, val test: (String) -> Boolean)
