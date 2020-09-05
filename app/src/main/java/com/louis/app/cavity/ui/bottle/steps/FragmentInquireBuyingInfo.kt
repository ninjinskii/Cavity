package com.louis.app.cavity.ui.bottle.steps

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireBuyingInfoBinding
import com.louis.app.cavity.ui.bottle.stepper.FragmentStepper

class FragmentInquireBuyingInfo : Fragment(R.layout.fragment_inquire_buying_info), FormValidator {
    private lateinit var binding: FragmentInquireBuyingInfoBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInquireBuyingInfoBinding.bind(view)

        registerStepperWatcher()
    }

    private fun registerStepperWatcher() {
        val stepperFragment =
            parentFragmentManager.findFragmentById(R.id.stepper) as FragmentStepper

        stepperFragment.addListener(object : FragmentStepper.StepperWatcher {
            override fun onRequestChangePage(): Boolean {
                val textFields = with(binding) { listOf(price, currency, buyDate, buyLocation) }
                val errorString = resources.getString(R.string.required_field)

                return checkAllRequiredFieldsFilled(textFields, errorString, binding.coordinator)
            }
        })
    }
}