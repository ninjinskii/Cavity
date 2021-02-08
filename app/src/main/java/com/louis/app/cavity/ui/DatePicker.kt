package com.louis.app.cavity.ui

import android.text.InputType
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.louis.app.cavity.R
import com.louis.app.cavity.util.DateFormatter

class DatePicker(
    childFragmentManager: FragmentManager,
    defaultDate: Long,
    associatedTextLayout: TextInputLayout,
    title: String
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
                    show(childFragmentManager, title)
                }
            }

            editText?.setOnClickListener {
                show(childFragmentManager, title)
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
    }

    private fun show(childFragmentManager: FragmentManager, title: String) {
        if (!isDatePickerDisplayed) {
            isDatePickerDisplayed = true

            datePicker.show(
                childFragmentManager,
                datePicker.resources.getString(R.string.tag_date_picker)
            )
        }
    }
}
