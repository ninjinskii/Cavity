package com.louis.app.cavity.ui

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.louis.app.cavity.ui.navigation.MaterialTransitionExecutor
import com.louis.app.cavity.ui.navigation.TransitionSpec
import com.louis.app.cavity.ui.navigation.navigator

open class TransitionFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        navigator.restoreTransitions(this)
    }
}
