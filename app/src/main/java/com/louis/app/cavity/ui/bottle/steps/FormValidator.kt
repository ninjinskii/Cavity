package com.louis.app.cavity.ui.bottle.steps

import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.textfield.TextInputEditText
import com.louis.app.cavity.R
import com.louis.app.cavity.util.showSnackbar

// add error string for each textlayout in pairs and eventually the validation lambda
interface FormValidator {
    fun checkAllRequiredFieldsFilled(
        textFields: List<TextInputEditText>,
        errorText: String,
        snackLayout: CoordinatorLayout
    ): Boolean {
        textFields.forEach {
            if (it.text.toString().isEmpty()) {
                it.error = errorText
                snackLayout.showSnackbar(R.string.no_required_fields_filled)
                return false
            }
        }

        return true
    }
}