package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "county", indices = [Index(value = ["name"], unique = true)])
data class County(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "pref_order") var prefOrder: Int
) : Chipable {
    override fun getItemId() = id
    override fun getChipText() = name

    override fun toString() = name

    fun hasValidName() = name.isNotBlank()
}
