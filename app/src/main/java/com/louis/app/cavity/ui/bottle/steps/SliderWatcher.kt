package com.louis.app.cavity.ui.bottle.steps

interface SliderWatcher {
    fun isValueAllowed(value: Int) : Boolean

    // Returns max value that can be set to the slider
    fun onValueRejected() : Int
}