package com.louis.app.cavity.ui.bottle

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentStepperBinding
import com.louis.app.cavity.util.setVisible
import kotlinx.android.synthetic.main.fragment_stepper.*

class FragmentStepper : Fragment(R.layout.fragment_stepper) {
    private lateinit var binding: FragmentStepperBinding
    private lateinit var stepIcons: List<Int>
    private lateinit var stepViews: List<ImageView>
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
            val stepsViews = setOf(step1, step2, step3, step4)

            stepsViews.forEachIndexed { index, imageView ->
                imageView.setOnClickListener { stepperViewModel.goToStep(index) }
            }

            buttonNext.setOnClickListener {
                if (stepperViewModel.goToNextStep()) {
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

        if (lookBehind) updateStepsOnLookBehind(step, validSteps)
        else updateSteps(step)
    }

    private fun updateSteps(viewedStep: Int) {
        stepViews.forEachIndexed { index, imageView ->
            if (index < viewedStep) imageView.setImageResource(R.drawable.ic_check)
        }

        progressBar.setProgress(30 * viewedStep, true)
    }

    private fun updateStepsOnLookBehind(viewedStep: Int, validSteps: Int) {
        stepViews.forEachIndexed { index, imageView ->
            if (index != viewedStep && index < validSteps)
                imageView.setImageResource(R.drawable.ic_check)
        }

        stepViews[viewedStep].setImageResource(stepIcons[viewedStep])
    }

    interface OnStepChange {
        fun onStepChange(step: Int)
    }
}