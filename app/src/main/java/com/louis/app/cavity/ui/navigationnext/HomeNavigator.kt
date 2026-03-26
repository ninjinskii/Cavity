package com.louis.app.cavity.ui.navigationnext

import com.louis.app.cavity.ui.home.FragmentHomeDirections

class HomeNavigator {
    fun toBottleDetails(wineId: Long) =
        FragmentHomeDirections.homeToBottleDetails(wineId, -1)

    fun toAddBottle(wineId: Long) =
        FragmentHomeDirections.homeToAddBottle(wineId, -1)

}
