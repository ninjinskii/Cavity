package com.louis.app.cavity.ui.bottle.stepper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.util.L

class StepperViewModel(app: Application) : AndroidViewModel(app) {
    private val finalStep = 3

    private val _step = MutableLiveData(0)
    val step: LiveData<Int>
        get() = _step

    fun goToNextStep(): Boolean {
        val currentStep = _step.value ?: 0

        return if (currentStep + 1 <= finalStep) {
            _step.postValue(currentStep + 1)
            false
        } else {
            true
        }
    }

    fun goToPreviousStep() {
        val currentStep = _step.value ?: 0
        if (currentStep > 0) _step.postValue(currentStep - 1)
    }

    // Called when the user swipe to the next page and StepperWatcher allows him to do so
//    fun reachedPage(index: Int) {
//        _step.value = index
//    }

    fun reset() {
        _step.postValue(0)
    }
}