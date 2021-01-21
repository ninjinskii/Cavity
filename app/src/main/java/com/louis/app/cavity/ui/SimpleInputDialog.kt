package com.louis.app.cavity.ui

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogSimpleInputBinding
import com.louis.app.cavity.util.hideKeyboard
import com.louis.app.cavity.util.showKeyboard

class SimpleInputDialog(
    private val context: Context,
    private val layoutInflater: LayoutInflater,
) {
    private val dialogBinding by lazy {
        DialogSimpleInputBinding.inflate(layoutInflater)
    }

    fun show(
        @StringRes title: Int,
        @StringRes hint: Int,
        @DrawableRes icon: Int?,
        onPositiveClick: (String) -> Unit
    ) {
        customizeEditText(hint, icon)
        buildAndShow(title, onPositiveClick)
    }

    fun showForEdit(
        @StringRes title: Int,
        @StringRes hint: Int,
        @DrawableRes icon: Int?,
        editedString: String,
        onPositiveClick: (String) -> Unit
    ) {
        customizeEditText(hint, icon)

        dialogBinding.input.apply {
            setText(editedString)
            setSelection(editedString.length)
        }

        buildAndShow(title, onPositiveClick)
    }

    private fun customizeEditText(@StringRes hint: Int, @DrawableRes icon: Int?) {
        dialogBinding.inputLayout.hint = context.getString(hint)
        icon?.let {
            dialogBinding.inputLayout.startIconDrawable = ContextCompat.getDrawable(context, icon)
        }
    }

    private fun buildAndShow(@StringRes title: Int, onPositiveClick: (String) -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.submit) { _, _ ->
                val input = dialogBinding.input.text.toString().trim()
                onPositiveClick(input)
            }
            .setView(dialogBinding.root)
            .setOnDismissListener { dialogBinding.root.hideKeyboard() }
            .show()

        dialogBinding.input.post { dialogBinding.input.showKeyboard() }
    }
}
