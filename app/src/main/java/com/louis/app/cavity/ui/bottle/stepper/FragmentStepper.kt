package com.louis.app.cavity.ui.bottle.stepper

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentStepperBinding
import com.louis.app.cavity.util.setVisible
import kotlinx.android.synthetic.main.fragment_stepper.*
import java.lang.Exception
import java.lang.IndexOutOfBoundsException

class FragmentStepper : Fragment(R.layout.fragment_stepper) {
    private lateinit var binding: FragmentStepperBinding
    private lateinit var stepViews: List<TextView>
    private lateinit var cursors: List<View>
    private val listeners = mutableListOf<StepperWatcher>()
    private val stepperViewModel: StepperViewModel by activityViewModels()
    private var viewPager: ViewPager2? = null
    private var handlingClickButton = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStepperBinding.bind(view)
        stepperViewModel.reset()

        with(binding) {
            stepViews = listOf(step1, step2, step3, step4)
            cursors = listOf(cursor1, cursor2, cursor3, cursor4)
        }

        setListeners()
        observe()
    }

    private fun setListeners() {
        with(binding) {
            stepViews.forEachIndexed { index, imageView ->
                imageView.setOnClickListener {
                    if (allowedToChangePage(index)) stepperViewModel.goToStep(index)
                }
            }

            buttonNext.setOnClickListener {
                // Dummy index cause we are moving forward
                if (allowedToChangePage(10)) {
                    handlingClickButton = true

                    if (stepperViewModel.goToNextStep()) {
                        // Observers won't be triggered so we tell that we handle the click
                        handlingClickButton = false
                        // Stepper meets end, callback to parent fragment
                    }
                }
            }

            buttonPrevious.setOnClickListener {
                stepperViewModel.goToPreviousStep()
            }
        }
    }

    private fun observe() {
        stepperViewModel.step.observe(viewLifecycleOwner) {
            viewPager?.let { viewPager -> viewPager.currentItem = it.first }
            animateStepTransition(it.first, it.second)
            handlingClickButton = false
        }
    }

    private fun animateStepTransition(step: Int, lookBehind: Boolean = false) {
        cursors.forEachIndexed { index, view -> view.setVisible(index == step) }
        if (!lookBehind) updateSteps(step)
    }

    private fun updateSteps(viewedStep: Int) {
        progressBar.setProgress(33 * viewedStep, true)
    }

    fun setupWithViewPager(viewPager: ViewPager2) {
        this.viewPager = viewPager

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (!handlingClickButton) stepperViewModel.goToStep(position)
            }
        })
    }

    private fun allowedToChangePage(index: Int): Boolean {
        val currentStep = stepperViewModel.step.value?.first ?: 0

        try {
            return if (index <= currentStep) true
            else listeners[currentStep].onRequestChangePage()
        } catch (e: IndexOutOfBoundsException) {
            throw Exception("One or multiple fragments has not been registered to the stepper")
        }
    }

    fun addListener(stepperWatcher: StepperWatcher) = listeners.add(stepperWatcher)

    fun resetState() {
        listeners.clear()
        stepperViewModel.reset()
    }

    interface StepperWatcher {
        fun onRequestChangePage(): Boolean
    }
}
