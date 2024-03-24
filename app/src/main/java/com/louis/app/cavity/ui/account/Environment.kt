package com.louis.app.cavity.ui.account

import android.os.Build

object Environment {
    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercaseChar() }
        val deviceName = Build.PRODUCT.replaceFirstChar { it.uppercaseChar() }

        return "$manufacturer - $deviceName"
    }
}
