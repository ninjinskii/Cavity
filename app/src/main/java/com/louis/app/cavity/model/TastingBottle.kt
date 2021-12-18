package com.louis.app.cavity.model

data class TastingBottle(
    val bottleId: Long,
    val wine: Wine,
    val vintage: Int,
    val size: BottleSize,
    var shouldFridge: Int = 0,
    var shouldJug: Int = 0,
    var isSelected: Boolean = false,
    var showOccupiedWarning: Boolean = false
)
