package com.louis.app.cavity.ui.navigation

import androidx.annotation.IdRes

interface NavigationDestination {
    @get:IdRes
    val menuDestinationId: Int
}
