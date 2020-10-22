package com.louis.app.cavity.ui

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout
import com.louis.app.cavity.util.L

class RuledTextInputLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : TextInputLayout(context, attrs, defStyleAttr), InputValidation {

    private var rule: (String) -> Boolean = { true }

    override fun setValidationRule(rule: (String) -> Boolean) {
        this.rule = rule
    }

    override fun validate() {
        L.v("validating")
        if (rule(editText?.text.toString())) {

        } else {
            error = "Erreur"
        }
    }

    override fun clearErrors() {
        error = null
    }
}
