package com.louis.app.cavity.ui.bottle.stepper

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentStepperBinding
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.setVisible
import kotlinx.android.synthetic.main.fragment_stepper.*

class FragmentStepper : Fragment(R.layout.fragment_stepper) {
    private lateinit var binding: FragmentStepperBinding
    private lateinit var cursors: List<View>
    private val listeners = mutableListOf<StepperWatcher>()
    private val stepperViewModel: StepperViewModel by activityViewModels()
    private var viewPager: ViewPager2? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStepperBinding.bind(view)

        with(binding) {
            cursors = listOf(cursor1, cursor2, cursor3, cursor4)
        }

        setListeners()
        observe()
    }

    private fun setListeners() {
        with(binding) {
            buttonNext.setOnClickListener {
                if (allowedToChangePage(getCurrentStep() + 1)) {
                    //handlingClickButton = true

                    if (stepperViewModel.goToNextStep()) {
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
            viewPager?.let { viewPager -> viewPager.currentItem = it }
            animateStepTransition(it)
        }
    }

    private fun animateStepTransition(step: Int) {
        cursors.forEachIndexed { index, view -> view.setVisible(index == step) }
        progressBar.setProgress(33 * step, true)
    }

    fun setupWithViewPager(viewPager: ViewPager2) {
        this.viewPager = viewPager

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when {
                    position < getCurrentStep() -> stepperViewModel.goToPreviousStep()
                    allowedToChangePage(position) -> {
                        stepperViewModel.reachedPage(position)
                        animateStepTransition(position)
                    }
                    else -> viewPager.currentItem = getCurrentStep()
                }
            }
        })
    }

    private fun allowedToChangePage(index: Int): Boolean {
        val currentStep = getCurrentStep()

        return try {
            if (index <= currentStep) true
            else listeners[currentStep].onRequestChangePage()
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException(
                "One or multiple fragments has not been registered to the stepper"
            )
        }
    }

    fun addListener(stepperWatcher: StepperWatcher) = listeners.add(stepperWatcher)

    fun resetState() {
        listeners.clear()
        stepperViewModel.reset()
    }

    private fun getCurrentStep() = stepperViewModel.step.value ?: 0

    interface StepperWatcher {
        fun onRequestChangePage(): Boolean
    }
}
