package com.louis.app.cavity.ui.bottle.steps

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireExpertAdviceBinding
import com.louis.app.cavity.model.ExpertAdvice
import com.louis.app.cavity.ui.bottle.AddBottleViewModel
import com.louis.app.cavity.ui.bottle.stepper.FragmentStepper
import com.louis.app.cavity.util.showSnackbar

class FragmentInquireExpertAdvice : Fragment(R.layout.fragment_inquire_expert_advice) {
    private lateinit var binding: FragmentInquireExpertAdviceBinding
    private lateinit var adviceAdapter: ExpertAdviceRecyclerAdapter
    private val addBottleViewModel: AddBottleViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInquireExpertAdviceBinding.bind(view)

        //registerStepperWatcher() // useless ?
        initRecyclerView()
        setListener()
    }

    private fun registerStepperWatcher() {
        val stepperFragment =
            parentFragmentManager.findFragmentById(R.id.stepper) as FragmentStepper

        stepperFragment.addListener(object : FragmentStepper.StepperWatcher {
            override fun onRequestChangePage() = true
        })
    }

    private fun initRecyclerView() {
        adviceAdapter = ExpertAdviceRecyclerAdapter {
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = adviceAdapter
        }

        addBottleViewModel.expertAdvices.observe(viewLifecycleOwner) {
            adviceAdapter.submitList(it.toMutableList()) // Force submitList to trigger
        }
    }

    private fun setListener() {
        binding.buttonAddExpertAdvice.setOnClickListener {
            val constestName = binding.contestName.text.toString()
            val rate = binding.rate.text.toString()

            if (validateFieds()) {
                val advice = makeExpertAdvice(constestName, rate.toInt())
                addBottleViewModel.addExpertAdvice(advice)
            }
        }
    }

    private fun validateFieds(): Boolean {
        with(binding) {
            val contestName = binding.contestName.text.toString()
            val rate = binding.rate.text.toString()

            if (contestName.isEmpty()) return false

            try {
                if (rbGroupType.checkedRadioButtonId == R.id.rbRate20) {
                    return rate.toInt() in 0..20
                }
            } catch (e: NumberFormatException) {
                coordinator.showSnackbar(R.string.rate_outside_0_to_20)
            }

            try {
                if (rbGroupType.checkedRadioButtonId == R.id.rbRate100) {
                    return rate.toInt() in 0..100
                }
            } catch (e: NumberFormatException) {
                coordinator.showSnackbar(R.string.rate_outside_0_to_100)
            }
        }

        return true
    }

    private fun makeExpertAdvice(contestName: String, rate: Int): ExpertAdvice {
        with(binding) {
            return when (rbGroupType.checkedRadioButtonId) {
                R.id.rbMedal -> {
                    val value = when {
                        rbBronze.isChecked -> 0
                        rbSilver.isChecked -> 1
                        else -> 2
                    }
                    ExpertAdvice(0, contestName, 1, 0, 0, 0, value, 0)
                }
                R.id.rbRate100 -> ExpertAdvice(
                    0,
                    contestName,
                    0,
                    0,
                    0,
                    1,
                    rate,
                    0
                )
                R.id.rbRate20 -> ExpertAdvice(
                    0,
                    contestName,
                    0,
                    0,
                    1,
                    0,
                    rate,
                    0
                )
                else -> {
                    val value = when {
                        rbStar1.isChecked -> 0
                        rbStar2.isChecked -> 1
                        else -> 2
                    }
                    ExpertAdvice(
                        0,
                        contestName,
                        0,
                        1,
                        0,
                        0,
                        value,
                        0
                    )
                }
            }
        }
    }
}