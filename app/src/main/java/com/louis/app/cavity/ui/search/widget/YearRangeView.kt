package com.louis.app.cavity.ui.search.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.NumberPicker
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.YearPickerBinding
import com.louis.app.cavity.util.setVisible
import java.util.Calendar
import kotlin.properties.Delegates

class YearRangeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: YearPickerBinding
    val year = Calendar.getInstance().get(Calendar.YEAR)

    private var onValueChangeListener: ((Pair<Int?, Int?>) -> Unit)? = null

    var yearRange: Pair<Int?, Int?> by Delegates.observable(null to null) { _, _, new ->
        onValueChangeListener?.invoke(new)
    }
        private set

    var valueFrom = year - 40
        private set

    var valueTo = year
        private set

    init {
        val view = inflate(context, R.layout.year_picker, this)
        binding = YearPickerBinding.bind(view)

        binding.startVintage.setOnCloseIconClickListener {
            setStartYear(null)
        }

        binding.endVintage.setOnCloseIconClickListener {
            setEndYear(null)
        }

        binding.previous.setOnClickListener {
            val numberPicker = NumberPicker(context).apply {
                maxValue = yearRange.second ?: valueTo
                minValue = valueFrom
                value = yearRange.second ?: valueTo
            }

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.search)
                .setView(numberPicker)
                .setPositiveButton(R.string.ok) { _, _ ->
                    setYearRange(numberPicker.value, yearRange.second)
                }
                .show()
        }

        binding.next.setOnClickListener {
            val numberPicker = NumberPicker(context).apply {
                maxValue = valueTo
                minValue = yearRange.first ?: valueFrom
                value = yearRange.first ?: valueFrom
            }

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.search)
                .setView(numberPicker)
                .setPositiveButton(R.string.ok) { _, _ ->
                    setYearRange(yearRange.first, numberPicker.value)
                }
                .show()
        }
    }

    fun setYearRange(startYear: Int?, endYear: Int?) {
        setStartYear(startYear)
        setEndYear(endYear)
    }

    fun setOnValueChangeListener(listener: (Pair<Int?, Int?>) -> Unit) {
        onValueChangeListener = listener
    }

    private fun setStartYear(year: Int?) {
        yearRange = year to yearRange.second
        with(binding.startVintage) {
            text = year?.toString() ?: ""
            setVisible(year != null, invisible = true)
        }

        binding.startPlaceholder.setVisible(year == null)
    }

    private fun setEndYear(year: Int?) {
        yearRange = yearRange.first to year
        with(binding.endVintage) {
            text = year?.toString() ?: ""
            setVisible(year != null, invisible = true)
        }

        binding.endPlaceholder.setVisible(year == null)
    }
}
