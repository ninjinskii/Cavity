package com.louis.app.cavity.ui.addbottle.stepper

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentStepperBinding
import com.louis.app.cavity.util.setVisible
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

        setListener()
    }

    private fun setListener() {
        binding.endIcon.setOnClickListener {
            accomplished()
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
                    position < currentPagePos -> currentPagePos = position
                    allowedToChangePage(position) && position > currentPagePos -> {
                        listeners[currentPagePos].onPageRequestAccepted()
                        currentPagePos = position
                    }
                    else -> viewPager.currentItem = currentPagePos
                }
            }
        })
    }

    fun addListener(stepperWatcher: StepperWatcher) = listeners.add(stepperWatcher)

    fun requireNextPage() {
        if (allowedToChangePage(currentPagePos + 1))
            viewPager?.currentItem = currentPagePos + 1
    }

    fun accomplished() {
        listeners[currentPagePos].onFinalStepAccomplished()
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
