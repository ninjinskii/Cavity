package com.louis.app.cavity.ui.bottle.steps

import android.os.Bundle
import android.view.View
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireDatesBinding
import com.louis.app.cavity.ui.bottle.AddBottleViewModel
import com.louis.app.cavity.ui.bottle.stepper.FragmentStepper
import java.util.*

class FragmentInquireDates : Fragment(R.layout.fragment_inquire_dates) {
    private lateinit var binding: FragmentInquireDatesBinding
    private val addBottleViewModel: AddBottleViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInquireDatesBinding.bind(view)

        registerStepperWatcher()
        initNumberPickers()
    }

    private fun registerStepperWatcher() {
        val stepperFragment =
            parentFragmentManager.findFragmentById(R.id.stepper) as FragmentStepper

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

    private fun validateFields(): Boolean {
        with(binding) {
            val count = count.text.toString().trim()
            var price = price.text.toString().trim()
            val currency = currency.text.toString()
            val location = buyLocation.text.toString().trim()
            val date = buyDate.text.toString()

            if (count.isEmpty() || !count.isDigitsOnly() || count.toInt() <= 0) {
                countLayout.error = resources.getString(R.string.zero_bottle)
                return false
            }

            if (price.isEmpty()) price = "0"

            if (!price.isDigitsOnly()) {
                priceLayout.error = resources.getString(R.string.incorrect_price_format)
                return false
            }

            addBottleViewModel.addBottle(
                vintage.value,
                apogee.value,
                count.toInt(),
                price.toInt(),
                currency,
                location,
                date
            )
        }

        return true
    }
}
