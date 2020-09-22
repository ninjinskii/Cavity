package com.louis.app.cavity.ui.bottle.steps

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
import com.louis.app.cavity.ui.bottle.AddBottleViewModel
import com.louis.app.cavity.ui.bottle.stepper.FragmentStepper
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.showSnackbar
import java.text.SimpleDateFormat
import java.util.*

class FragmentInquireDates : Fragment(R.layout.fragment_inquire_dates) {
    private lateinit var binding: FragmentInquireDatesBinding
    private lateinit var stepperFragment: FragmentStepper
    private lateinit var feedBackObserver: Observer<Event<Int>>
    private lateinit var datePicker: MaterialDatePicker<Long>
    private var buyDate = ""
    private val addBottleViewModel: AddBottleViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInquireDatesBinding.bind(view)

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

        binding.buyDate.apply {
            inputType = InputType.TYPE_NULL
            datePicker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText(R.string.buying_date)
                .build()

            datePicker.addOnPositiveButtonClickListener {
                formatDate()
                setText(datePicker.headerText.toString())
            }

            setOnClickListener { datePicker.show(childFragmentManager, "CALENDAR") }

            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) datePicker.show(childFragmentManager, "CALENDAR")
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
            buyDate.setText(editedBottle.buyDate)
            formatDate()
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
                this@FragmentInquireDates.buyDate
            )
        }
    }

    private fun formatDate() {
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.FRENCH)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = datePicker.selection ?: return
        buyDate = formatter.format(calendar.time)
    }

    override fun onPause() {
        addBottleViewModel.userFeedback.removeObserver(feedBackObserver)
        super.onPause()
    }
}
