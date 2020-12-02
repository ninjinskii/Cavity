package com.louis.app.cavity.ui.addbottle.steps

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
import com.louis.app.cavity.ui.addbottle.AddBottleViewModel
import com.louis.app.cavity.ui.addbottle.stepper.Stepper
import com.louis.app.cavity.util.DateFormatter
import java.util.*

class FragmentInquireDates : Fragment(R.layout.fragment_inquire_dates) {
    private var _binding: FragmentInquireDatesBinding? = null
    private val binding get() = _binding!!
    private lateinit var datePicker: MaterialDatePicker<Long>
    private lateinit var stepper: Stepper
    private val addBottleViewModel: AddBottleViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )
    private var isDatePickerDisplayed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireDatesBinding.bind(view)

        stepper = parentFragment as Stepper

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
        binding.buyDateLayout.setEndIconOnClickListener {
            binding.buyDate.setText("")
            addBottleViewModel.setTimestamp(-1L)
        }

        binding.buyDate.apply {
            inputType = InputType.TYPE_NULL
            datePicker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText(R.string.buying_date)
                .build()

            datePicker.addOnDismissListener {
                clearFocus()
                isDatePickerDisplayed = false
            }

            datePicker.addOnPositiveButtonClickListener {
                addBottleViewModel.setTimestamp(datePicker.selection ?: -1L)
                setText(datePicker.headerText.toString())
            }

            setOnClickListener {
                datePicker.show(
                    childFragmentManager,
                    resources.getString(R.string.tag_date_picker)
                )
            }

            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    if (!isDatePickerDisplayed) {
                        isDatePickerDisplayed = true

                        datePicker.show(
                            childFragmentManager,
                            resources.getString(R.string.tag_date_picker)
                        )
                    }
                }
            }
        }

        binding.stepper.next.setOnClickListener { validateFields() }

        binding.stepper.previous.setOnClickListener { stepper.requestPreviousPage() }
    }

    private fun observe() {
        addBottleViewModel.updatedBottle.observe(viewLifecycleOwner) {
            if (it != null) updateFields(it)
        }
    }

    private fun updateFields(editedBottle: Bottle) {
        with(binding) {
            vintage.value = editedBottle.vintage
            apogee.value = editedBottle.apogee
            count.setText(editedBottle.count.toString())
            price.setText(editedBottle.price.toString())
            currency.setSelection(0) // TODO: get actual selection
            buyLocation.setText(editedBottle.buyLocation)
            buyDate.setText(DateFormatter.formatDate(editedBottle.buyDate))
        }
    }

    private fun validateFields() = binding.countLayout.validate() && binding.priceLayout.validate()
        .also { if (it) { savePartialBottle(); stepper.requestNextPage() } }

    private fun savePartialBottle() {
        with(binding) {
            val count = count.text.toString().trim().toInt()
            val price = price.text.toString().trim()
            val formattedPrice = if (price.isEmpty()) -1F else price.toFloat()
            val currency = currency.text.toString()
            val location = buyLocation.text.toString().trim()

            addBottleViewModel.saveStep1(
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
