package com.louis.app.cavity.model

/**
 * An interface specifying that the object know how to represent itself in a material Chip
 */
interface Chipable {
    fun getId(): Long
    fun getChipText(): String
}
