package com.louis.app.cavity.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tag", indices = [Index(value = ["name"], unique = true)])
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
) : Chipable {
    override fun getItemId() = id
    override fun getChipText() = name
    fun hasValidName() = name.isNotBlank()
}
