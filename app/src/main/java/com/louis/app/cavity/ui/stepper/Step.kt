package com.louis.app.cavity.ui.stepper

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class Step(@LayoutRes layout: Int) : Fragment(layout) {
    protected var stepperFragment: Stepper? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            stepperFragment = parentFragment as Stepper
        } catch (e: ClassCastException) {
            if (parentFragment == null) {
                throw IllegalStateException("Parent fragment is null")
            }
        }
    }

    fun setPeekSiblingsSteps(peekSiblingsSteps: Boolean) {
        stepperFragment?.setPeekSiblingsSteps(peekSiblingsSteps)
    }

    open fun requestNextPage() = true
}
