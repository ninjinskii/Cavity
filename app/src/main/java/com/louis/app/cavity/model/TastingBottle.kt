package com.louis.app.cavity.model

data class TastingBottle(
    val bottleId: Long,
    val wine: Wine,
    val vintage: Int,
    var drinkTemp: Temperature,
    var jugTime: Int?,
    var isSelected: Boolean = false
)
