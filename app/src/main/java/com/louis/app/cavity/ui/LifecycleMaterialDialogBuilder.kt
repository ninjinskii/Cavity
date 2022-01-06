package com.louis.app.cavity.ui

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * A material dialog that close itself when the registered lifecycle owner reach "on destroy" state
 */
class LifecycleMaterialDialogBuilder(
    context: Context,
    private val lifecycleOwner: LifecycleOwner
) :
    MaterialAlertDialogBuilder(context) {

    override fun show(): AlertDialog {
        return super.show().also {
            lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    it.dismiss()
                }
            })
        }
    }
}
