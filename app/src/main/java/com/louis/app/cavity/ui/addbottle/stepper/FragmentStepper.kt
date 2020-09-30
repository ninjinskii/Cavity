package com.louis.app.cavity.ui.addbottle.stepper

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentStepperBinding
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentStepper : Fragment(R.layout.fragment_stepper) {
    private var _binding: FragmentStepperBinding? = null
    private val binding get() = _binding!!
    private lateinit var stepStrings: List<String>
    private lateinit var stepNumbers: List<String>
    private val listeners = mutableListOf<StepperWatcher>()
    private var viewPager: ViewPager2? = null
    private var currentPagePos = 0
    private val forwardAnimations by lazy {
        AnimationUtils.loadAnimation(activity, R.anim.slide_in_right) to
                AnimationUtils.loadAnimation(activity, R.anim.slide_out_left)
    }
    private val backwardAnimations by lazy {
        AnimationUtils.loadAnimation(activity, R.anim.slide_in_right) to
                AnimationUtils.loadAnimation(activity, R.anim.slide_out_right)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStepperBinding.bind(view)

        stepStrings = listOf(
            resources.getString(R.string.step_1_text),
            resources.getString(R.string.step_2_text),
            resources.getString(R.string.step_3_text),
            resources.getString(R.string.step_4_text)
        )

        stepNumbers = listOf(
            resources.getString(R.string.step_1),
            resources.getString(R.string.step_2),
            resources.getString(R.string.step_3),
            resources.getString(R.string.step_4)
        )

        initTextSwitcher()
        setListener()
    }

    private fun initTextSwitcher() {
        with(binding) {
            switcher.setFactory {
                TextView(ContextThemeWrapper(activity, R.style.CavityTheme), null, 0).apply {
                    setTextAppearance(R.style.TextAppearance_MaterialComponents_Headline5)
                    setTextColor(resources.getColor(R.color.colorSecondary, context.theme))
                }
            }

            switcher.setText(stepStrings[0])
            stepNumber.text = stepNumbers[0]
        }
    }

    private fun setListener() {
        binding.endIcon.setOnClickListener {
            accomplished()
        }
    }

    private fun animateForward(newStep: Int) {
        binding.switcher.apply {
            inAnimation = forwardAnimations.first
            outAnimation = forwardAnimations.second
        }

        lifecycleScope.launch(Main) {
            with(binding) {
                prograssBarMain.setProgress(100, true)
                delay(200)
                progressBarStart.setProgress(0, true)
                switcher.setText(stepStrings[newStep])
                delay(200)
                stepNumber.text = (newStep + 1).toString()
                prograssBarMain.setProgress(0, true)
                progressBarStart.apply {
                    setVisible(true)
                    setProgress(100, true)
                }

                if (newStep == 3) endIcon.setVisible(true)
                else endIcon.setVisible(false)
            }
        }
    }

    private fun animateBackward(newStep: Int) {
        binding.switcher.apply {
            inAnimation = backwardAnimations.first
            outAnimation = backwardAnimations.second
        }

        lifecycleScope.launch(Main) {
            with(binding) {
                endIcon.setVisible(false)
                progressBarStart.setProgress(0, true)
                delay(200)
                switcher.setText(stepStrings[newStep])
                delay(200)
                stepNumber.text = (newStep + 1).toString()
                progressBarStart.setProgress(100, true)
                if (newStep == 0) progressBarStart.setVisible(false)
            }
        }
    }

    private fun animateEnd() {
        lifecycleScope.launch(Main) {
            binding.prograssBarMain.setProgress(100, true)
            delay(100)
            //animate endIcon
            delay(100)
        }
    }

    private fun allowedToChangePage(index: Int): Boolean {
        return try {
            if (index <= currentPagePos) true
            else listeners[currentPagePos].onRequestChangePage()
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException(
                "One or multiple fragments has not been registered to the stepper"
            )
        }
    }

    fun setupWithViewPager(viewPager: ViewPager2) {
        this.viewPager = viewPager

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when {
                    position < currentPagePos -> {
                        animateBackward(position)
                        currentPagePos = position
                    }
                    allowedToChangePage(position) && position > currentPagePos -> {
                        listeners[currentPagePos].onPageRequestAccepted()
                        animateForward(position)
                        currentPagePos = position
                    }
                    else -> viewPager.currentItem = currentPagePos
                }
            }
        })
    }

    fun addListener(stepperWatcher: StepperWatcher) = listeners.add(stepperWatcher)

    fun requireNextPage() {
        if (allowedToChangePage(currentPagePos + 1)) viewPager?.currentItem = currentPagePos + 1
    }

    fun accomplished() {
        animateEnd()

        lifecycleScope.launch(Main) {
            delay(200)
            listeners[currentPagePos].onFinalStepAccomplished()
        }
    }

    interface StepperWatcher {
        fun onRequestChangePage(): Boolean
        fun onPageRequestAccepted()
        fun onFinalStepAccomplished() {
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
