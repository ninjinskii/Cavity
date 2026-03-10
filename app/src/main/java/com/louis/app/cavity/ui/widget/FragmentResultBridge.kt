package com.louis.app.cavity.ui.widget

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment

class FragmentResultBridge<T : Parcelable>(val fragment: Fragment, val requestKey: String) {
    inline fun <reified T : Parcelable> listen(noinline onResult: (T?) -> Unit) {
        fragment.parentFragmentManager.setFragmentResultListener(
            requestKey,
            fragment.viewLifecycleOwner
        ) { _, bundle ->
            val result: T? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable("$requestKey-result", T::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable("$requestKey-result")
            }

            onResult(result)
        }
    }

    fun put(result: T) {
        val result = Bundle().apply { putParcelable("$requestKey-result", result) }
        fragment.parentFragmentManager.setFragmentResult(requestKey, result)
    }
}
