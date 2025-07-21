package com.louis.app.cavity.util

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

abstract class PermissionChecker(private val fragment: Fragment, private val perms: Array<String>) {
    private val isAndroid13OrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    private val launcher =
        fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            this.handlePermissionResult(it)
        }

    // Permission that are not needed in Android 13 or later
    private val ignoredPerms: Array<String> =
        if (isAndroid13OrHigher) {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            emptyArray()
        }

    private val permissions = perms.filter { it !in ignoredPerms }

    init {
        if (perms.isEmpty()) {
            throw IllegalArgumentException("Must pass at least one permission to PermissionChecker")
        }
    }

    fun askPermissionsIfNecessary(runIfAlreadyAccepted: Boolean = true) {
        if (!hasPermissions()) {
            launcher.launch(perms)
            return
        } else if (runIfAlreadyAccepted) {
            onPermissionsAccepted()
        }
    }

    private fun hasPermissions(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        val perms = permissions.filter { it.key !in ignoredPerms }

        if (perms.all { it.value }) {
            onPermissionsAccepted()
        } else {
            onPermissionsDenied()
        }
    }

    abstract fun onPermissionsAccepted()

    abstract fun onPermissionsDenied()
}
