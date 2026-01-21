package com.louis.app.cavity.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tag", indices = [Index(value = ["name"], unique = true)])
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
) :
    Chipable {

    @Ignore
    var selected: Boolean = false

    override fun getItemId() = id
    override fun getChipText() = name
    override fun isSelected() = selected
    fun hasValidName() = name.isNotBlank()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Tag

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + selected.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}
