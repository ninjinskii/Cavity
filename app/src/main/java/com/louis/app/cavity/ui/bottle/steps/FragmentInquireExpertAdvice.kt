package com.louis.app.cavity.ui.bottle.steps

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireExpertAdviceBinding
import com.louis.app.cavity.ui.bottle.stepper.FragmentStepper

class FragmentInquireExpertAdvice : Fragment(R.layout.fragment_inquire_expert_advice) {
    private lateinit var binding: FragmentInquireExpertAdviceBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInquireExpertAdviceBinding.bind(view)

        registerStepperWatcher()
    }

    private fun registerStepperWatcher() {
        val stepperFragment =
            parentFragmentManager.findFragmentById(R.id.stepper) as FragmentStepper

        stepperFragment.addListener(object : FragmentStepper.StepperWatcher {
            override fun onRequestChangePage(): Boolean {
//                val textField = listOf(binding.count)
//                val errorString = resources.getString(R.string.required_field)
//
//                if (checkAllRequiredFieldsFilled(textField, errorString, binding.coordinator)) {
//                    return validateFieldsContent()
//                }
//
//                return false
                return true
            }
        })
    }
}