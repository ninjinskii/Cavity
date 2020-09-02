package com.louis.app.cavity.ui.bottle

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentStepperBinding
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.setVisible
import kotlinx.android.synthetic.main.fragment_stepper.*

class FragmentStepper : Fragment(R.layout.fragment_stepper) {
    private lateinit var binding: FragmentStepperBinding
    private lateinit var stepIcons: List<Int>
    private lateinit var stepViews: List<TextView>
    private lateinit var cursors: List<View>
    private lateinit var onStepChange: OnStepChange
    private val stepperViewModel: StepperViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStepperBinding.bind(view)

        onStepChange = parentFragment as OnStepChange

        with(binding) {
            stepViews = listOf(step1, step2, step3, step4)
            cursors = listOf(cursor1, cursor2, cursor3, cursor4)
            stepIcons = listOf(
                R.drawable.ic_step_1,
                R.drawable.ic_step_2,
                R.drawable.ic_step_3,
                R.drawable.ic_step_4
            )
        }

        setListeners()
        observe()
    }

    private fun setListeners() {
        with(binding) {
            stepViews.forEachIndexed { index, imageView ->
                imageView.setOnClickListener { stepperViewModel.goToStep(index) }
            }

            buttonNext.setOnClickListener {
                if (stepperViewModel.goToNextStep()) {
                    L.v("Stepper end", "STEPPER")
                    animateStepTransition(
                        stepperViewModel.finalStep,
                        stepperViewModel.lastValidStep.value ?: 0
                    )
                    // Stepper meets end, callback to parent fragment
                }
            }

            buttonPrevious.setOnClickListener {
                stepperViewModel.goToPreviousStep()
            }
        }
    }

    private fun observe() {
        stepperViewModel.step.observe(viewLifecycleOwner) {
            onStepChange.onStepChange(it.first)
            animateStepTransition(it.first, stepperViewModel.lastValidStep.value ?: 0, it.second)
        }
    }

    private fun animateStepTransition(step: Int, validSteps: Int, lookBehind: Boolean = false) {
        cursors.forEachIndexed { index, view -> view.setVisible(index == step) }
        if (!lookBehind) updateSteps(step)
    }

    private fun updateSteps(viewedStep: Int) {
        progressBar.setProgress(33 * viewedStep, true)
    }

    interface OnStepChange {
        fun onStepChange(step: Int)
    }
}