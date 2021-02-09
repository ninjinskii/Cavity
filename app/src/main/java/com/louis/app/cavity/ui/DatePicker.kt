package com.louis.app.cavity.ui

import android.text.InputType
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.louis.app.cavity.R
import com.louis.app.cavity.util.DateFormatter

class DatePicker(
    childFragmentManager: FragmentManager,
    associatedTextLayout: TextInputLayout,
    title: String,
    defaultDate: Long? = null
) {
    private val datePicker = MaterialDatePicker.Builder
        .datePicker()
        .setTitleText(title)
        .build()

    var isDatePickerDisplayed = false
        private set

    var onDateChangedListener: ((Long) -> Unit)? = null
    var onEndIconClickListener: (() -> Unit)? = null

    init {
        associatedTextLayout.apply {
            editText?.inputType = InputType.TYPE_NULL

            setEndIconOnClickListener {
                editText?.setText("")
                onEndIconClickListener?.invoke()
            }

            editText?.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    show(childFragmentManager)
                }
            }

            editText?.setOnClickListener {
                show(childFragmentManager)
            }
        }

        datePicker.apply {
            addOnDismissListener {
                associatedTextLayout.editText?.clearFocus()
                isDatePickerDisplayed = false
            }

            addOnPositiveButtonClickListener {
                val formattedDate = DateFormatter.formatDate(selection ?: defaultDate)
                associatedTextLayout.editText?.setText(formattedDate)
                selection?.let { onDateChangedListener?.invoke(it) }
            }
        }

        defaultDate?.let {
            val formattedDate = DateFormatter.formatDate(it)
            associatedTextLayout.editText?.setText(formattedDate)
            onDateChangedListener?.invoke(it)
        }
    }

    private fun show(childFragmentManager: FragmentManager) {
        if (!isDatePickerDisplayed) {
            isDatePickerDisplayed = true

            datePicker.show(
                childFragmentManager,
                "random-tag"
            )
        }
    }
}
