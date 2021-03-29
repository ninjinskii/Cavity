package com.louis.app.cavity.model

/**
 * An interface specifying that the object know how to represent itself in a material Chip
 */
interface Chipable {
    fun getItemId(): Long
    fun getChipText(): String
}
