package com.louis.app.cavity.ui.addbottle

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireDatesBinding
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.ui.DatePicker
import com.louis.app.cavity.ui.addbottle.stepper.Stepper
import com.louis.app.cavity.ui.addbottle.viewmodel.DateViewModel
import com.louis.app.cavity.util.DateFormatter
import java.util.*

class FragmentInquireDates : Fragment(R.layout.fragment_inquire_dates) {
    private lateinit var stepperx: Stepper
    private var _binding: FragmentInquireDatesBinding? = null
    private val binding get() = _binding!!
    private val dateViewModel: DateViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )
    private var isDatePickerDisplayed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireDatesBinding.bind(view)

        stepperx = parentFragment as Stepper

        initNumberPickers()
        initCurrencyDropdown()
        setListeners()
        observe()
    }

    private fun initNumberPickers() {
        val year = Calendar.getInstance().get(Calendar.YEAR)

        binding.vintage.apply {
            minValue = year - 20
            maxValue = year
            value = year - 5
        }

        binding.apogee.apply {
            minValue = year
            maxValue = year + 30
            value = year + 5
        }
    }

    private fun initCurrencyDropdown() {
        val items = resources.getStringArray(R.array.currencies)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)
        binding.currency.setAdapter(adapter)
    }

    private fun setListeners() {
        val title = getString(R.string.buying_date)
        DatePicker(childFragmentManager, -1L, binding.buyDateLayout, title).apply {
            onEndIconClickListener = { dateViewModel.setTimestamp(-1L) }
            onDateChangedListener = { dateViewModel.setTimestamp(it) }
        }

        with(binding) {
            stepper.next.setOnClickListener { validateFields() }
            stepper.previous.setOnClickListener { stepperx.requestPreviousPage() }
        }
    }

    private fun observe() {
        dateViewModel.updatedBottle.observe(viewLifecycleOwner) {
            if (it != null) updateFields(it)
        }
    }

    private fun updateFields(editedBottle: Bottle) {
        val formattedPrice = editedBottle.price.let { if (it != -1F) it.toString() else "" }

        with(binding) {
            vintage.value = editedBottle.vintage
            apogee.value = editedBottle.apogee
            count.setText(editedBottle.count.toString())
            price.setText(formattedPrice)
            currency.setSelection(0) // TODO: get actual selection
            buyLocation.setText(editedBottle.buyLocation)
            buyDate.setText(DateFormatter.formatDate(editedBottle.buyDate))
        }
    }

    private fun validateFields() = binding.countLayout.validate() && binding.priceLayout.validate()
        .also {
            if (it) {
                savePartialBottle()
                stepperx.requestNextPage()
            }
        }

    private fun savePartialBottle() {
        with(binding) {
            val count = count.text.toString().trim().toInt()
            val price = price.text.toString().trim()
            val formattedPrice = if (price.isEmpty()) -1F else price.toFloat()
            val currency = currency.text.toString()
            val location = buyLocation.text.toString().trim()

            dateViewModel.submitDates(
                vintage.value,
                apogee.value,
                count,
                formattedPrice,
                currency,
                location,
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
