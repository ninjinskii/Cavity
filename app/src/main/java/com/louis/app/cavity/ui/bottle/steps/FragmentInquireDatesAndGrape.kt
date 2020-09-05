package com.louis.app.cavity.ui.bottle.steps

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireDatesAndGrapeBinding
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.ui.bottle.AddBottleViewModel
import com.louis.app.cavity.ui.bottle.stepper.FragmentStepper
import java.util.*

class FragmentInquireDatesAndGrape : Fragment(R.layout.fragment_inquire_dates_and_grape),
    FormValidator {
    private lateinit var binding: FragmentInquireDatesAndGrapeBinding
    private lateinit var grapeAdapter: GrapeRecyclerAdapter
    private val addBottleViewModel: AddBottleViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInquireDatesAndGrapeBinding.bind(view)

        registerStepperWatcher()
        initNumberPickers()
        initRecyclerView()
        setListener()
    }

    private fun registerStepperWatcher() {
        val stepperFragment =
            parentFragmentManager.findFragmentById(R.id.stepper) as FragmentStepper

        stepperFragment.addListener(object : FragmentStepper.StepperWatcher {
            override fun onRequestChangePage(): Boolean {
                val textField = listOf(binding.count)
                val errorString = resources.getString(R.string.required_field)

                if (checkAllRequiredFieldsFilled(textField, errorString, binding.coordinator)) {
                    return validateFieldsContent()
                }

                return false
            }
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

    private fun initRecyclerView() {
        grapeAdapter = GrapeRecyclerAdapter {
            addBottleViewModel.removeGrape(it)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = grapeAdapter
        }

        addBottleViewModel.grapes.observe(viewLifecycleOwner) {
            grapeAdapter.submitList(it.toMutableList()) // Force submitList to trigger
        }
    }

    private fun setListener() {
        binding.buttonAddGrape.setOnClickListener {
            val grapeName = binding.grapeName.text.toString()
            val defaultPercentage = if (grapeAdapter.currentList.size >= 1) 0 else 25

            if (!addBottleViewModel.alreadyContainsGrape(grapeName)) {
                addBottleViewModel.addGrape(Grape(0, grapeName, defaultPercentage, 0))
            }
        }
    }

    private fun validateFieldsContent(): Boolean {
        return if (binding.count.text.toString().toInt() > 0) {
            true
        } else {
            binding.count.error = resources.getString(R.string.zero_bottle)
            false
        }
    }
}
