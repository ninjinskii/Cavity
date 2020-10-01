package com.louis.app.cavity.ui.addbottle.steps

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.datepicker.MaterialDatePicker
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireDatesBinding
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.ui.addbottle.AddBottleViewModel
import com.louis.app.cavity.ui.addbottle.stepper.FragmentStepper
import com.louis.app.cavity.util.DateFormatter
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.showSnackbar
import java.util.*

class FragmentInquireDates : Fragment(R.layout.fragment_inquire_dates) {
    private var _binding: FragmentInquireDatesBinding? = null
    private val binding get() = _binding!!
    private lateinit var stepperFragment: FragmentStepper
    private lateinit var feedBackObserver: Observer<Event<Int>>
    private lateinit var datePicker: MaterialDatePicker<Long>
    private val addBottleViewModel: AddBottleViewModel by activityViewModels()
    private var isDatePickerDisplayed = false
    private var buyDateTimestamp = -1L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireDatesBinding.bind(view)

        registerStepperWatcher()
        initNumberPickers()
        initCurrencyDropdown()
        setListeners()
        observe()
    }

    private fun registerStepperWatcher() {
        stepperFragment = parentFragmentManager.findFragmentById(R.id.stepper) as FragmentStepper

        stepperFragment.addListener(object : FragmentStepper.StepperWatcher {
            override fun onRequestChangePage() = validateFields()

            override fun onPageRequestAccepted() = savePartialBottle()
        })
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
        binding.buttonNext.setOnClickListener {
            stepperFragment.requireNextPage()
        }

        binding.buyDateLayout.setEndIconOnClickListener {
            binding.buyDate.setText("")
            buyDateTimestamp = -1L
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
                buyDateTimestamp = datePicker.selection ?: -1L
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
    }

    private fun observe() {
        feedBackObserver = Observer {
            it.getContentIfNotHandled()?.let { stringRes ->
                binding.coordinator.showSnackbar(stringRes)
            }
        }

        addBottleViewModel.userFeedback.observe(viewLifecycleOwner, feedBackObserver)

        addBottleViewModel.editedBottle.observe(viewLifecycleOwner) {
            if (it != null) updateFields(it)
        }
    }

    private fun updateFields(editedBottle: Bottle) {
        with(binding) {
            vintage.value = editedBottle.vintage
            apogee.value = editedBottle.apogee
            count.setText(editedBottle.count)
            price.setText(editedBottle.price)
            currency.setSelection(0) // TODO: get actual selection
            buyLocation.setText(editedBottle.buyLocation)
            buyDate.setText(DateFormatter.formatDate(editedBottle.buyDate))
        }
    }

    private fun validateFields(): Boolean {
        with(binding) {
            val count = count.text.toString().trim()
            val price = price.text.toString().trim()

            return addBottleViewModel.validateBottle(count, price)
        }
    }

    private fun savePartialBottle() {
        with(binding) {
            val count = count.text.toString().trim()
            val price = price.text.toString().trim()
            val currency = currency.text.toString()
            val location = buyLocation.text.toString().trim()

            addBottleViewModel.setPartialBottle(
                vintage.value,
                apogee.value,
                count,
                price,
                currency,
                location,
                this@FragmentInquireDates.buyDateTimestamp
            )
        }
    }

    override fun onPause() {
        addBottleViewModel.userFeedback.removeObserver(feedBackObserver)
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}