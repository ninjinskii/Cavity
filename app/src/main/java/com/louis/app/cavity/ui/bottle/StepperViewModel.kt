package com.louis.app.cavity.ui.bottle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.util.L
import java.lang.Exception

class StepperViewModel(app: Application) : AndroidViewModel(app) {
    val finalStep = 3

    private val _step = MutableLiveData(0 to false)
    val step: LiveData<Pair<Int, Boolean>>
        get() = _step

    private val _lastValidStep = MutableLiveData(0)
    val lastValidStep: LiveData<Int>
        get() = _lastValidStep

    fun goToNextStep() : Boolean {
        val currentStep = _step.value?.first ?: 0
        val currentLastValidStep = _lastValidStep.value ?: throw Exception("lastValisStep is null")
        return if (currentStep + 1 <= finalStep) {
            val wasLookingBehind = currentStep < currentLastValidStep
            _step.postValue(currentStep + 1 to wasLookingBehind)

            if(!wasLookingBehind) _lastValidStep.postValue(currentStep + 1)

            false
        } else {
            true
        }
    }

    fun goToPreviousStep() {
        val currentStep = _step.value?.first ?: 0
        if (currentStep > 0) _step.postValue( currentStep - 1 to true)
    }

    fun goToStep(step: Int) {
        val currentLastValidStep = _lastValidStep.value ?: 0
        L.v("$step $currentLastValidStep")
        val isLookingBehind = currentLastValidStep > step

        if (step in 0..currentLastValidStep) {
            _step.postValue(step to isLookingBehind)
        }
    }
}