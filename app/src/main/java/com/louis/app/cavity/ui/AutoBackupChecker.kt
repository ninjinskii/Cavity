package com.louis.app.cavity.ui

import android.content.Context
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.louis.app.cavity.R
import com.louis.app.cavity.ui.settings.SettingsViewModel

class AutoBackupChecker(
    settingsViewModel: SettingsViewModel,
    private val viewLifecycleOwner: LifecycleOwner,
    private val navController: NavController
) {

    init {
        settingsViewModel.fetchCanAutoBackup()
    }

    fun setupToolbarMenu(toolbar: Toolbar) {
        if (toolbar.context == null) {
            return
        }

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.fixAutBackup -> {
                    onBackupFailedClick(toolbar.context)
                    true
                }

                else -> false
            }
        }
    }

    private fun onBackupFailedClick(context: Context) {
        LifecycleMaterialDialogBuilder(context, viewLifecycleOwner)
            .setTitle(R.string.auto_backup_failed_title)
            .setMessage(R.string.auto_backup_unauthorized)
            .setPositiveButton(R.string.login) { _, _ ->
                navController.navigate(R.id.account_dest)
            }
            .show()
    }
}
