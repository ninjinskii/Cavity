package com.louis.app.cavity.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.transition.Transition
import com.louis.app.cavity.ui.navigation.AppRoute
import com.louis.app.cavity.ui.navigation.TransitionSpec
import com.louis.app.cavity.util.access

class SharedViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    var fragmentTransition: Any? by savedStateHandle access "bruh"
    var route: AppRoute? = null

    fun setOnEtransition(transition: TransitionSpec) {
        fragmentTransition = transition
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                SharedViewModel(createSavedStateHandle())
            }
        }
    }
}
