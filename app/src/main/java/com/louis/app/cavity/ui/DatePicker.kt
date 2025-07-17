package com.louis.app.cavity.ui

import android.text.InputType
import androidx.core.view.doOnDetach
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputLayout.END_ICON_NONE
import com.louis.app.cavity.domain.error.SentryErrorReporter
import com.louis.app.cavity.util.DateFormatter

class DatePicker(
    fragmentManager: FragmentManager,
    private val associatedTextLayout: TextInputLayout,
    private val title: String,
    private val clearable: Boolean = false,
    private val defaultDate: Long? = null,
    private val constraint: CalendarConstraints? = null,
) {

    private val errorReporter = SentryErrorReporter.getInstance(associatedTextLayout.context)
    private var isDatePickerDisplayed = false

    private var picker: MaterialDatePicker<Long>? = null

    var onDateChangedListener: ((Long) -> Unit)? = null
    var onEndIconClickListener: (() -> Unit)? = null

    init {
        associatedTextLayout.apply {
            if (!clearable) {
                endIconMode = END_ICON_NONE
            }

            editText?.inputType = InputType.TYPE_NULL

            setEndIconOnClickListener {
                editText?.setText("")

                onEndIconClickListener?.invoke()
            }

            editText?.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    show(fragmentManager, createDatePicker())
                }
            }

            editText?.setOnClickListener {
                show(fragmentManager, createDatePicker())
            }
        }

        defaultDate?.let {
            val formattedDate = DateFormatter.formatDate(it)
            associatedTextLayout.editText?.setText(formattedDate)
            onDateChangedListener?.invoke(it)
        }

        associatedTextLayout.doOnDetach {
            dispose()
        }
    }

    fun dispose() {
        associatedTextLayout.apply {
            setEndIconOnClickListener(null)

            editText?.onFocusChangeListener = null
            editText?.setOnClickListener(null)
        }

        onDateChangedListener = null
        onEndIconClickListener = null
        picker = null
    }

    private fun createDatePicker(): MaterialDatePicker<Long> {
        val picker = MaterialDatePicker.Builder
            .datePicker()
            .setTitleText(title)
            .setCalendarConstraints(constraint)
            .build()

        picker.addOnDismissListener {
            associatedTextLayout.editText?.clearFocus()
            isDatePickerDisplayed = false
            this.picker = null
        }

        picker.addOnPositiveButtonClickListener {
            val date = if (DateFormatter.isToday(it)) System.currentTimeMillis() else it
            val formattedDate = DateFormatter.formatDate(date ?: defaultDate)
            associatedTextLayout.editText?.setText(formattedDate)
            date?.let { d -> onDateChangedListener?.invoke(d) }
        }

        return picker.also { this.picker = picker }
    }


    private fun show(childFragmentManager: FragmentManager, datePicker: MaterialDatePicker<Long>) {
        if (!isDatePickerDisplayed) {
            isDatePickerDisplayed = true

            try {
                datePicker.show(
                    childFragmentManager,
                    "random-tag"
                )
            } catch (e: IllegalStateException) {
                errorReporter.captureException(e)
            }
        }
    }
}
