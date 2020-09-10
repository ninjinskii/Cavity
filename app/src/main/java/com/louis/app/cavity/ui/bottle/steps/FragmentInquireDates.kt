package com.louis.app.cavity.ui.bottle.steps

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireDatesBinding
import com.louis.app.cavity.ui.bottle.AddBottleViewModel
import com.louis.app.cavity.ui.bottle.stepper.FragmentStepper
import com.louis.app.cavity.util.showSnackbar
import java.util.*

class FragmentInquireDates : Fragment(R.layout.fragment_inquire_dates) {
    private lateinit var binding: FragmentInquireDatesBinding
    private lateinit var stepperFragment: FragmentStepper
    private val addBottleViewModel: AddBottleViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInquireDatesBinding.bind(view)

        registerStepperWatcher()
        initNumberPickers()
        initCurrencyDropdown()
        setListener()
        observe()
    }

    private fun registerStepperWatcher() {
        stepperFragment = parentFragmentManager.findFragmentById(R.id.stepper) as FragmentStepper

        stepperFragment.addListener(object : FragmentStepper.StepperWatcher {
            override fun onRequestChangePage() = validateFields()
        })
    }

    private fun initNumberPickers() {
        val year = Calendar.getInstance().get(Calendar.YEAR)

        with(binding) {
            vintage.minValue = year - 20
            vintage.maxValue = year
            vintage.value = year - 5

            apogee.minValue = year
            apogee.maxValue = year + 30
            apogee.value = year + 5
        }
    }

    private fun initCurrencyDropdown() {
        val items = resources.getStringArray(R.array.currencies)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)
        binding.currency.setAdapter(adapter)
    }

    private fun setListener() {
        binding.buttonNext.setOnClickListener {
            stepperFragment.requireNextPage()
        }
    }

    private fun observe() {
        addBottleViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                binding.coordinator.showSnackbar(stringRes)
            }
        }
    }

    private fun validateFields(): Boolean {
        with(binding) {
            val count = count.text.toString().trim()
            val price = price.text.toString().trim()
            val currency = currency.text.toString()
            val location = buyLocation.text.toString().trim()
            val date = buyDate.text.toString()

            return addBottleViewModel.addBottle(
                vintage.value,
                apogee.value,
                count,
                price,
                currency,
                location,
                date
            )
        }
    }

    override fun onResume() {
        super.onResume()
        addBottleViewModel.removeNotCompletedBottle()
    }
}
