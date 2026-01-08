package com.louis.app.cavity.ui

import android.content.Context
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogSimpleInputBinding
import com.louis.app.cavity.util.hideKeyboard
import com.louis.app.cavity.util.showKeyboard

class SimpleInputDialog(
    private val context: Context,
    private val layoutInflater: LayoutInflater,
    private val lifecycleOwner: LifecycleOwner,
    private val passwordInput: Boolean = false,
    private val textArea: Boolean = false
) :
    DefaultLifecycleObserver {

    private lateinit var dialogBinding: DialogSimpleInputBinding

    fun show(resources: DialogContent) {
        dialogBinding = DialogSimpleInputBinding.inflate(layoutInflater)
        buildAndShow(resources)
    }

    fun showForEdit(resources: DialogContent, editedString: String) {
        dialogBinding = DialogSimpleInputBinding.inflate(layoutInflater)
        dialogBinding.input.apply {
            setText(editedString)
            setSelection(editedString.length)
        }

        buildAndShow(resources)
    }

    private fun customizeEditText(@StringRes hint: Int, @DrawableRes icon: Int?) {
        if (this.passwordInput) {
            dialogBinding.input.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        if (this.textArea) {
            dialogBinding.inputLayout.gravity = Gravity.TOP
            with(dialogBinding.input) {
                minLines = 7
                gravity = Gravity.TOP
                inputType =
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            }
        }

        dialogBinding.inputLayout.hint = context.getString(hint)
        icon?.let {
            dialogBinding.inputLayout.startIconDrawable = ContextCompat.getDrawable(context, icon)
        }
    }

    private fun buildAndShow(resources: DialogContent) {
        customizeEditText(resources.hint, resources.icon)

        val dialog = LifecycleMaterialDialogBuilder(context, lifecycleOwner)
            .setTitle(resources.title)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.submit) { _, _ ->
                val input = dialogBinding.input.text.toString().trim()
                resources.onPositiveClick(input)
            }
            .setView(dialogBinding.root)
            .setOnDismissListener { dialogBinding.root.hideKeyboard() }

        resources.message?.let { dialog.setMessage(context.getString(it)) }
        dialog.show()

        dialogBinding.input.post { dialogBinding.input.showKeyboard() }
    }

    data class DialogContent(
        @StringRes val title: Int,
        @StringRes val message: Int? = null,
        @StringRes val hint: Int,
        @DrawableRes val icon: Int? = null,
        val onPositiveClick: (String) -> Unit
    )
}
