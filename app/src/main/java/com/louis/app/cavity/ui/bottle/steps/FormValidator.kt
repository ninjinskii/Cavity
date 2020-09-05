package com.louis.app.cavity.ui.bottle.steps

import com.google.android.material.textfield.TextInputEditText

interface FormValidator {
    fun checkAllRequiredFieldsFilled(
        textFields: List<TextInputEditText>,
        errorText: String
    ): Boolean {
        textFields.forEach {
            if (it.text.toString().isEmpty()) {
                it.error = errorText
                return false
            }
        }

        return true
    }
}