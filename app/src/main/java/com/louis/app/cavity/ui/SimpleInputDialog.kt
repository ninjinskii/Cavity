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

class SimpleInputDialog(private val context: Context, private val layoutInflater: LayoutInflater) {
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
        dialogBinding.inputLayout.hint = context.getString(hint)
        icon?.let {
            dialogBinding.inputLayout.startIconDrawable = ContextCompat.getDrawable(context, icon)
        }
    }

    private fun buildAndShow(resources: DialogContent) {
        customizeEditText(resources.hint, resources.icon)

        val dialog = MaterialAlertDialogBuilder(context)
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
        val onPositiveClick: (String) -> Unit,
    )
}
