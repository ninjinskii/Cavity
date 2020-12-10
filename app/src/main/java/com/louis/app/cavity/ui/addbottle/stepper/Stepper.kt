package com.louis.app.cavity.ui.addbottle.stepper

interface Stepper {
    fun requestNextPage()
    fun requestPreviousPage()
    fun getBottleId(): Long
}
