package com.louis.app.cavity.domain

import android.os.Build
import com.louis.app.cavity.BuildConfig

object Environment {
    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercaseChar() }
        val deviceName = Build.PRODUCT.replaceFirstChar { it.uppercaseChar() }

        return "$manufacturer - $deviceName"
    }

    fun isDebugMode() = BuildConfig.DEBUG
}
