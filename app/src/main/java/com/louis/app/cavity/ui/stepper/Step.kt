package com.louis.app.cavity.ui.stepper

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class Step(@LayoutRes layout: Int) : Fragment(layout) {
    protected lateinit var stepperFragment: Stepper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            stepperFragment = parentFragment as Stepper
        } catch (e: ClassCastException) {
            if (parentFragment == null) {
                throw IllegalStateException("Parent is null")
            } else {
                throw IllegalStateException("Step should be a child of stepper. Parent is: ${parentFragment!!::class.java.name}")
            }
        }
    }
}
